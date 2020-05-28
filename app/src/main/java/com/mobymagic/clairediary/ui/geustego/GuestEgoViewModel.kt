package com.mobymagic.clairediary.ui.geustego

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.repository.SessionRepository
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.ui.sessionlist.SessionListType
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.util.PrefUtil
import com.mobymagic.clairediary.vo.Alert
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Session
import com.mobymagic.clairediary.vo.User
import timber.log.Timber

class GuestEgoViewModel(
        private val androidUtil: AndroidUtil,
        private val sessionRepository: SessionRepository,
        private val prefUtil: PrefUtil,
        private val userRepository: UserRepository) : ViewModel() {

    private val sessionRequestLiveData: MutableLiveData<SessionRequest> = MutableLiveData()
    private val sessionListLiveData: LiveData<Resource<List<Session>>>
    private val alertSessionTypeLiveData: MutableLiveData<SessionListType> = MutableLiveData()
    private val sessionAlertLiveData: LiveData<Resource<Alert>>
    lateinit var userId: String

    init {
        sessionListLiveData = Transformations.switchMap(sessionRequestLiveData) { sessionRequest ->
            Timber.d("SessionRequest: %s", sessionRequest)
            getSessionListLiveData(sessionRequest)
        }

        sessionAlertLiveData =
                Transformations.switchMap(alertSessionTypeLiveData) { sessionListType ->
                    val alertLiveData: MutableLiveData<Resource<Alert>> = MutableLiveData()

                    // Set resource into loading state
                    alertLiveData.value =
                            Resource.loading(androidUtil.getString(R.string.common_message_loading))

                    alertLiveData.value = Resource.success(null)

                    alertLiveData
                }
    }

    fun setSessionRequest(sessionListType: SessionListType, userId: String, lastSession: Session?) {
        Timber.d(
                "Loading sessions. UserId: %s, SessionListType: %s, LastSession: %s",
                userId, sessionListType, lastSession
        )
        val newSessionRequest = SessionRequest(userId, sessionListType, lastSession)

        sessionRequestLiveData.value = newSessionRequest
        alertSessionTypeLiveData.value = sessionListType
    }

    private fun getAlertKey(sessionListType: SessionListType): String {
        return sessionListType.name + "_has_alert"
    }

    /**
     * Get a LiveData to use to listen for a list of requested sessions
     */
    fun getSessions(): LiveData<Resource<List<Session>>> = sessionListLiveData

    /**
     * Retries loading a list of session using the SessionRequest
     */
    fun retry() {
        Timber.d("Retrying session list load")
        sessionRequestLiveData.value = sessionRequestLiveData.value
        alertSessionTypeLiveData.value = alertSessionTypeLiveData.value
    }

    fun getAlerts(): LiveData<Resource<Alert>> = sessionAlertLiveData

    fun cancelAlert() {
        val alertKey = getAlertKey(sessionRequestLiveData.value!!.sessionListType)
        prefUtil.setBool(alertKey, false)
        alertSessionTypeLiveData.value = alertSessionTypeLiveData.value
    }

    /**
     * Queries the SessionRepository based on the request and returns a LiveData
     * @param sessionRequest The session request
     * @return LiveData
     */
    private fun getSessionListLiveData(sessionRequest: SessionRequest): LiveData<Resource<List<Session>>> {
        return sessionRepository.getPublicSessions(sessionRequest.lastSession, sessionRequest.userId)
    }

    fun getUser(userId: String): LiveData<Resource<User>> {
        return userRepository.getUserWithId(userId)
    }

    data class SessionRequest(
            val userId: String,
            val sessionListType: SessionListType,
            val lastSession: Session?
    )

}