package com.mobymagic.clairediary.ui.commentlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mobymagic.clairediary.repository.CommentRepository
import com.mobymagic.clairediary.vo.Comment
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Session
import timber.log.Timber

class CommentListViewModel(private val commentRepository: CommentRepository) : ViewModel() {

    private val sessionLiveData: MutableLiveData<Session> = MutableLiveData()
    private val commentListLiveData: LiveData<Resource<List<Comment>>>

    init {
        commentListLiveData = Transformations.switchMap(sessionLiveData, { session ->
            Timber.d("Loading comments for session: %s", session)
            commentRepository.getComments(session, null)
        })
    }

    fun toggleThanks(userId: String, session: Session, comment: Comment): Comment {
        if (comment.thanks.contains(userId)) {
            comment.thanks.remove(userId)
        } else {
            comment.thanks.add(userId)
        }
        comment.numberOfThanks = comment.thanks.size

        commentRepository.updateComment(session, comment)
        return comment
    }

    fun setSession(session: Session) {
        Timber.d("Setting session to: %s", session)
        if (sessionLiveData.value == session) {
            Timber.d("Session matches previous one, skipping...")
            return
        }

        sessionLiveData.value = session
    }

    /**
     * Get a LiveData to use to listen for a list of requested comments
     */
    fun getCommentList(): LiveData<Resource<List<Comment>>> {
        return commentListLiveData
    }

    /**
     * Retries loading a list of comment
     */
    fun retry() {
        Timber.d("Retrying comment list load")
        val session = sessionLiveData.value
        sessionLiveData.value = session
    }

}