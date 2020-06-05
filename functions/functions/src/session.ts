import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import * as commentUtil from './util/commentUtil';
import * as sessionUtil from './util/sessionUtil';
import * as userUtil from './util/userUtil';
import * as commentNotificationUtil from './util/commentNotificationUtil';
import {Shard} from "./model/Shard";

const userSessionCountCollectionName = 'user_session_counters';
const userCommentCounterCollectionName = 'user_comment_counters';
const sessionCommentCounterName = 'session_comment_counter';
const  COLLECTION_SESSION_COMMENT_SHARDS = "session_comment_shards"

const defaultNumberOfShards = 10;

admin.initializeApp(functions.config().firebase);
const db = admin.firestore();

// Send a notification when new comment documents are added
export const sendCommentNotification =
    async function (snap: FirebaseFirestore.DocumentSnapshot, context: functions.EventContext) {
        const newComment = snap.data();
        console.log(`New comment added: ${newComment}`);

        try {
            // Get the session and the user that created the session
            const sessionId = context.params.sessionId;
            const session = await sessionUtil.getSession(db, sessionId);
            const sessionCreator = await userUtil.getUser(db, session.userId);
            const sessionComments = await commentUtil.getComments(db, sessionId);
            const commentUsers = await commentUtil.getCommentUsers(db, sessionComments, session);
            const messaging = admin.messaging();

            await commentNotificationUtil.sendNotificationToSessionCreator(messaging, session, sessionCreator, newComment);
            await commentNotificationUtil.sendNotificationToRegularUsers(messaging, commentUsers.regular, session, sessionCreator, newComment);
            await commentNotificationUtil.sendNotificationToAdmins(messaging, commentUsers.alterEgo, session, sessionCreator, newComment);
        } catch (error) {
            console.log(`An error occurred while sending new comment notification: ${error}`);
        }
    };

// Increase the number of session count for the user
export const incrementSessionCount =
    async function (snap: FirebaseFirestore.DocumentSnapshot, context: functions.EventContext) {
        const newSession = snap.data();
        console.log(`New session added: ${newSession}`);
        try {
            console.log(`Incrementing count for the new session added: ${newSession}`);
            const userSessionCountRef = db.collection(userSessionCountCollectionName).doc(newSession.userId);
            userSessionCountRef.get()
                .then(doc => {
                    if (!doc.exists) {
                        console.log('User session count not found, creating new one');
                        const newUserSessionCount = {
                            userId: newSession.userId,
                            numberOfSessions: 1
                        };
                        db.collection(userSessionCountCollectionName)
                            .doc(newUserSessionCount.userId).set(newUserSessionCount);
                    } else {
                        console.log('User session count found updating count', doc.data());
                        let numberOfSessions = doc.data().numberOfSessions;
                        numberOfSessions += 1;
                        db.collection(userSessionCountCollectionName)
                            .doc(newSession.userId).update({numberOfSessions: numberOfSessions})
                    }
                })
                .catch(err => {
                    console.log('an error occured while incrementing session count', err);
                });
        } catch (error) {
            console.log(`An error occurred while incrementing session count ${error}`);
        }
    };

// Increase the number of session count for the user
export const incrementCommentCount =
    async function (snap: FirebaseFirestore.DocumentSnapshot, context: functions.EventContext) {
        const newComment = snap.data();
        console.log(`New comment added: ${newComment}`);
        try {
            console.log(`Incrementing count for the new comment added: ${newComment}`);
            const userCommentCounttRef = db.collection(userCommentCounterCollectionName).doc(newComment.userId);
            userCommentCounttRef.get()
                .then(doc => {
                    if (!doc.exists) {
                        console.log('User comment count not found, creating new one');
                        const newUserCommentCount = {
                            userId: newComment.userId,
                            numberOfComments: 1
                        };
                        db.collection(userCommentCounterCollectionName)
                            .doc(newUserCommentCount.userId).set(newUserCommentCount);
                    } else {
                        console.log('User comment count found updating count', doc.data());
                        let numberOfComments = doc.data().numberOfComments;
                        numberOfComments += 1;
                        db.collection(userCommentCounterCollectionName)
                            .doc(newComment.userId).update({numberOfComments: numberOfComments})
                    }
                })
                .catch(err => {
                    console.log('an error occurred while incrementing comment count', err);
                });
        } catch (error) {
            console.log(`An error occurred while incrementing comment count ${error}`);
        }
    };
export const incrementSessionCommentCounter =
    async function (snap: FirebaseFirestore.DocumentSnapshot, context: functions.EventContext) {
        const sessionId = context.params.sessionId;
        // Update count in a transaction
        try {
            return db.runTransaction(t => {
                // Select a shard of the counter at random
                const shard_id = Math.floor(Math.random() * defaultNumberOfShards).toString();
                let shard: Shard = null;
                let newSessionCommentCountShardRef = db.collection(sessionCommentCounterName)
                    .doc(sessionId).collection(COLLECTION_SESSION_COMMENT_SHARDS).doc(shard_id);
                return t.get(newSessionCommentCountShardRef).then(doc => {
                    if (doc.exists) {
                         shard = <Shard> {
                                count: doc.data().count as number
                        };
                        t.update(newSessionCommentCountShardRef, shard);
                    }
                    else {
                        shard = <Shard> {
                            count: 1
                        };
                        t.set(newSessionCommentCountShardRef, shard);
                    }
                })
            });
        } catch (error) {
            console.log(`An error occurred while incrementing session comment count ${error}`);
        }
    };

