package com.mobymagic.clairediary.ui.sessionlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.repository.CommentRepository
import com.mobymagic.clairediary.repository.SessionRepository
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.util.PrefUtil
import com.mobymagic.clairediary.vo.Alert
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Session
import com.mobymagic.clairediary.vo.Status
import com.mobymagic.clairediary.vo.counters.Shard
import timber.log.Timber


class SessionListViewModel(
    private val androidUtil: AndroidUtil,
    private val sessionRepository: SessionRepository,
    private val commentRepository: CommentRepository,
    private val prefUtil: PrefUtil
) : ViewModel() {

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
                val alertKey = getAlertKey(sessionListType)
                val alertLiveData: MutableLiveData<Resource<Alert>> = MutableLiveData()

                // Set resource into loading state
                alertLiveData.value =
                    Resource.loading(androidUtil.getString(R.string.common_message_loading))

                // Check if we have any alert for the session type
                when (sessionListType) {
                    SessionListType.TRENDING -> {
                        val alert =
                            Alert(androidUtil.getString(R.string.session_list_alert_trending))
                        alertLiveData.value = Resource.success(alert)
                    }
                    SessionListType.DIARY -> {
                        val alert =
                            Alert(androidUtil.getString(R.string.session_list_alert_diary))
                        alertLiveData.value = Resource.success(alert)
                    }
                    SessionListType.NON_ASSIGNED -> {
                        val alert =
                            Alert(androidUtil.getString(R.string.session_list_alert_new_alter_ego))
                        alertLiveData.value = Resource.success(alert)
                    }
                    else -> {
                        alertLiveData.value = Resource.success(null)
                    }
                }

                alertLiveData
            }
    }

    fun setSessionRequest(sessionListType: SessionListType, userId: String, lastSession: Session?) {
        Timber.d(
            "Loading sessions. UserId: %s, SessionListType: %s, LastSession: %s",
            userId, sessionListType, lastSession
        )
        val newSessionRequest = SessionRequest(userId, sessionListType, lastSession)

        if (sessionRequestLiveData.value == newSessionRequest) {
            Timber.i("Session request matches previous request, skipping...")
            return
        }

        sessionRequestLiveData.value = newSessionRequest
        alertSessionTypeLiveData.value = sessionListType
    }

    fun getAlerts(): LiveData<Resource<Alert>> = sessionAlertLiveData

    fun cancelAlert() {
        val alertKey = getAlertKey(sessionRequestLiveData.value!!.sessionListType)
        prefUtil.setBool(alertKey, false)
        alertSessionTypeLiveData.value = alertSessionTypeLiveData.value
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

    /**
     * Queries the SessionRepository based on the request and returns a LiveData
     * @param sessionRequest The session request
     * @return LiveData
     */
    private fun getSessionListLiveData(sessionRequest: SessionRequest): LiveData<Resource<List<Session>>> {
        return when (sessionRequest.sessionListType) {
            SessionListType.EGO ->
                sessionRepository.getArchivedSessions(
                    sessionRequest.lastSession,
                    sessionRequest.userId
                )
            SessionListType.TRENDING -> {
                sessionRepository.getFeaturedSessions(sessionRequest.lastSession, userId)
            }
            SessionListType.DIARY ->
                sessionRepository.getDiarySessions(
                    sessionRequest.lastSession,
                    sessionRequest.userId
                )

            SessionListType.FOLLOWING -> {
                sessionRepository.getFollowingSessions(sessionRequest.lastSession, userId)
            }

            SessionListType.NON_ASSIGNED ->
                sessionRepository.getNonAssignedSessions(sessionRequest.lastSession)

            SessionListType.ASSIGNED ->
                sessionRepository.getAssignedSessions(
                    sessionRequest.lastSession,
                    sessionRequest.userId
                )

            SessionListType.FLAGGED ->
                sessionRepository.getFlaggedSessions(sessionRequest.lastSession)

            SessionListType.ALL ->
                sessionRepository.getAllSessions(sessionRequest.lastSession)

        }
    }

    private fun getNumberOfCommentShardForSession(sessionId: String): LiveData<Resource<List<Shard>>> {
        return commentRepository.getNumberOfCommentsForSession(sessionId)
    }

    fun getNumberOfCommentsForSessions(sessionId: String): LiveData<Resource<Int>> {
        return Transformations.map(getNumberOfCommentShardForSession(sessionId)) { shardResource ->
            when (shardResource?.status) {
                Status.LOADING -> {
                    return@map Resource(Status.LOADING, 0, shardResource.message)
                }

                Status.ERROR -> {
                    return@map Resource(Status.ERROR, 0, shardResource.message)
                }

                Status.SUCCESS -> {
                    // try getting the shard count from the resource and post it to the result live data
                    if (shardResource.data != null && shardResource.data.isNotEmpty()) {
                        return@map Resource(
                            Status.SUCCESS,
                            shardResource.data.sumBy { it.count.toInt() },
                            shardResource.message
                        )
                    } else {
                        return@map Resource(Status.ERROR, 0, shardResource.message)
                    }

                }
                else -> {
                    return@map Resource(Status.ERROR, 0, shardResource.message)
                }
            }
        }
    }

    data class SessionRequest(
        val userId: String,
        val sessionListType: SessionListType,
        val lastSession: Session?
    )

}