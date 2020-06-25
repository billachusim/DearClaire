package com.mobymagic.clairediary.ui.sessiondetail

import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.repository.CommentRepository
import com.mobymagic.clairediary.repository.FileRepository
import com.mobymagic.clairediary.repository.SessionRepository
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.vo.*
import timber.log.Timber
import java.io.File

class SessionDetailViewModel(
        private val androidUtil: AndroidUtil,
        private val userRepository: UserRepository,
        private val fileRepository: FileRepository,
        private val commentRepository: CommentRepository,
        private val sessionRepository: SessionRepository
) : ViewModel() {

    private val commentLiveData: MutableLiveData<Comment> = MutableLiveData()
    private val sessionLiveData: MutableLiveData<Session> = MutableLiveData()
    private lateinit var addCommentLiveData: LiveData<Resource<Comment>>
    private lateinit var draftCommentLiveData: LiveData<Comment>
    private lateinit var draftCommentObserver: Observer<Comment>
    private var submitted = false
    private var fromAlterEgo = false
    private var editingComment = false
    private var editingSession = false

    init {
        // Use a default empty comment
        val comment = Comment()
        commentLiveData.value = comment

        // Then look for a draft session
        loadDraftComment()
        initAddCommentLiveData()
    }

    fun editComment(comment: Comment) {
        editingComment = true
        commentLiveData.value = comment
    }

    fun editSession(session: Session) {
        editingSession = true
        sessionLiveData.value = session
    }

    fun toggleMeToo(userId: String, session: Session): Session {
        var shouldIncreaseMeTooCount = true
        if (session.meToos!!.contains(userId)) {
            session.meToos!!.remove(userId)
            shouldIncreaseMeTooCount = false
        } else {
            session.meToos!!.add(userId)
        }

        sessionRepository.updateSession(session, userId, UserActivityType.MEETOO,
                shouldIncrementMeeToo = shouldIncreaseMeTooCount, fromAlterEgo = fromAlterEgo)
        return session
    }

    fun toggleFollowers(userId: String, session: Session): Session {
        val shouldIncrementFollow: Boolean
        if (session.followers.contains(userId)) {
            session.followers.remove(userId)
            shouldIncrementFollow = false
            Answers.getInstance().logCustom(CustomEvent("Session Unfollowed"))
        } else {
            session.followers.add(userId)
            shouldIncrementFollow = true
            Answers.getInstance().logCustom(CustomEvent("Session Followed"))
        }

        sessionRepository.updateSession(session, userId, UserActivityType.FOLLOW,
                shouldIncrementFollow = shouldIncrementFollow, fromAlterEgo = fromAlterEgo)
        return session
    }

    private fun initAddCommentLiveData() {
        addCommentLiveData = Transformations.switchMap(sessionLiveData) { session ->
            val comment = commentLiveData.value!!
            val user = userRepository.getLoggedInUser()
            when {
                user == null -> {
                    // This shouldn't happen, but if for some reason logged in user is null, handle it
                    val errorLiveData = MutableLiveData<Resource<Comment>>()
                    errorLiveData.value =
                            Resource.error(androidUtil.getString(R.string.common_error_login_again))
                    errorLiveData
                }
                isInputValid(comment) -> {
                    submitComment(session, comment, user, fromAlterEgo)
                }
                else -> {
                    // Show input error
                    val errorLiveData = MutableLiveData<Resource<Comment>>()
                    errorLiveData.value = Resource.error(getInputError(comment))
                    errorLiveData
                }
            }
        }
    }

    private fun submitComment(
            session: Session,
            comment: Comment,
            user: User,
            fromAlterEgo: Boolean
    ): LiveData<Resource<Comment>> {
        // Put in user details
        comment.userNickname = user.nickname
        comment.userAvatarUrl = user.avatarUrl!!
        comment.userId = user.userId!!
        comment.isUserAdmin = User.UserType.isAdmin(user.userType) && fromAlterEgo
        comment.alterEgoId = user.alterEgoId

        val addSessionLiveData = MediatorLiveData<Resource<Comment>>()
        val fileWrappers = getFileWrappers(comment)

        if (fileWrappers.isNotEmpty()) {
            uploadFiles(addSessionLiveData, fileWrappers, session, comment)
        } else {
            saveComment(addSessionLiveData, session, comment)
        }

        return addSessionLiveData
    }

    private fun saveComment(
            addCommentLiveData: MediatorLiveData<Resource<Comment>>,
            session: Session,
            comment: Comment
    ) {
        if (editingComment) {
            val updateCommentLiveData = commentRepository.updateComment(session, comment)
            addCommentLiveData.addSource(updateCommentLiveData) { commentResource ->
                Timber.d("Comment resource: %s", commentResource)
                addCommentLiveData.value = commentResource

                if (commentResource?.status == Status.SUCCESS) {
                    addCommentLiveData.removeSource(updateCommentLiveData)
                    commentRepository.clearDraftComment()
                    submitted = true
                    editingComment = false
                    commentLiveData.value = Comment()
                    Answers.getInstance().logCustom(CustomEvent("Comment Updated"))
                } else if (commentResource?.status == Status.ERROR) {
                    addCommentLiveData.removeSource(updateCommentLiveData)
                }
            }
        } else {
            val saveCommentLiveData = commentRepository.addComment(session, comment)
            addCommentLiveData.addSource(saveCommentLiveData) { commentResource ->
                Timber.d("Comment resource: %s", commentResource)
                addCommentLiveData.value = commentResource

                if (commentResource?.status == Status.SUCCESS) {
                    addCommentLiveData.removeSource(saveCommentLiveData)
                    commentRepository.clearDraftComment()
                    submitted = true
                    commentLiveData.value = Comment()
                    Answers.getInstance().logCustom(CustomEvent("Comment Added"))
                } else if (commentResource?.status == Status.ERROR) {
                    addCommentLiveData.removeSource(saveCommentLiveData)
                }
            }
        }
    }

    private fun uploadFiles(
            addCommentLiveData: MediatorLiveData<Resource<Comment>>,
            fileWrappers: List<FileRepository.FileWrapper>,
            session: Session,
            comment: Comment
    ) {
        val uploadLiveData = fileRepository.uploadFiles(fileWrappers)
        addCommentLiveData.addSource(uploadLiveData) { filesResource ->
            Timber.d("File resource: %s", filesResource)
            when (filesResource?.status) {
                Status.LOADING -> {
                    addCommentLiveData.value = Resource.loading(filesResource.message)
                }
                Status.ERROR -> {
                    addCommentLiveData.removeSource(uploadLiveData)
                    addCommentLiveData.value = Resource.error(filesResource.message)
                }
                Status.SUCCESS -> {
                    addCommentLiveData.removeSource(uploadLiveData)
                    comment.imageUrls.clear()
                    for (fileWrapper in filesResource.data!!) {
                        if (fileWrapper.fileType == FileRepository.FileType.PHOTO) {
                            comment.imageUrls.add(fileWrapper.uploadUrl.toString())
                        } else if (fileWrapper.fileType == FileRepository.FileType.AUDIO) {
                            comment.audioUrl = fileWrapper.uploadUrl.toString()
                        }
                    }
                    saveComment(addCommentLiveData, session, comment)
                }
            }
        }
    }

    private fun getFileWrappers(comment: Comment): List<FileRepository.FileWrapper> {
        val wrapperList = mutableListOf<FileRepository.FileWrapper>()
        if (comment.audioUrl != null) {
            wrapperList.add(
                    FileRepository.FileWrapper(
                            File(comment.audioUrl),
                            FileRepository.FileType.AUDIO
                    )
            )
        }

        for (imageUrl in comment.imageUrls) {
            wrapperList.add(
                    FileRepository.FileWrapper(
                            File(imageUrl),
                            FileRepository.FileType.PHOTO
                    )
            )
        }

        return wrapperList
    }

    private fun isInputValid(comment: Comment): Boolean {
        if (comment.message.isEmpty() && comment.audioUrl == null)
            return false

        return true
    }

    private fun getInputError(comment: Comment): String? {
        return if (comment.message.isEmpty() && comment.audioUrl == null)
            androidUtil.getString(R.string.session_detail_error_message_or_audio_empty)
        else
            null
    }

    private fun loadDraftComment() {
        draftCommentLiveData = commentRepository.getDraftComment()
        draftCommentObserver = Observer { session ->
            Timber.d("Draft comment gotten: %s", session)
            draftCommentLiveData.removeObserver(draftCommentObserver)
            val curSession = commentLiveData.value!!
            if (session == null) {
                Timber.d("No draft comment")
                return@Observer
            }

            // Copy any non empty value from the current session into the draft session
            if (curSession.message.isNotEmpty()) {
                session.message = curSession.message
            }
            if (curSession.imageUrls.isNotEmpty()) {
                session.imageUrls = curSession.imageUrls
            }
            if (curSession.audioUrl != null) {
                session.audioUrl = curSession.audioUrl
            }

            commentLiveData.value = session
        }

        draftCommentLiveData.observeForever(draftCommentObserver)
    }

    override fun onCleared() {
        Timber.d("ViewModel cleared, removing callbacks")
        draftCommentLiveData.removeObserver(draftCommentObserver)
    }

    fun submitComment(session: Session, userId: String, fromAlterEgo: Boolean) {
        this.fromAlterEgo = fromAlterEgo
        if (fromAlterEgo) {
            session.respondentUserId = userId
        }
        session.timeLastActivity = null
        sessionRepository.updateSession(session, userId, UserActivityType.COMMENT, fromAlterEgo = fromAlterEgo)
        sessionLiveData.value = session
    }

    fun setMessage(message: String) {
        Timber.d("Setting message: %s", message)
        val comment = commentLiveData.value!!
        comment.message = message
        commentLiveData.value = comment
    }

    fun addPhotos(photoUris: List<String>) {
        Timber.d("Adding photos to session: %s", photoUris)
        val comment = commentLiveData.value!!
        comment.imageUrls.addAll(photoUris)
        commentLiveData.value = comment
    }

    fun removePhoto(photoUri: String) {
        Timber.d("Removing photo: %s", photoUri)
        val comment = commentLiveData.value!!
        comment.imageUrls.remove(photoUri)
        commentLiveData.value = comment
    }

    fun setAudioNote(audioUri: String) {
        Timber.d("Setting audio note uri: %s", audioUri)
        val comment = commentLiveData.value!!
        comment.audioUrl = audioUri
        commentLiveData.value = comment
    }

    fun removeAudioNote() {
        Timber.d("Removing audio note")
        val comment = commentLiveData.value!!
        comment.audioUrl = null
        commentLiveData.value = comment
    }

    fun retry() {
        sessionLiveData.value = sessionLiveData.value
    }

    fun saveDraft() {
        Timber.d("Saving draft")
        if (!submitted) {
            commentRepository.saveDraftComment(commentLiveData.value!!)
        }
    }

    fun getDraftComment(): LiveData<Comment> = commentLiveData

    fun getSubmitStatus(): LiveData<Resource<Comment>> = addCommentLiveData

    fun setFromalterEgo(fromAlterEgo: Boolean) {
        this.fromAlterEgo = fromAlterEgo
    }

}