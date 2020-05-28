package com.mobymagic.clairediary.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.mobymagic.clairediary.Constants
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.jobs.SyncProfileJob
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.util.FirestoreSingleLiveData
import com.mobymagic.clairediary.util.PrefUtil
import com.mobymagic.clairediary.vo.AsyncRequest
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.User
import timber.log.Timber
import java.util.*

private const val COLLECTION_USER = "users"

class UserRepository(
        private val androidUtil: AndroidUtil,
        private val firestore: FirebaseFirestore,
        private val prefUtil: PrefUtil
) {

    fun getLoggedInUser(): User? {
        val avatarUrl = prefUtil.getString(Constants.PREF_KEY_USER_AVATAR_URL, null)
        val nickname = prefUtil.getString(Constants.PREF_KEY_USER_NICKNAME, null)
        val secretCode = prefUtil.getString(Constants.PREF_KEY_USER_SECRET_CODE, null)
        val email = prefUtil.getString(Constants.PREF_KEY_USER_EMAIL, null)
        val fcmId = prefUtil.getString(Constants.PREF_KEY_USER_FCM_ID, null)
        val gender = prefUtil.getString(Constants.PREF_KEY_USER_GENDER, null)
        val userId = prefUtil.getString(Constants.PREF_KEY_USER_ID, null)
        val lastUnlockedTime = Date(prefUtil.getLong(Constants.PREF_KEY_USER_LAST_UNLOCKED, 0))
        val registeredTime = Date(prefUtil.getLong(Constants.PREF_KEY_USER_TIME_REGISTERED, 0))
        val alterEgoId = prefUtil.getString(Constants.PREF_KEY_ALTER_EGO_ID, null)
        val alterEgoAccessCode = prefUtil.getString(Constants.PREF_KEY_ALTER_EGO_ACCESS_CODE, null)
        val userType = User.UserType.valueOf(
                prefUtil.getString(
                        Constants.PREF_KEY_USER_USER_TYPE,
                        User.UserType.REGULAR.name
                )!!
        )

        return if (gender != null && nickname != null && email != null && secretCode != null) {
            User(
                    avatarUrl,
                    fcmId,
                    gender,
                    nickname,
                    email,
                    secretCode,
                    userType,
                    lastUnlockedTime,
                    registeredTime,
                    userId,
                    alterEgoId ?: "",
                    alterEgoAccessCode ?: ""
            )
        } else {
            null
        }
    }

    fun setLoggedInUser(user: User) {
        prefUtil.setString(Constants.PREF_KEY_USER_AVATAR_URL, user.avatarUrl)
        prefUtil.setString(Constants.PREF_KEY_USER_NICKNAME, user.nickname)
        prefUtil.setString(Constants.PREF_KEY_USER_SECRET_CODE, user.secretCode)
        prefUtil.setString(Constants.PREF_KEY_USER_EMAIL, user.email)
        prefUtil.setString(Constants.PREF_KEY_USER_FCM_ID, FirebaseInstanceId.getInstance().token)
        prefUtil.setString(Constants.PREF_KEY_USER_GENDER, user.gender)
        prefUtil.setString(Constants.PREF_KEY_USER_ID, user.userId)
        prefUtil.setString(Constants.PREF_KEY_USER_USER_TYPE, user.userType.name)
        prefUtil.setString(Constants.PREF_KEY_ALTER_EGO_ID, user.alterEgoId)
        prefUtil.setString(Constants.PREF_KEY_ALTER_EGO_ACCESS_CODE, user.alterEgoAccessCode)
        prefUtil.setLong(Constants.PREF_KEY_USER_TIME_REGISTERED, user.timeRegistered?.time ?: 0)
        prefUtil.setLong(Constants.PREF_KEY_USER_LAST_UNLOCKED, user.timeLastUnlocked?.time ?: 0)
        SyncProfileJob.schedule()
    }

    /**
     * Get the user with the given email
     * @param email The user email
     * @return LiveData
     */
    fun getUserWithEmail(email: String): LiveData<Resource<User>> {
        Timber.d("Getting user with email: %s", email)
        val query = firestore.collection(COLLECTION_USER)
                .whereEqualTo("email", email)
                .limit(1)

        return FirestoreSingleLiveData(androidUtil, query, User::class.java)
    }

    fun getAdmin(alterEgoId: String, accessCode: String): LiveData<Resource<User>> {
        Timber.d("Getting admin with id: %s and accessCode: %s", alterEgoId, accessCode)
        val query = firestore.collection(COLLECTION_USER)
                .whereEqualTo("alterEgoId", alterEgoId)
                .whereEqualTo("alterEgoAccessCode", accessCode)
                .limit(1)

        return FirestoreSingleLiveData(androidUtil, query, User::class.java)
    }

    /**
     * Get the user with the given nickname
     * @param nickname The user nickname
     * @return LiveData
     */
    fun getUserWithNickname(nickname: String): LiveData<Resource<User>> {
        Timber.d("Getting user with nickname: %s", nickname)
        val query = firestore.collection(COLLECTION_USER)
                .whereEqualTo("nickname", nickname)
                .limit(1)

        return FirestoreSingleLiveData(androidUtil, query, User::class.java)
    }

    /**
     * Get the user with the given id
     * @param userId The user id
     * @return LiveData
     */
    fun getUserWithId(userId: String): LiveData<Resource<User>> {
        Timber.d("Getting user with id: %s", userId)
        val query = firestore.collection(COLLECTION_USER)
                .whereEqualTo("userId", userId)
                .limit(1)

        return FirestoreSingleLiveData(androidUtil, query, User::class.java)
    }

    /**
     * Add the given user into the database
     * @param user The user to add
     * @return LiveData
     */
    fun addUser(user: User): LiveData<Resource<User>> {
        Timber.d("Adding new user: %s", user)
        val addUserRequestLiveData = MutableLiveData<Resource<User>>()

        // Set resource into loading state
        addUserRequestLiveData.value = Resource.loading(
                androidUtil
                        .getString(R.string.new_user_saving)
        )

        // Set the user document id
        val newUserRef = firestore.collection(COLLECTION_USER).document(user.userId!!)

        // Save user into the Firestore database
        newUserRef.set(user)
                .addOnSuccessListener { documentReference ->
                    Timber.d("User successfully added: %s", documentReference)
                    addUserRequestLiveData.value = Resource.success(user)
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error saving new user")
                    addUserRequestLiveData.value = Resource.error(
                            androidUtil
                                    .getString(R.string.new_user_save_error)
                    )
                }

        return addUserRequestLiveData
    }

    /**
     * Updates the given user in the database
     * @param user The user to update
     * @return LiveData
     */
    fun updateUser(user: User): LiveData<Resource<AsyncRequest>> {
        Timber.d("Updating user: %s", user)
        val addUserRequestLiveData = MutableLiveData<Resource<AsyncRequest>>()

        // Set resource into loading state
        addUserRequestLiveData.value = Resource.loading(
                androidUtil
                        .getString(R.string.user_updating)
        )

        // Get a reference to the user to update
        val userToUpdateRef = firestore.collection(COLLECTION_USER).document(user.userId!!)

        // Save user into the Firestore database
        userToUpdateRef.set(user)
                .addOnSuccessListener { documentReference ->
                    Timber.d("User successfully updated: %s", documentReference)
                    setLoggedInUser(user)
                    addUserRequestLiveData.value = Resource.success(AsyncRequest())
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error updating user")
                    addUserRequestLiveData.value = Resource.error(
                            androidUtil
                                    .getString(R.string.user_update_error)
                    )
                }

        return addUserRequestLiveData
    }

    fun getUSerId(): String? {
        return prefUtil.getString(Constants.PREF_KEY_USER_ID, "")
    }

    fun getUserType(): User.UserType {
        return User.UserType.valueOf(
                prefUtil.getString(
                        Constants.PREF_KEY_USER_USER_TYPE,
                        User.UserType.REGULAR.name
                )!!)

    }

}