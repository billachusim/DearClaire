import * as admin from 'firebase-admin';

export async function getUser(db: admin.firestore.Firestore, userId: string): Promise<FirebaseFirestore.DocumentData> {
    const user = await db.collection('users').doc(userId).get();
    return user.data();
}

export async function updateUser(db: admin.firestore.Firestore, user): Promise<object> {
    await db.collection('users').doc(user.userId).set(user);
    return user;
}