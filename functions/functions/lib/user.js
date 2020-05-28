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
const userUtil = require("./util/userUtil");
admin.initializeApp(functions.config().firebase, "users");
const db = admin.firestore();
exports.syncUser = function (req, res) {
    return __awaiter(this, void 0, void 0, function* () {
        try {
            // Get the local user as well as the user from the database
            const localUser = req.body.localUser;
            const dbUser = yield userUtil.getUser(db, localUser.userId);
            console.log(`Syncing Local user: ${localUser}, with DB user: ${dbUser}`);
            // Sync DB fields for user with values from local/app
            dbUser.email = localUser.email;
            dbUser.secretCode = localUser.secretCode;
            dbUser.fcmId = localUser.fcmId;
            dbUser.avatarUrl = localUser.avatarUrl;
            dbUser.gender = localUser.gender;
            dbUser.nickname = localUser.nickname;
            dbUser.timeLastUnlocked = localUser.timeLastUnlocked;
            dbUser.timeRegistered = localUser.timeRegistered;
            // Then update the database with those values
            const syncedUser = userUtil.updateUser(db, dbUser);
            console.log(`User profile synced`);
            // And return the synced user
            res.status(200).send({
                user: syncedUser
            });
        }
        catch (error) {
            console.log(`An error occurred while syncing user profile: ${error}`);
            res.status(401).send(`An error occurred while syncing user profile: ${error}`);
        }
    });
};
exports.makeAdmin = function (req, res) {
    return __awaiter(this, void 0, void 0, function* () {
        const claireId = req.query.claireId;
        const userId = req.query.userId;
        const role = req.query.role;
        const accessCode = String(Math.floor(100000 + Math.random() * 900000));
        console.log(`Giving role: ${role} to user: ${userId} with Claire id: ${claireId} and access code: ${accessCode}`);
        if (userId.length === 0) {
            res.status(400).send("Please enter a userId");
            return;
        }
        if (role !== "SUPER_ADMIN" && role !== "ADMIN") {
            res.status(400).send("The admin role must be SUPER_ADMIN or ADMIN");
            return;
        }
        try {
            // Get the user with the given id
            yield db.collection('users').doc(userId).update({
                alterEgoId: claireId,
                alterEgoAccessCode: accessCode,
                userType: role
            });
            res.status(200).send(`User has been made an admin. User access code is: ${accessCode}`);
        }
        catch (error) {
            console.log(`An error occurred while making user admin: ${error}`);
            res.status(401).send(`An error occurred while making user admin: ${error}`);
        }
    });
};
//# sourceMappingURL=user.js.map