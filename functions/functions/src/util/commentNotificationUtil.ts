import * as admin from 'firebase-admin';
import * as notificationUtil from './notificationUtil';

const ACTION_SESSION_UPDATE = "session_update";

export async function sendNotificationToSessionCreator(messaging: admin.messaging.Messaging, session, sessionCreator, newComment) {
    if (newComment.userId === sessionCreator.userId) {
        console.log(`User responded to their own session, no need to send notification to him/her`);
    } else {
        console.log(`Sending notification to session creator`);
        const title = newComment.isUserAdmin ? "New Advice" : "New comment";
        const shortMessage = newComment.isUserAdmin ? "Claire just responded" : `${newComment.userNickname} just responded`;
        const longMessage = truncateWithEllipsis(newComment.message, 100);
        const notificationData = getNotificationData(sessionCreator.userId, title, shortMessage, longMessage,
            session.sessionId, newComment.commentUserId, newComment.commentId, false);

        console.log(`Notification data ${notificationData}`);
        await notificationUtil.sendNotification(messaging, sessionCreator.fcmId, ACTION_SESSION_UPDATE, notificationData);
    }
}

export async function sendNotificationToAdmins(messaging: admin.messaging.Messaging, alterEgos, session, sessionCreator, newComment) {
    if (alterEgos.length === 0) {
        console.log(`No alter ego have responded to this session`);
    } else {
        const title = 'New response';
        const shortMessage = truncateWithEllipsis(`${session.title}`, 100);
        const longMessage = truncateWithEllipsis(`${newComment.message}`, 100);

        for (const alterEgo of alterEgos) {
            if(alterEgo.userId === newComment.userId) {
                console.log(`Alter ego was the one that just commented, no need to send notification to him/her`);
                continue;
            }

            console.log(`Sending notification to alter ego: ${alterEgo}`);
            const notificationData = getNotificationData(alterEgo.userId, title, shortMessage, longMessage,
                session.sessionId, newComment.commentUserId, newComment.commentId, true);
            console.log(`Notification data ${notificationData}`);
            await notificationUtil.sendNotification(messaging, alterEgo.fcmId, ACTION_SESSION_UPDATE, notificationData);
        }
    }
}

export async function sendNotificationToRegularUsers(messaging: admin.messaging.Messaging, regularUsers, session, sessionCreator, newComment) {
    if (regularUsers.length === 0) {
        console.log(`No regular user have responded to this session`);
    } else {
        const title = 'New comment';
        const shortMessage = truncateWithEllipsis(`${session.title}`, 100);
        const longMessage = truncateWithEllipsis(`${newComment.message}`, 100);

        for (const regularUser of regularUsers) {
            if(regularUser.userId === newComment.userId) {
                console.log(`User was the one that just commented, no need to send notification to him/her`);
                continue;
            }

            console.log(`Sending notification to regularUser: ${regularUser}`);
            const notificationData = getNotificationData(regularUser.userId, title, shortMessage, longMessage,
                session.sessionId, newComment.commentUserId, newComment.commentId, false);

            console.log(`Notification data ${notificationData}`);
            await notificationUtil.sendNotification(messaging, regularUser.fcmId, ACTION_SESSION_UPDATE, notificationData);
        }
    }
}

function getNotificationData(userId: string, title: string, shortMessage: string, longMessage: string,
                             sessionId: string, commentUserId: string, commentId: string, isAlterEgo: boolean): object {
    return {
        userId,
        title,
        shortMessage,
        longMessage,
        sessionId,
        commentUserId,
        commentId,
        isAlterEgo
    }
}

function truncateWithEllipsis(str: string, length: number): string {
    // Truncate long message with an ellipsis so it fits in with FCM 4kb limit
    const ending = '...';
    if (str.length > length) {
        return str.substring(0, length - ending.length) + ending;
    }
    return str;
}