package com.mobymagic.clairediary.ui.createsession

import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.mobymagic.clairediary.Constants.DIARY_COLORS
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.repository.FileRepository
import com.mobymagic.clairediary.repository.SessionRepository
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.util.Mood
import com.mobymagic.clairediary.vo.*
import timber.log.Timber
import java.io.File
import java.util.*

class CreateSessionViewModel(
        private val androidUtil: AndroidUtil,
        private val userRepository: UserRepository,
        private val fileRepository: FileRepository,
        private val sessionRepository: SessionRepository
) : ViewModel() {

    private val sessionLiveData: MutableLiveData<Session> = MutableLiveData()
    private val submitSessionLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private lateinit var addSessionLiveData: LiveData<Resource<Session>>
    private lateinit var draftSessionLiveData: LiveData<Session>
    private lateinit var draftSessionObserver: Observer<Session>
    private var submitted = false
    private var updating = false

    init {
        // Use a default empty session with random color at first
        val session = Session()
        session.colorHex = getRandomColor()
        sessionLiveData.value = session

        initAddSessionLiveData()
    }

    fun initSession(session: Session?) {
        if (session == null) {
            loadDraftSession()
            updating = false
        } else {
            sessionLiveData.value = session
            updating = true
        }
    }

    private fun getRandomColor(): String {
        return DIARY_COLORS[Random().nextInt(DIARY_COLORS.size)]
    }

    fun isUpdate(): Boolean {
        return updating
    }

    private fun initAddSessionLiveData() {
        addSessionLiveData = Transformations.switchMap(submitSessionLiveData, {
            val session = sessionLiveData.value!!
            val user = userRepository.getLoggedInUser()
            when {
                user == null -> {
                    // This shouldn't happen, but if for some reason logged in user is null, handle it
                    val errorLiveData = MutableLiveData<Resource<Session>>()
                    errorLiveData.value =
                            Resource.error(androidUtil.getString(R.string.common_error_login_again))
                    errorLiveData
                }
                isInputValid(session) -> {
                    submitSession(session, user)
                }
                else -> {
                    // Show input error
                    val errorLiveData = MutableLiveData<Resource<Session>>()
                    errorLiveData.value = Resource.error(getInputError(session))
                    errorLiveData
                }
            }
        })
    }

    private fun submitSession(session: Session, user: User): LiveData<Resource<Session>> {
        // Put in user details
        session.userNickname = user.nickname
        session.userAvatarUrl = user.avatarUrl!!
        session.userId = user.userId!!

        val addSessionLiveData = MediatorLiveData<Resource<Session>>()
        val fileWrappers = getFileWrappers(session)

        if (fileWrappers.isNotEmpty()) {
            uploadFiles(addSessionLiveData, fileWrappers, session)
        } else {
            saveSession(addSessionLiveData, session)
        }

        return addSessionLiveData
    }

    private fun saveSession(
            addSessionLiveData: MediatorLiveData<Resource<Session>>,
            session: Session
    ) {
        if (updating) {
            val updateSessionLiveData = sessionRepository.updateSession(session)
            addSessionLiveData.addSource(updateSessionLiveData) { sessionResource ->
                Timber.d("Session resource: %s", sessionResource)
                addSessionLiveData.value = sessionResource

                if (sessionResource?.status == Status.SUCCESS) {
                    addSessionLiveData.removeSource(updateSessionLiveData)
                    sessionRepository.clearDraftSession()
                    submitted = true
                    Answers.getInstance().logCustom(CustomEvent("Session Updated"))
                } else if (sessionResource?.status == Status.ERROR) {
                    addSessionLiveData.removeSource(updateSessionLiveData)
                }
            }
        } else {
            val saveSessionLiveData = sessionRepository.addSession(session)
            addSessionLiveData.addSource(saveSessionLiveData) { sessionResource ->
                Timber.d("Session resource: %s", sessionResource)
                addSessionLiveData.value = sessionResource

                if (sessionResource?.status == Status.SUCCESS) {
                    addSessionLiveData.removeSource(saveSessionLiveData)
                    sessionRepository.clearDraftSession()
                    submitted = true
                    Answers.getInstance().logCustom(CustomEvent("Session Created"))
                } else if (sessionResource?.status == Status.ERROR) {
                    addSessionLiveData.removeSource(saveSessionLiveData)
                }
            }
        }
    }

    private fun uploadFiles(
            addSessionLiveData: MediatorLiveData<Resource<Session>>,
            fileWrappers: List<FileRepository.FileWrapper>,
            session: Session
    ) {
        val uploadLiveData = fileRepository.uploadFiles(fileWrappers)
        addSessionLiveData.addSource(uploadLiveData) { filesResource ->
            Timber.d("File resource: %s", filesResource)
            when {
                filesResource?.status == Status.LOADING -> {
                    addSessionLiveData.value = Resource.loading(filesResource.message)
                }
                filesResource?.status == Status.ERROR -> {
                    addSessionLiveData.removeSource(uploadLiveData)
                    addSessionLiveData.value = Resource.error(filesResource.message)
                }
                filesResource?.status == Status.SUCCESS -> {
                    addSessionLiveData.removeSource(uploadLiveData)
                    session.imageUrls?.clear()
                    for (fileWrapper in filesResource.data!!) {
                        if (fileWrapper.fileType == FileRepository.FileType.PHOTO) {
                            session.imageUrls?.add(fileWrapper.uploadUrl.toString())
                        } else if (fileWrapper.fileType == FileRepository.FileType.AUDIO) {
                            session.audioUrl = fileWrapper.uploadUrl.toString()
                        }
                    }
                    saveSession(addSessionLiveData, session)
                }
            }
        }
    }

    private fun getFileWrappers(session: Session): List<FileRepository.FileWrapper> {
        val wrapperList = mutableListOf<FileRepository.FileWrapper>()
        if (session.audioUrl != null) {
            wrapperList.add(
                    FileRepository.FileWrapper(
                            File(session.audioUrl),
                            FileRepository.FileType.AUDIO
                    )
            )
        }

        for (imageUrl in session.imageUrls!!) {
            wrapperList.add(
                    FileRepository.FileWrapper(
                            File(imageUrl),
                            FileRepository.FileType.PHOTO
                    )
            )
        }

        return wrapperList
    }

    private fun isInputValid(session: Session): Boolean {
        if (session.title?.isEmpty()!!)
            return false
        else if (session.message!!.isEmpty() && session.audioUrl == null)
            return false
        /*else if(session.message.length < 100 && session.audioUrl == null)
            return false*/

        return true
    }

    private fun getInputError(session: Session): String? {
        return if (session.title!!.isEmpty())
            androidUtil.getString(R.string.create_session_error_title_empty)
        else if (session.message!!.isEmpty() && session.audioUrl == null)
            androidUtil.getString(R.string.create_session_error_message_or_audio_empty)
        else if (session.message!!.length < 100 && session.audioUrl == null)
            androidUtil.getString(R.string.create_session_error_message_too_short)
        else
            null
    }

    private fun loadDraftSession() {
        draftSessionLiveData = sessionRepository.getDraftSession()
        draftSessionObserver = Observer { session ->
            Timber.d("Draft session gotten: %s", session)
            draftSessionLiveData.removeObserver(draftSessionObserver)
            val curSession = sessionLiveData.value!!
            if (session == null) {
                Timber.d("No draft session")
                return@Observer
            }

            // Copy any non empty value from the current session into the draft session
            session.colorHex = curSession.colorHex
            if (curSession.message!!.isNotEmpty()) {
                session.message = curSession.message
            }
            if (curSession.title!!.isNotEmpty()) {
                session.title = curSession.title
            }
            if (curSession.imageUrls!!.isNotEmpty()) {
                session.imageUrls = curSession.imageUrls
            }
            if (curSession.audioUrl != null) {
                session.audioUrl = curSession.audioUrl
            }
            if (curSession.private) {
                session.private = curSession.private
            }

            sessionLiveData.value = session
        }

        draftSessionLiveData.observeForever(draftSessionObserver)
    }

    override fun onCleared() {
        Timber.d("ViewModel cleared, removing callbacks")
        draftSessionLiveData.removeObserver(draftSessionObserver)
    }

    fun toggleBackground() {
        Timber.d("Toggling background color")
        val session = sessionLiveData.value!!
        session.colorHex = getRandomColor()
        sessionLiveData.value = session
    }

    fun setFont(font: Font) {
        Timber.d("Setting font to: %s", font)
        val session = sessionLiveData.value!!
        session.font = font.fontName
        sessionLiveData.value = session
    }

    fun setPrivacyMode(isPrivate: Boolean) {
        Timber.d("Setting privacy mode: %s", isPrivate)
        val session = sessionLiveData.value!!
        session.private = isPrivate
        sessionLiveData.value = session
    }

    fun setMode(mood: Mood) {
        Timber.d("Setting mood: %s", mood)
        val session = sessionLiveData.value!!
        session.moodId = mood.id
        sessionLiveData.value = session
    }

    fun setRepliesEnabled(repliesEnabled: Boolean) {
        Timber.d("Setting replies enabled: %s", repliesEnabled)
        val session = sessionLiveData.value!!
        session.repliesEnabled = repliesEnabled
        sessionLiveData.value = session
    }

    fun setTitle(title: String) {
        Timber.d("Setting title: %s", title)
        val session = sessionLiveData.value!!
        session.title = title
        sessionLiveData.value = session
    }

    fun setMessage(message: String) {
        Timber.d("Setting message: %s", message)
        val session = sessionLiveData.value!!
        session.message = message
        sessionLiveData.value = session
    }

    fun addPhotos(photoUris: List<String>) {
        Timber.d("Adding photos to session: %s", photoUris)
        val session = sessionLiveData.value!!
        session.imageUrls?.addAll(photoUris)
        sessionLiveData.value = session
    }

    fun removePhoto(photoUri: String) {
        Timber.d("Removing photo: %s", photoUri)
        val session = sessionLiveData.value!!
        session.imageUrls?.remove(photoUri)
        sessionLiveData.value = session
    }

    fun setAudioNote(audioUri: String) {
        Timber.d("Setting audio note uri: %s", audioUri)
        val session = sessionLiveData.value!!
        session.audioUrl = audioUri
        sessionLiveData.value = session
    }

    fun removeAudioNote() {
        Timber.d("Removing audio note")
        val session = sessionLiveData.value!!
        session.audioUrl = null
        sessionLiveData.value = session
    }

    fun submitSession() {
        submitSessionLiveData.value = true
    }

    fun retry() {
        submitSessionLiveData.value = true
    }

    fun saveDraft() {
        if (!submitted) {
            Timber.d("Saving draft")
            sessionRepository.saveDraftSession(sessionLiveData.value!!)
        }
    }

    fun getDraftSession(): LiveData<Session> = sessionLiveData

    fun getSubmitStatus(): LiveData<Resource<Session>> = addSessionLiveData

}