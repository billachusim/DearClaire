import * as admin from 'firebase-admin';
import * as userUtil from './userUtil';

export async function getComments(db: admin.firestore.Firestore, sessionId: string): Promise<FirebaseFirestore.DocumentData[]> {
    const getSessionComments = await db.collection('sessions').doc(sessionId).collection('comments').get();
    const sessionComments: FirebaseFirestore.DocumentData[] = [];
    getSessionComments.forEach(snap => {
        const comment = snap.data()
        console.log(`Get comment: ${comment}`);
        sessionComments.push(comment);
    });
    return sessionComments;
}

export async function getCommentUsers(db: admin.firestore.Firestore, sessionComments: FirebaseFirestore.DocumentData[],
                                      session: FirebaseFirestore.DocumentData) {
    const alterEgo = [];
    const regular = [];
    const uniqueUserIds = [];




    for (const comment of sessionComments) {
        console.log(`Getting the user with comment: ${comment}`);

        if (uniqueUserIds.indexOf(comment.userId) !== -1) {
            console.log(`We already have user, skipping another occurrence`);
            continue;
        }

        const user = await userUtil.getUser(db, comment.userId);
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

        const user = await userUtil.getUser(db, follower);

        regular.push(user);
    }

    return {
        alterEgo,
        regular
    };
}