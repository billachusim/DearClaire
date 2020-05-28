import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import * as userUtil from './util/userUtil';

admin.initializeApp(functions.config().firebase, "users");
const db = admin.firestore();

export const syncUser = async function (req: functions.Request, res: functions.Response) {
    try {
        // Get the local user as well as the user from the database
        const localUser = req.body.localUser;
        const dbUser = await userUtil.getUser(db, localUser.userId);
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
    } catch (error) {
        console.log(`An error occurred while syncing user profile: ${error}`);
        res.status(401).send(`An error occurred while syncing user profile: ${error}`);
    }
};

export const makeAdmin = async function (req: functions.Request, res: functions.Response) {
    const claireId: string = req.query.claireId;
    const userId: string = req.query.userId;
    const role: string = req.query.role;
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
        await db.collection('users').doc(userId).update({
            alterEgoId: claireId,
            alterEgoAccessCode: accessCode,
            userType: role
        });
        res.status(200).send(`User has been made an admin. User access code is: ${accessCode}`)
    } catch (error) {
        console.log(`An error occurred while making user admin: ${error}`);
        res.status(401).send(`An error occurred while making user admin: ${error}`);
    }
};