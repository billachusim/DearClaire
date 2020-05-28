import * as admin from 'firebase-admin';

export async function sendNotification(messaging: admin.messaging.Messaging, fcmTokens: string[], action: string, notificationData: object) {
    // Create the data payload used to send the push message
    const payload = {
        data: {
            action,
            extraData: JSON.stringify(notificationData)
        }
    };

    /* Create an options object that contains the time to live for the push and the priority. */
    const options = {
        priority: "high",
        timeToLive: 60 * 60 * 24 //24 hours
    };

    // send the notification
    try {
        await messaging.sendToDevice(fcmTokens, payload, options);
        console.log("New notification sent");
    } catch (error) {
        console.log(`An error occurred sending notification: ${error}`)
    }
}