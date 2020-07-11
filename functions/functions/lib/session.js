"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
const functions = require("firebase-functions");
const admin = require("firebase-admin");
const commentUtil = require("./util/commentUtil");
const sessionUtil = require("./util/sessionUtil");
const userUtil = require("./util/userUtil");
const commentNotificationUtil = require("./util/commentNotificationUtil");
const userSessionCountCollectionName = 'user_session_counters';
const userCommentCounterCollectionName = 'user_comment_counters';
const sessionCommentCounterName = 'session_comment_counter';
const COLLECTION_SESSION_COMMENT_SHARDS = "session_comment_shards";
const defaultNumberOfShards = 100;
admin.initializeApp(functions.config().firebase);
const db = admin.firestore();
// Send a notification when new comment documents are added
exports.sendCommentNotification = function (snap, context) {
    return __awaiter(this, void 0, void 0, function* () {
        const newComment = snap.data();
        console.log(`New comment added: ${newComment}`);
        try {
            // Get the session and the user that created the session
            const sessionId = context.params.sessionId;
            const session = yield sessionUtil.getSession(db, sessionId);
            const sessionCreator = yield userUtil.getUser(db, session.userId);
            const sessionComments = yield commentUtil.getComments(db, sessionId);
            const commentUsers = yield commentUtil.getCommentUsers(db, sessionComments, session);
            const messaging = admin.messaging();
            yield commentNotificationUtil.sendNotificationToSessionCreator(messaging, session, sessionCreator, newComment);
            yield commentNotificationUtil.sendNotificationToRegularUsers(messaging, commentUsers.regular, session, sessionCreator, newComment);
            yield commentNotificationUtil.sendNotificationToAdmins(messaging, commentUsers.alterEgo, session, sessionCreator, newComment);
        }
        catch (error) {
            console.log(`An error occurred while sending new comment notification: ${error}`);
        }
    });
};
// Increase the number of session count for the user
exports.incrementSessionCount = function (snap, context) {
    return __awaiter(this, void 0, void 0, function* () {
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
                }
                else {
                    console.log('User session count found updating count', doc.data());
                    let numberOfSessions = doc.data().numberOfSessions;
                    numberOfSessions += 1;
                    db.collection(userSessionCountCollectionName)
                        .doc(newSession.userId).update({ numberOfSessions: numberOfSessions });
                }
            })
                .catch(err => {
                console.log('an error occurred while incrementing session count', err);
            });
        }
        catch (error) {
            console.log(`An error occurred while incrementing session count ${error}`);
        }
    });
};
// Increase the number of advise count for the user
exports.incrementCommentCount = function (snap, context) {
    return __awaiter(this, void 0, void 0, function* () {
        const newComment = snap.data();
        console.log(`New comment added: ${newComment}`);
        try {
            console.log(`Incrementing count for the new comment added: ${newComment}`);
            const userCommentCountRef = db.collection(userCommentCounterCollectionName).doc(newComment.userId);
            userCommentCountRef.get()
                .then(doc => {
                if (!doc.exists) {
                    console.log('User comment count not found, creating new one');
                    const newUserCommentCount = {
                        userId: newComment.userId,
                        numberOfComments: 1
                    };
                    db.collection(userCommentCounterCollectionName)
                        .doc(newUserCommentCount.userId).set(newUserCommentCount);
                }
                else {
                    console.log('User comment count found updating count', doc.data());
                    let numberOfComments = doc.data().numberOfComments;
                    numberOfComments += 1+;
                    db.collection(userCommentCounterCollectionName)
                        .doc(newComment.userId).update({ numberOfComments: numberOfComments });
                }
            })
                .catch(err => {
                console.log('an error occurred while incrementing comment count', err);
            });
        }
        catch (error) {
            console.log(`An error occurred while incrementing comment count ${error}`);
        }
    });
};
exports.incrementSessionCommentCounter = function (snap, context) {
    return __awaiter(this, void 0, void 0, function* () {
        const sessionId = context.params.sessionId;
        // Update count in a transaction
        try {
            return db.runTransaction(t => {
                // Select a shard of the counter at random
                const shard_id = Math.floor(Math.random() * defaultNumberOfShards).toString();
                let shard = null;
                let newSessionCommentCountShardRef = db.collection(sessionCommentCounterName)
                    .doc(sessionId).collection(COLLECTION_SESSION_COMMENT_SHARDS).doc(shard_id);
                return t.get(newSessionCommentCountShardRef).then(doc => {
                    if (doc.exists) {
                        shard = {
                            count: doc.data().count
                        };
                        t.update(newSessionCommentCountShardRef, shard);
                    }
                    else {
                        shard = {
                            count: 1
                        };
                        t.set(newSessionCommentCountShardRef, shard);
                    }
                });
            });
        }
        catch (error) {
            console.log(`An error occurred while incrementing session comment count ${error}`);
        }
    });
};
//# sourceMappingURL=session.js.map