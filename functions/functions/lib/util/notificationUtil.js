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
function sendNotification(messaging, fcmTokens, action, notificationData) {
    return __awaiter(this, void 0, void 0, function* () {
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
            yield messaging.sendToDevice(fcmTokens, payload, options);
            console.log("New notification sent");
        }
        catch (error) {
            console.log(`An error occurred sending notification: ${error}`);
        }
    });
}
exports.sendNotification = sendNotification;
//# sourceMappingURL=notificationUtil.js.map