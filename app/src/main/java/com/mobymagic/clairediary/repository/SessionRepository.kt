package com.mobymagic.clairediary.repository

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.util.FirestoreListLiveData
import com.mobymagic.clairediary.util.FirestoreSingleLiveData
import com.mobymagic.clairediary.util.PrefUtil
import com.mobymagic.clairediary.util.filters.FollowingSessionFilter
import com.mobymagic.clairediary.util.filters.ResultFilter
import com.mobymagic.clairediary.util.filters.TrendingSessionFilter
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Session
import com.mobymagic.clairediary.vo.UserActivity
import com.mobymagic.clairediary.vo.UserActivityType
import com.mobymagic.clairediary.vo.counters.Counter
import com.mobymagic.clairediary.vo.counters.Shard
import com.mobymagic.clairediary.vo.counters.UserSessionCounter
import timber.log.Timber
import java.util.*
import kotlin.math.floor

const val PREF_KEY_DRAFT_SESSION = "PREF_KEY_DRAFT_SESSION"
const val COLLECTION_SESSIONS = "sessions"
const val COLLECTION_USER_SESSION_COUNTERS = "user_session_counters"
const val COLLECTION_USER_ACTIVITY = "user_activity"
const val COLLECTION_USER_MEE_TOO_COUNTERS = "user_mee_too_counters"
const val COLLECTION_USER_MEE_TOO_SHARDS = "user_mee_too_shards"
const val COLLECTION_USER_FOLLOW_COUNTERS = "user_follow_counters"
const val COLLECTION_USER_FOLLOW_SHARDS = "user_follow_shards"
const val COLLECTION_USER_MEE_TOO_SHARDS_NUM = 10
const val LIMIT_SESSIONS_RESULT = 50L

