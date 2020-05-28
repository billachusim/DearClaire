import * as admin from 'firebase-admin';

export async function getSession(db: admin.firestore.Firestore, sessionId: string): Promise<FirebaseFirestore.DocumentData> {
    const session = await db.collection('sessions').doc(sessionId).get();
    return session.data();
}

export async function getSessionFollowers(session: FirebaseFirestore.DocumentData){
    return session.followers;
}