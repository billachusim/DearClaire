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
const notificationUtil = require("./notificationUtil");
const ACTION_SESSION_UPDATE = "session_update";
function sendNotificationToSessionCreator(messaging, session, sessionCreator, newComment) {
    return __awaiter(this, void 0, void 0, function* () {
        if (newComment.userId === sessionCreator.userId) {
            console.log(`User responded to their own session, no need to send notification to him/her`);
        }
        else {
            console.log(`Sending notification to session creator`);
            const title = newComment.isUserAdmin ? "New Advice" : "New comment";
            const shortMessage = newComment.isUserAdmin ? "Claire just responded" : `${newComment.userNickname} just responded`;
            const longMessage = truncateWithEllipsis(newComment.message, 100);
            const notificationData = getNotificationData(sessionCreator.userId, title, shortMessage, longMessage, session.sessionId, newComment.commentUserId, newComment.commentId, false);
            console.log(`Notification data ${notificationData}`);
            yield notificationUtil.sendNotification(messaging, sessionCreator.fcmId, ACTION_SESSION_UPDATE, notificationData);
        }
    });
}
exports.sendNotificationToSessionCreator = sendNotificationToSessionCreator;
function sendNotificationToAdmins(messaging, alterEgos, session, sessionCreator, newComment) {
    return __awaiter(this, void 0, void 0, function* () {
        if (alterEgos.length === 0) {
            console.log(`No alter ego have responded to this session`);
        }
        else {
            const title = 'New response';
            const shortMessage = truncateWithEllipsis(`${session.title}`, 100);
            const longMessage = truncateWithEllipsis(`${newComment.message}`, 100);
            for (const alterEgo of alterEgos) {
                if (alterEgo.userId === newComment.userId) {
                    console.log(`Alter ego was the one that just commented, no need to send notification to him/her`);
                    continue;
                }
                console.log(`Sending notification to alter ego: ${alterEgo}`);
                const notificationData = getNotificationData(alterEgo.userId, title, shortMessage, longMessage, session.sessionId, newComment.commentUserId, newComment.commentId, true);
                console.log(`Notification data ${notificationData}`);
                yield notificationUtil.sendNotification(messaging, alterEgo.fcmId, ACTION_SESSION_UPDATE, notificationData);
            }
        }
    });
}
exports.sendNotificationToAdmins = sendNotificationToAdmins;
function sendNotificationToRegularUsers(messaging, regularUsers, session, sessionCreator, newComment) {
    return __awaiter(this, void 0, void 0, function* () {
        if (regularUsers.length === 0) {
            console.log(`No regular user have responded to this session`);
        }
        else {
            const title = 'New comment';
            const shortMessage = truncateWithEllipsis(`${session.title}`, 100);
            const longMessage = truncateWithEllipsis(`${newComment.message}`, 100);
            for (const regularUser of regularUsers) {
                if (regularUser.userId === newComment.userId) {
                    console.log(`User was the one that just commented, no need to send notification to him/her`);
                    continue;
                }
                console.log(`Sending notification to regularUser: ${regularUser}`);
                const notificationData = getNotificationData(regularUser.userId, title, shortMessage, longMessage, session.sessionId, newComment.commentUserId, newComment.commentId, false);
                console.log(`Notification data ${notificationData}`);
                yield notificationUtil.sendNotification(messaging, regularUser.fcmId, ACTION_SESSION_UPDATE, notificationData);
            }
        }
    });
}
exports.sendNotificationToRegularUsers = sendNotificationToRegularUsers;
function getNotificationData(userId, title, shortMessage, longMessage, sessionId, commentUserId, commentId, isAlterEgo) {
    return {
        userId,
        title,
        shortMessage,
        longMessage,
        sessionId,
        commentUserId,
        commentId,
        isAlterEgo
    };
}
function truncateWithEllipsis(str, length) {
    // Truncate long message with an ellipsis so it fits in with FCM 4kb limit
    const ending = '...';
    if (str.length > length) {
        return str.substring(0, length - ending.length) + ending;
    }
    return str;
}
//# sourceMappingURL=commentNotificationUtil.js.map