class SessionRepository(
        private val appExecutors: AppExecutors,
        private val androidUtil: AndroidUtil,
        private val prefUtil: PrefUtil,
        private val gson: Gson,
        private val firestore: FirebaseFirestore,
        private val userRepository: UserRepository
) {

    fun getDraftSession(): LiveData<Session> {
        Timber.d("Getting draft session")
        val sessionLiveData: MutableLiveData<Session> = MutableLiveData()

        appExecutors.diskIO().execute {
            val draftSessionJson = prefUtil.getString(PREF_KEY_DRAFT_SESSION, null)

            if (draftSessionJson != null) {
                Timber.d("Got draft session, serializing")
                sessionLiveData.postValue(gson.fromJson(draftSessionJson, Session::class.java))
            } else {
                Timber.d("No draft session")
                sessionLiveData.postValue(null)
            }
        }

        return sessionLiveData
    }

    fun saveDraftSession(session: Session) {
        Timber.d("Saving draft session: %s", session)
        appExecutors.diskIO().execute {
            val sessionJson = gson.toJson(session)
            prefUtil.setString(PREF_KEY_DRAFT_SESSION, sessionJson)
            Timber.d("Draft session saved")
        }
    }

    fun clearDraftSession() {
        Timber.d("Clearing draft session")
        val sessionLiveData: MutableLiveData<Session> = MutableLiveData()
        appExecutors.diskIO().execute {
            prefUtil.setString(PREF_KEY_DRAFT_SESSION, null)
            Timber.d("Draft session cleared")
            sessionLiveData.postValue(null)
        }
    }

    fun getSessionWithId(sessionId: String): LiveData<Resource<Session>> {
        Timber.d("Getting session with id: %s", sessionId)
        val query = firestore.collection(COLLECTION_SESSIONS)
                .whereEqualTo("sessionId", sessionId)
                .limit(1)

        return FirestoreSingleLiveData(androidUtil, query, Session::class.java)
    }

    /**
     * Get user sessions that have been archived but not flagged
     * @param lastSession The last session from previous request. Used for pagination
     * @param userId The id of the user to get archived sessions for
     * @return LiveData
     */
    fun getArchivedSessions(
            lastSession: Session?,
            userId: String
    ): LiveData<Resource<List<Session>>> {
        Timber.d("Getting archived sessions. Last session: %s", lastSession)
        val query = firestore.collection(COLLECTION_SESSIONS)
                .whereEqualTo("archived", true)
                .whereEqualTo("flagged", false)
                .whereEqualTo("userId", userId)
                .orderBy("timeLastActivity", Query.Direction.DESCENDING)
                .limit(LIMIT_SESSIONS_RESULT)

        if (lastSession != null) query.startAfter(lastSession.timeLastActivity)

        return FirestoreListLiveData(androidUtil, query, Session::class.java, null)
    }

    /**
     * Get sessions that have been featured but not flagged and archived
     * @param lastSession The last session from previous request. Used for pagination
     * @return LiveData
     */
    fun getFeaturedSessions(lastSession: Session?, userId: String): LiveData<Resource<List<Session>>> {
        Timber.d("Getting featured sessions. Last session: %s", lastSession)
        val query = firestore.collection(COLLECTION_SESSIONS)
                .whereEqualTo("featured", true)
                .whereEqualTo("archived", false)
                .whereEqualTo("flagged", false)
                .orderBy("timeLastActivity", Query.Direction.DESCENDING)
                .limit(LIMIT_SESSIONS_RESULT)

        if (lastSession != null) query.startAfter(lastSession.timeLastActivity)
        var featuredSessionFilter: ResultFilter<Session>? = null
        if (!TextUtils.isEmpty(userId)) {
            featuredSessionFilter = TrendingSessionFilter(userId)
        }

        return FirestoreListLiveData(androidUtil, query, Session::class.java, featuredSessionFilter)
    }

    /**
     * Get sessions that have been featured but not flagged and archived
     * @param lastSession The last session from previous request. Used for pagination
     * @return LiveData
     */
    fun getPublicSessions(lastSession: Session?, userId: String): LiveData<Resource<List<Session>>> {
        Timber.d("Getting public sessions. Last session: %s", lastSession)
        val query = firestore.collection(COLLECTION_SESSIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("private", false)
                .whereEqualTo("archived", false)
                .whereEqualTo("flagged", false)
                .orderBy("timeLastActivity", Query.Direction.DESCENDING)
                .limit(LIMIT_SESSIONS_RESULT)

        if (lastSession != null) query.startAfter(lastSession.timeLastActivity)


        return FirestoreListLiveData(androidUtil, query, Session::class.java, null)
    }

    /**
     * Get sessions that have been featured but not flagged and archived
     * @param lastSession The last session from previous request. Used for pagination
     * @return LiveData
     */
    fun getFollowingSessions(lastSession: Session?, userId: String): LiveData<Resource<List<Session>>> {
        Timber.d("Getting featured sessions. Last session: %s", lastSession)
        val query = firestore.collection(COLLECTION_SESSIONS)
                .whereEqualTo("featured", true)
                .whereEqualTo("archived", false)
                .whereEqualTo("flagged", false)
                .orderBy("timeLastActivity", Query.Direction.DESCENDING)
                .limit(LIMIT_SESSIONS_RESULT)

        if (lastSession != null) query.startAfter(lastSession.timeLastActivity)

        val featuredSessionFilter: ResultFilter<Session> = FollowingSessionFilter(userId)

        return FirestoreListLiveData(androidUtil, query, Session::class.java, featuredSessionFilter)
    }

    /**
     * Get the user sessions that haven't been flagged and archived
     * @param lastSession The last session from previous request. Used for pagination
     * @param userId The id of the user to get diary sessions for
     * @return LiveData
     */
    fun getDiarySessions(lastSession: Session?, userId: String): LiveData<Resource<List<Session>>> {
        Timber.d("Getting diary sessions. Last session: %s", lastSession)

        val query = firestore.collection(COLLECTION_SESSIONS)
                .whereEqualTo("archived", false)
                .whereEqualTo("flagged", false)
                .whereEqualTo("userId", userId)
                .orderBy("timeLastActivity", Query.Direction.DESCENDING)
                .limit(LIMIT_SESSIONS_RESULT)

        if (lastSession != null) query.startAfter(lastSession.timeLastActivity)

        return FirestoreListLiveData(androidUtil, query, Session::class.java, null)
    }

    /**
     * Get new sessions that no admin has responded to before
     * @param lastSession The last session from previous request. Used for pagination
     * @return LiveData
     */
    fun getNonAssignedSessions(lastSession: Session?): LiveData<Resource<List<Session>>> {
        Timber.d("Getting non assigned sessions. Last session: %s", lastSession)
        val query = firestore.collection(COLLECTION_SESSIONS)
                .whereEqualTo("archived", false)
                .whereEqualTo("flagged", false)
                .whereEqualTo("repliesEnabled", true)
                .whereEqualTo("respondentUserId", "")
                .orderBy("timeLastActivity", Query.Direction.DESCENDING)
                .limit(LIMIT_SESSIONS_RESULT)

        if (lastSession != null) query.startAfter(lastSession.timeLastActivity)

        return FirestoreListLiveData(androidUtil, query, Session::class.java, null)
    }

    /**
     * Get sessions that have been assigned to the admin user. When an admin replies a session,
     * the session automatically gets assigned to him
     * @param lastSession The last session from previous request. Used for pagination
     * @param userId The id of the user to get assigned sessions for
     * @return LiveData
     */
    fun getAssignedSessions(
            lastSession: Session?,
            userId: String
    ): LiveData<Resource<List<Session>>> {
        Timber.d("Getting assigned sessions. Last session: %s", lastSession)
        val query = firestore.collection(COLLECTION_SESSIONS)
                .whereEqualTo("archived", false)
                .whereEqualTo("flagged", false)
                .whereEqualTo("respondentUserId", userId)
                .orderBy("timeLastActivity", Query.Direction.DESCENDING)
                .limit(LIMIT_SESSIONS_RESULT)

        if (lastSession != null) query.startAfter(lastSession.timeLastActivity)

        return FirestoreListLiveData(androidUtil, query, Session::class.java, null)
    }

    /**
     * Get sessions that have been flagged by an admin
     * @param lastSession The last session from previous request. Used for pagination
     * @return LiveData
     */
    fun getFlaggedSessions(lastSession: Session?): LiveData<Resource<List<Session>>> {
        Timber.d("Getting flagged sessions. Last session: %s", lastSession)
        val query = firestore.collection(COLLECTION_SESSIONS)
                .whereEqualTo("flagged", true)
                .orderBy("timeLastActivity", Query.Direction.DESCENDING)
                .limit(LIMIT_SESSIONS_RESULT)

        if (lastSession != null) query.startAfter(lastSession.timeLastActivity)

        return FirestoreListLiveData(androidUtil, query, Session::class.java, null)
    }

    /**
     * gets session for a user withing the date range
     * @param userId The id of the user to get  sessions for
     * @return LiveData
     */
    fun getUserSessionsByDate(
            userId: String,
            startDate: Date,
            endDate: Date
    ): LiveData<Resource<List<Session>>> {
        Timber.d("Getting sessions for user %s from %s to %s", userId, startDate, endDate)
        val query = firestore.collection(COLLECTION_SESSIONS)
                .whereGreaterThanOrEqualTo("timeCreated", startDate)
                .whereLessThanOrEqualTo("timeCreated", endDate)
                .whereEqualTo("userId", userId)

        return FirestoreListLiveData(androidUtil, query, Session::class.java, null)
    }

    /**
     * gets session for a user withing the date range
     * @param userId The id of the user to get  sessions for
     * @return LiveData
     */
    fun getUserSessionsByDate(
            userId: String,
            startDate: Date): LiveData<Resource<List<Session>>> {
        Timber.d("Getting sessions for user %s from %s ", userId, startDate)
        val query = firestore.collection(COLLECTION_SESSIONS)
                .whereGreaterThanOrEqualTo("timeCreated", startDate)
                .whereEqualTo("userId", userId)

        return FirestoreListLiveData(androidUtil, query, Session::class.java, null)
    }

    /**
     * Get user sessions that have been created
     * param lastSession The last session from previous request. Used for pagination
     * @return LiveData
     */
    fun getUserBestSession(userId: String): LiveData<Resource<List<Session>>> {
        Timber.d("Getting best sessions for user: %s", userId)
        val query = firestore.collection(COLLECTION_SESSIONS)
                .whereEqualTo("userId", userId)
                .orderBy("meTooFollowCount", Query.Direction.DESCENDING)
                .limit(3)
        return FirestoreListLiveData(androidUtil, query, Session::class.java, null)
    }


    /**
     * Get all sessions that have been created
     * @param lastSession The last session from previous request. Used for pagination
     * @return LiveData
     */
    fun getAllSessions(lastSession: Session?): LiveData<Resource<List<Session>>> {
        Timber.d("Getting all sessions. Last session: %s", lastSession)
        val query = firestore.collection(COLLECTION_SESSIONS)
                .orderBy("timeCreated", Query.Direction.DESCENDING)
                .limit(LIMIT_SESSIONS_RESULT)

        if (lastSession != null) query.startAfter(lastSession.timeLastActivity)

        return FirestoreListLiveData(androidUtil, query, Session::class.java, null)
    }

    /**
     * Adds a new session into the database
     * @param session The session to add
     * @return LiveData
     */
    fun addSession(session: Session): LiveData<Resource<Session>> {
        Timber.d("Adding new session: %s", session)
        val addSessionRequestLiveData = MutableLiveData<Resource<Session>>()

        // Set resource into loading state
        addSessionRequestLiveData.value = Resource.loading(
                androidUtil
                        .getString(R.string.new_session_saving)
        )

        // Set the session document id
        val newSessionRef = firestore.collection(COLLECTION_SESSIONS).document()
        session.sessionId = newSessionRef.id

        // Save session into the Firestore database
        newSessionRef.set(session)
                .addOnSuccessListener {
                    Timber.d("Session successfully added")
                    addSessionRequestLiveData.value = Resource.success(session)
                }

                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error saving new session")
                    addSessionRequestLiveData.value = Resource.error(
                            androidUtil
                                    .getString(R.string.new_session_save_error)
                    )
                }

        return addSessionRequestLiveData
    }

    /**
     * Updates the given session in the database
     * @param session The session to update
     * @param clientUserId The id of the user that triggered this action
     * @return LiveData
     */
    fun updateSession(session: Session, clientUserId: String? = null,
                      userActivityType: String? = null,
                      shouldIncrementMeeToo: Boolean? = null,
                      shouldIncrementFollow: Boolean? = null,
                      fromAlterEgo: Boolean? = null): LiveData<Resource<Session>> {
        Timber.d("Updating session: %s", session)
        val updateSessionRequestLiveData = MutableLiveData<Resource<Session>>()
        session.meTooFollowCount = session.meToos!!.count() + session.followers.count()
        // Set resource into loading state
        updateSessionRequestLiveData.value = Resource.loading(
                androidUtil
                        .getString(R.string.session_updating)
        )

        // Get a reference to the session to update
        val sessionToUpdateRef =
                firestore.collection(COLLECTION_SESSIONS).document(session.sessionId.toString())

        // Update the session by merging changes
        sessionToUpdateRef.set(session, SetOptions.merge())
                .addOnSuccessListener { documentReference ->
                    Timber.d("Session successfully updated: %s", documentReference)
                    updateSessionRequestLiveData.value = Resource.success(session)

                    //TODO: Use an event bus for these operations
                    if (clientUserId != null && userActivityType != null) {
                        //add user activity except it is a call to decrement mee too or decrement follow
                        if (shouldIncrementMeeToo != null && shouldIncrementMeeToo == false) {

                        } else if (shouldIncrementFollow != null && shouldIncrementFollow == false) {

                        } else {
                            addUserActivityFromSessionUpdate(session, clientUserId,
                                    userActivityType, fromAlterEgo)
                        }

                    }
                    if (shouldIncrementMeeToo != null) {
                        try {
                            incrementUserMeeTooCount(session.userId.toString(), Counter(), shouldIncrementMeeToo)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }

                    if (shouldIncrementFollow != null) {
                        try {
                            incrementUserFollowCount(session.userId.toString(), Counter(), shouldIncrementFollow)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error updating session")
                    updateSessionRequestLiveData.value = Resource.error(
                            androidUtil
                                    .getString(R.string.session_update_error)
                    )
                }

        return updateSessionRequestLiveData
    }

    /**
     * get session counter by user Id
     */
    fun getUserSessionCounter(userId: String): LiveData<Resource<List<UserSessionCounter>>> {
        Timber.d("Getting all user session counters for user %s", userId)
        val query = firestore.collection(COLLECTION_USER_SESSION_COUNTERS)
                .whereEqualTo("userId", userId)
                .limit(50)
        return FirestoreListLiveData(androidUtil, query, UserSessionCounter::class.java, null)
    }

    /**
     * get session counter by user Id
     */
    private fun getUserSessionCounterValue(userId: String): UserSessionCounter? {
        var userSessionCounter: UserSessionCounter? = null
        Timber.d("Getting all user session counters for user %s", userId)
        val query = firestore.collection(COLLECTION_USER_SESSION_COUNTERS)
                .whereEqualTo("userId", userId)
                .limit(50)
        query.addSnapshotListener { querySnapshot, firestoreException ->
            Timber.d("query snapshot %s", firestoreException?.message)
            if (querySnapshot?.documents != null && querySnapshot.documents.size > 0) {
                userSessionCounter = querySnapshot.documents[0].toObject(UserSessionCounter::class.java)
            }
        }
        return userSessionCounter
    }

    /**
     * get session counter by user Id
     */
    fun incrementUserSessionCount(userId: String) {
        Thread(Runnable {
            var userSessionCounter: UserSessionCounter? = getUserSessionCounterValue(userId)

            if (userSessionCounter == null) {
                userSessionCounter = UserSessionCounter()
                userSessionCounter.userId = userId
                userSessionCounter.numberOfSessions = 1
                createUserSessionCount(userSessionCounter)
            } else {
                userSessionCounter.numberOfSessions++
                updateUserSessionCount(userSessionCounter)
            }
        }).start()

    }

    private fun createUserSessionCount(userSessionCounter: UserSessionCounter) {
        Timber.d("Adding new userSessionCounter: %s", userSessionCounter)

        // Set the session document id
        val newuserSessionCounterRef =
                firestore.collection(COLLECTION_USER_SESSION_COUNTERS)
                        .document(userSessionCounter.userId)

        // Save session into the Firestore database
        newuserSessionCounterRef.set(userSessionCounter)
                .addOnSuccessListener {
                    Timber.d("UserSessionCounter successfully added")
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error saving new UserSessionCounter")
                }
    }

    private fun updateUserSessionCount(userSessionCounter: UserSessionCounter) {
        Timber.d("Updating userSessionCounter: %s", userSessionCounter)

        // Get a reference to the session to update
        val sessionToUpdateRef =
                firestore.collection(COLLECTION_USER_SESSION_COUNTERS)
                        .document(userSessionCounter.userId)

        // Update the session by merging changes
        sessionToUpdateRef.set(userSessionCounter, SetOptions.merge())
                .addOnSuccessListener { documentReference ->
                    Timber.d("UserSessionCounter successfully updated: %s", documentReference)
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error updating UserSessionCounter")
                }
    }

    /**
     * writes an activity for the action that the user just took
     */
    private fun addUserActivityFromSessionUpdate(session: Session,
                                                 userId: String, userActivityType: String,
                                                 fromAlterEgo: Boolean?) {
        Thread(Runnable {
            val userActivity = UserActivity()
            userActivity.activityType = userActivityType
            userActivity.clientId = userId
            userActivity.sessionId = session.sessionId.toString()
            userActivity.userId = session.userId

            if (fromAlterEgo != null && !fromAlterEgo) {
                val user = userRepository.getLoggedInUser()
                if (user != null) {
                    userActivity.clientNickname = user.nickname
                }
            }

            if (fromAlterEgo != null && fromAlterEgo
                    && (userActivityType == UserActivityType.COMMENT
                            || userActivityType == UserActivityType.ADVISE)) {
                userActivity.clientNickname = "Claire"
            }
            addUserActivity(userActivity)
        }).start()
    }

    /**
     * Adds new user activity
     * @param userActivity The activity to be added
     */
    private fun addUserActivity(userActivity: UserActivity) {
        // if the user is reacting to his own post, then no need to to save it
        if (userActivity.userId == userActivity.clientId) return
        Timber.d("Adding new user Activity: %s", userActivity)
        // Set the userActivity document Id
        val newUserActivityRef = firestore.collection(COLLECTION_USER_ACTIVITY).document()
        userActivity.userActivityId = newUserActivityRef.id

        // Save userActivity into the Firestore database
        newUserActivityRef.set(userActivity)
                .addOnSuccessListener {
                    Timber.d("User Activity successfully added")
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error saving new session")
                }
    }

    /**
     * get user activity that was made to a user session
     */
    fun getUserActivity(userId: String): LiveData<Resource<List<UserActivity>>> {
        Timber.d("Getting Activity user %s", userId)
        val query = firestore.collection(COLLECTION_USER_ACTIVITY)
                .whereEqualTo("userId", userId)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .limit(50)
        return FirestoreListLiveData(androidUtil, query, UserActivity::class.java, null)
    }

    /**
     * get user activity that a user made
     */
    fun getActivityByUser(userId: String): LiveData<Resource<List<UserActivity>>> {
        Timber.d("Getting Activity user %s", userId)
        val query = firestore.collection(COLLECTION_USER_ACTIVITY)
                .whereEqualTo("clientId", userId)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .limit(50)
        return FirestoreListLiveData(androidUtil, query, UserActivity::class.java, null)
    }

    private fun incrementUserFollowCount(userId: String, counter: Counter, shouldIncrementFollow: Boolean = true) {
        Thread(Runnable {
            firestore.runTransaction { it ->
                it.apply {
                    val shardId = floor(Math.random() * counter.numberOfShards).toInt()
                    var shard: Shard? = null
                    firestore.collection(COLLECTION_USER_FOLLOW_COUNTERS)
                            .document(userId).collection(COLLECTION_USER_FOLLOW_SHARDS)
                            .document(shardId.toString()).get().addOnCompleteListener {

                                if (it.result!!.exists()) {
                                    shard = Shard(it.result!!.data!!["count"] as Long)
                                }
                                if (shard == null) {
                                    shard = if (shouldIncrementFollow) {
                                        Shard(1)
                                    } else {
                                        Shard(-1)
                                    }
                                    createFollowShard(userId, shard!!, shardId)
                                } else {
                                    if (shouldIncrementFollow) {
                                        shard!!.count++
                                    } else {
                                        shard!!.count--
                                    }

                                    updateUserFollowCountShard(userId, shard!!, shardId)
                                }
                            }
                }

            }
        }).start()
    }

    /**
     * Adds new user activity
     * param userActivity The activity to be added
     */
    private fun createFollowShard(userId: String, shard: Shard, shardId: Int) {
        Timber.d("Adding new follow Shard: %s", shard)
        // Set the shard document Id
        val newMeeTooShardRef = firestore.collection(COLLECTION_USER_FOLLOW_COUNTERS)
                .document(userId)
                .collection(COLLECTION_USER_FOLLOW_SHARDS)
                .document(shardId.toString())


        // Save userActivity into the Firestore database
        newMeeTooShardRef.set(shard)
                .addOnSuccessListener {
                    Timber.d("User Follow Shard successfully added")
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error saving Follow Shard")
                }
    }

    private fun updateUserFollowCountShard(userId: String, shard: Shard, shardId: Int) {
        Timber.d("Updating user follow shard: %s", shard)

        val shardToUpdatRef =
                firestore.collection(COLLECTION_USER_FOLLOW_COUNTERS)
                        .document(userId)
                        .collection(COLLECTION_USER_FOLLOW_SHARDS)
                        .document(shardId.toString())

        shardToUpdatRef.set(shard, SetOptions.merge())
                .addOnSuccessListener { documentReference ->
                    Timber.d("Follow Shard Updated successfully updated: %s", documentReference)
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error updating Follow Shard")
                }
    }

    fun getFollowSingleShard(userId: String, shardId: Int): DocumentReference {
        return firestore.collection(COLLECTION_USER_FOLLOW_COUNTERS)
                .document(userId).collection(COLLECTION_USER_FOLLOW_SHARDS)
                .document(shardId.toString())
    }

    fun getNumberOfFollowaForUser(userId: String): LiveData<Resource<List<Shard>>> {
        val query = firestore.collection(COLLECTION_USER_FOLLOW_COUNTERS)
                .document(userId).collection(COLLECTION_USER_FOLLOW_SHARDS).limit(100)

        return FirestoreListLiveData(androidUtil, query, Shard::class.java, null)
    }


    private fun incrementUserMeeTooCount(userId: String, counter: Counter, shouldIncrementMeeToo: Boolean = true) {
        Thread(Runnable {
            firestore.runTransaction { it ->
                it.apply {
                    val shardId = floor(Math.random() * counter.numberOfShards).toInt()
                    var shard: Shard? = null
                    firestore.collection(COLLECTION_USER_MEE_TOO_COUNTERS)
                            .document(userId).collection(COLLECTION_USER_MEE_TOO_SHARDS)
                            .document(shardId.toString()).get().addOnCompleteListener {

                                if (it.result!!.exists()) {
                                    shard = Shard(it.result!!.data!!["count"] as Long)
                                }
                                if (shard == null) {
                                    shard = if (shouldIncrementMeeToo) {
                                        Shard(1)
                                    } else {
                                        Shard(-1)
                                    }
                                    createMeeTooShard(userId, shard!!, shardId)
                                } else {
                                    if (shouldIncrementMeeToo) {
                                        shard!!.count++
                                    } else {
                                        shard!!.count--
                                    }

                                    updateUserMeeTooCountShard(userId, shard!!, shardId)
                                }
                            }
                }

            }
        }).start()
    }

    /**
     * Adds new user activity
     * param userActivity The activity to be added
     */
    private fun createMeeTooShard(userId: String, shard: Shard, shardId: Int) {
        Timber.d("Adding new mee too  Shard: %s", shard)
        // Set the shard document Id
        val newMeeTooShardRef = firestore.collection(COLLECTION_USER_MEE_TOO_COUNTERS)
                .document(userId)
                .collection(COLLECTION_USER_MEE_TOO_SHARDS)
                .document(shardId.toString())


        // Save userActivity into the Firestore database
        newMeeTooShardRef.set(shard)
                .addOnSuccessListener {
                    Timber.d("User Mee Too Shard  successfully added")
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error saving User Mee Too Shard")
                }
    }

    private fun updateUserMeeTooCountShard(userId: String, shard: Shard, shardId: Int) {
        Timber.d("Updating user mee too shard: %s", shard)

        // Get a reference to the session to update
        val shardToUpdatRef =
                firestore.collection(COLLECTION_USER_MEE_TOO_COUNTERS)
                        .document(userId)
                        .collection(COLLECTION_USER_MEE_TOO_SHARDS)
                        .document(shardId.toString())

        // Update the session by merging changes
        shardToUpdatRef.set(shard, SetOptions.merge())
                .addOnSuccessListener { documentReference ->
                    Timber.d("Mee Too Shard updated successfully : %s", documentReference)
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error updating Mee Too Shard")
                }
    }

    fun getMeeTooSingleShard(userId: String, shardId: Int): DocumentReference {
        return firestore.collection(COLLECTION_USER_MEE_TOO_COUNTERS)
                .document(userId).collection(COLLECTION_USER_MEE_TOO_SHARDS)
                .document(shardId.toString())
    }

    fun getNumberOfMeeToosForUser(userId: String): LiveData<Resource<List<Shard>>> {
        val query = firestore.collection(COLLECTION_USER_MEE_TOO_COUNTERS)
                .document(userId).collection(COLLECTION_USER_MEE_TOO_SHARDS).limit(100)

        return FirestoreListLiveData(androidUtil, query, Shard::class.java, null)
    }
}