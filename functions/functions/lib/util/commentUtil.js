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
const userUtil = require("./userUtil");
function getComments(db, sessionId) {
    return __awaiter(this, void 0, void 0, function* () {
        const getSessionComments = yield db.collection('sessions').doc(sessionId).collection('comments').get();
        const sessionComments = [];
        getSessionComments.forEach(snap => {
            const comment = snap.data();
            console.log(`Get comment: ${comment}`);
            sessionComments.push(comment);
        });
        return sessionComments;
    });
}
exports.getComments = getComments;
function getCommentUsers(db, sessionComments, session) {
    return __awaiter(this, void 0, void 0, function* () {
        const alterEgo = [];
        const regular = [];
        const uniqueUserIds = [];
        for (const comment of sessionComments) {
            console.log(`Getting the user with comment: ${comment}`);
            if (uniqueUserIds.indexOf(comment.userId) !== -1) {
                console.log(`We already have user, skipping another occurrence`);
                continue;
            }
            const user = yield userUtil.getUser(db, comment.userId);
            if (comment.isUserAdmin) {
                uniqueUserIds.push(comment.userId);
                alterEgo.push(user);
            }
        }
        // also push notification to the followers of the post
        for (const follower of session.followers) {
            console.log(`Getting the follower with id: ${follower}`);
            if (uniqueUserIds.indexOf(follower) !== -1) {
                console.log(`We already have this user in the lis of followes, skipping another occurrence`);
                continue;
            }
            uniqueUserIds.push(follower);
            const user = yield userUtil.getUser(db, follower);
            regular.push(user);
        }
        return {
            alterEgo,
            regular
        };
    });
}
exports.getCommentUsers = getCommentUsers;
//# sourceMappingURL=commentUtil.js.map