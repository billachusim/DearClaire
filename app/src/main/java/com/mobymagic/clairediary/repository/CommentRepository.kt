package com.mobymagic.clairediary.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.util.FirestoreListLiveData
import com.mobymagic.clairediary.util.PrefUtil
import com.mobymagic.clairediary.vo.Comment
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Session
import com.mobymagic.clairediary.vo.counters.Shard
import com.mobymagic.clairediary.vo.counters.UserCommentCounter
import timber.log.Timber

private const val PREF_KEY_DRAFT_COMMENT = "PREF_KEY_DRAFT_COMMENT"
private const val COLLECTION_COMMENTS = "comments"
private const val COLLECTION_USER_COMMENT_COUNT = "user_comment_counters"
const val LIMIT_COMMENTS_RESULT = 300L
const val SESSION_COMMENT_COUNTER = "session_comment_counter"
const val COLLECTION_SESSION_COMMENT_SHARDS = "session_comment_shards"

class CommentRepository(
        private val appExecutors: AppExecutors,
        private val androidUtil: AndroidUtil,
        private val prefUtil: PrefUtil,
        private val gson: Gson,
        private val firestore: FirebaseFirestore
) {

    fun getDraftComment(): LiveData<Comment> {
        Timber.d("Getting draft comment")
        val commentLiveData: MutableLiveData<Comment> = MutableLiveData()

        appExecutors.diskIO().execute {
            val draftCommentJson = prefUtil.getString(PREF_KEY_DRAFT_COMMENT, null)

            if (draftCommentJson != null) {
                Timber.d("Got draft comment, serializing")
                commentLiveData.postValue(gson.fromJson(draftCommentJson, Comment::class.java))
            } else {
                Timber.d("No draft comment")
                commentLiveData.postValue(null)
            }
        }

        return commentLiveData
    }

    fun saveDraftComment(comment: Comment) {
        Timber.d("Saving draft comment: %s", comment)
        appExecutors.diskIO().execute {
            val commentJson = gson.toJson(comment)
            prefUtil.setString(PREF_KEY_DRAFT_COMMENT, commentJson)
            Timber.d("Draft comment saved")
        }
    }

    fun clearDraftComment() {
        Timber.d("Clearing draft comment")
        appExecutors.diskIO().execute {
            prefUtil.setString(PREF_KEY_DRAFT_COMMENT, null)
        }
    }

    /**
     * Get the comments for a given session
     * @param session The session to fetch comments for
     * @param lastComment The last comment from previous request. Used for pagination
     * @return LiveData
     */
    fun getComments(session: Session, lastComment: Comment?): LiveData<Resource<List<Comment>>> {
        Timber.d("Getting comments for session: %s. Last comment: %s", session, lastComment)
        val query = firestore.collection(COLLECTION_SESSIONS).document(session.sessionId.toString())
                .collection(COLLECTION_COMMENTS).whereEqualTo("flagged", false)
                .orderBy("timeCreated", Query.Direction.ASCENDING).limit(LIMIT_COMMENTS_RESULT)

        if (lastComment != null) query.startAfter(lastComment.timeCreated)

        return FirestoreListLiveData(androidUtil, query, Comment::class.java, null)
    }


    /**
     * Adds the given comment into the database
     * @param session The session to add the comment to
     * @param comment The new comment to add into the database
     * @return LiveData
     */
    fun addComment(session: Session, comment: Comment): LiveData<Resource<Comment>> {
        Timber.d("Adding comment: %s to session: %s", comment, session)
        val addCommentRequestLiveData = MutableLiveData<Resource<Comment>>()

        // Set resource into loading state
        addCommentRequestLiveData.value = Resource.loading(
                androidUtil
                        .getString(R.string.new_comment_saving)
        )

        // Set the comment document id
        val newCommentRef = firestore.collection(COLLECTION_SESSIONS).document(session.sessionId.toString())
                .collection(COLLECTION_COMMENTS).document()
        comment.commentId = newCommentRef.id
        comment.flagged = false
        Timber.d("Adding comment: %s", comment)

        // Save comment into the Firestore database
        newCommentRef.set(comment)
                .addOnSuccessListener { documentReference ->
                    Timber.d("Comment successfully added: %s", documentReference)
                    addCommentRequestLiveData.value = Resource.success(comment)
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error saving new comment")
                    addCommentRequestLiveData.value = Resource.error(
                            androidUtil
                                    .getString(R.string.new_comment_save_error)
                    )
                }

        return addCommentRequestLiveData
    }

    /**
     * Updates the given comment in the database
     * @param session The session to add the comment to
     * @param comment The new comment to add into the database
     * @return LiveData
     */
    fun updateComment(session: Session, comment: Comment): LiveData<Resource<Comment>> {
        Timber.d("Updating comment: %s for session: %s", comment, session)
        val updateCommentRequestLiveData = MutableLiveData<Resource<Comment>>()

        // Set resource into loading state
        updateCommentRequestLiveData.value = Resource.loading(
                androidUtil
                        .getString(R.string.comment_updating)
        )

        // Get a reference to the comment to update
        val commentToUpdateRef =
                firestore.collection(COLLECTION_SESSIONS).document(session.sessionId.toString())
                        .collection(COLLECTION_COMMENTS).document(comment.commentId)

        // Save comment into the Firestore database
        commentToUpdateRef.set(comment, SetOptions.merge())
                .addOnSuccessListener { documentReference ->
                    Timber.d("Comment successfully updated: %s", documentReference)
                    updateCommentRequestLiveData.value = Resource.success(comment)
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error updating comment")
                    updateCommentRequestLiveData.value = Resource.error(
                            androidUtil
                                    .getString(R.string.comment_update_error)
                    )
                }

        return updateCommentRequestLiveData
    }

    /**
     * Get the number Of comments by a user
     * @param userId The id of the user
     * @return LiveData
     */
    fun getNumberOfCommentsForUser(userId: String): LiveData<Resource<List<UserCommentCounter>>> {
        Timber.d("Getting number of comments made for user: %s", userId)
        val query = firestore.collection(COLLECTION_USER_COMMENT_COUNT)
                .whereEqualTo("userId", userId).limit(1)
        return FirestoreListLiveData(androidUtil, query, UserCommentCounter::class.java, null)
    }

    fun getNumberOfCommentsForSession(sessionId: String): LiveData<Resource<List<Shard>>> {
        val query = firestore.collection(SESSION_COMMENT_COUNTER)
                .document(sessionId).collection(COLLECTION_SESSION_COMMENT_SHARDS).limit(1000)
        return FirestoreListLiveData(androidUtil, query, Shard::class.java, null)
    }

}