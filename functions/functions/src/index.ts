import * as functions from 'firebase-functions';
import * as sessionHandler from './session';
import * as userHandler from './user';

// var serviceAccount = require('functions/google-services.json');
//
// admin.initializeApp({
//     credential: admin.credential.cert(serviceAccount),
//     databaseURL: 'https://clair-52652.firebaseio.com'
// });

export const newComment = functions.firestore
    .document('sessions/{sessionId}/comments/{commentId}')
    .onCreate(async (snap: FirebaseFirestore.DocumentSnapshot, context: functions.EventContext) => {
        await sessionHandler.sendCommentNotification(snap, context);
        await sessionHandler.incrementCommentCount(snap, context.params.sessionId);
        await sessionHandler.incrementSessionCommentCounter(snap, context);
    });

export const makeAdmin = functions.https.onRequest(async (req, res) => {
    await userHandler.makeAdmin(req, res);
});

export const syncUser = functions.https.onRequest(async (req, res) => {
    await userHandler.syncUser(req, res);
});

export const newSession = functions.firestore
    .document('sessions/{sessionId}')
    .onCreate(async (snap: FirebaseFirestore.DocumentSnapshot, context: functions.EventContext) => {
        await sessionHandler.incrementSessionCount(snap, context);
    });