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
const sessionHandler = require("./session");
const userHandler = require("./user");
// var serviceAccount = require('functions/google-services.json');
//
// admin.initializeApp({
//     credential: admin.credential.cert(serviceAccount),
//     databaseURL: 'https://clair-52652.firebaseio.com'
// });
exports.newComment = functions.firestore
    .document('sessions/{sessionId}/comments/{commentId}')
    .onCreate((snap, context) => __awaiter(this, void 0, void 0, function* () {
    yield sessionHandler.sendCommentNotification(snap, context);
    yield sessionHandler.incrementCommentCount(snap, context.params.sessionId);
    yield sessionHandler.incrementSessionCommentCounter(snap, context);
}));
exports.makeAdmin = functions.https.onRequest((req, res) => __awaiter(this, void 0, void 0, function* () {
    yield userHandler.makeAdmin(req, res);
}));
exports.syncUser = functions.https.onRequest((req, res) => __awaiter(this, void 0, void 0, function* () {
    yield userHandler.syncUser(req, res);
}));
exports.newSession = functions.firestore
    .document('sessions/{sessionId}')
    .onCreate((snap, context) => __awaiter(this, void 0, void 0, function* () {
    yield sessionHandler.incrementSessionCount(snap, context);
}));
//# sourceMappingURL=index.js.map