package com.mobymagic.clairediary.ui.ego

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobymagic.clairediary.repository.CommentRepository
import com.mobymagic.clairediary.repository.SessionRepository
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.util.PrefUtil
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Session
import com.mobymagic.clairediary.vo.Status
import com.mobymagic.clairediary.vo.UserActivity
import com.mobymagic.clairediary.vo.counters.Shard
import com.mobymagic.clairediary.vo.counters.UserCommentCounter
import com.mobymagic.clairediary.vo.counters.UserSessionCounter
import java.util.*

class EgoViewModel(
        private val androidUtil: AndroidUtil,
        private val sessionRepository: SessionRepository,
        private val commentRepository: CommentRepository,
        private val prefUtil: PrefUtil) : ViewModel() {

    lateinit var userId: String
    lateinit var resultingResource: Resource<Int>
    private var userMeeTooCountLiveData: MediatorLiveData<Resource<Int>> = MediatorLiveData()
    private var userFollowCountLiveData: MediatorLiveData<Resource<Int>> = MediatorLiveData()
    private var activitySessionLiveData: MutableLiveData<Resource<Session>> = MutableLiveData()


    fun initializeMediatorLiveData() {

        userFollowCountLiveData.addSource(getNumberOfFollowShardToUser()) { it ->
            when (it?.status) {
                Status.LOADING -> {
                    resultingResource = Resource(Status.LOADING, 0, it.message)
                    userFollowCountLiveData.postValue(resultingResource)
                }

                Status.ERROR -> {
                    resultingResource = Resource(Status.ERROR, 0, it.message)
                    userFollowCountLiveData.postValue(resultingResource)
                }

                Status.SUCCESS -> {
                    // try getting the shard count from the resource and post it to the result live data
                    if (it.data != null && it.data.isNotEmpty()) {
                        resultingResource = Resource(Status.SUCCESS, it.data.sumBy { it.count.toInt() }, it.message)
                        userFollowCountLiveData.postValue(resultingResource)
                    }

                }
            }

        }
    }

    fun getUserSessionCount(): LiveData<Resource<List<UserSessionCounter>>> {
        return sessionRepository.getUserSessionCounter(userId)
    }

    fun getUserBestSession(): LiveData<Resource<List<Session>>> {
        return sessionRepository.getUserBestSession(userId)
    }

    fun retryLoadingBestSession() {
        getUserBestSession()
    }

    fun getUserActivity(): LiveData<Resource<List<UserActivity>>> {
        return sessionRepository.getUserActivity(userId)
    }

    fun getActivityByUser(): LiveData<Resource<List<UserActivity>>> {
        return sessionRepository.getActivityByUser(userId)
    }

    fun retryLoadingUserActivity() {
        getUserActivity()
    }

    fun retryLoadingActivityByUser() {
        getActivityByUser()
    }

    fun getNumberOfCommentsByUser(userId: String): LiveData<Resource<List<UserCommentCounter>>> {
        return commentRepository.getNumberOfCommentsForUser(userId)
    }

    fun retryLoadingNumberOfCommentsByUser() {
        getNumberOfCommentsByUser(userId)
    }

    private fun getNumberOfMeeTooShardToUser(): LiveData<Resource<List<Shard>>> {
        return sessionRepository.getNumberOfMeeToosForUser(userId)
    }


    fun getUserMeeTooCount(): LiveData<Resource<Int>> {
        getNumberOfMeeTooShardToUser()
        return userMeeTooCountLiveData
    }

    fun retryLoadingUserMeeTooCount() {
        getNumberOfMeeTooShardToUser()
    }

    private fun getNumberOfFollowShardToUser(): LiveData<Resource<List<Shard>>> {
        return sessionRepository.getNumberOfFollowaForUser(userId)
    }


    fun getUserFollowCount(): LiveData<Resource<Int>> {
        getNumberOfMeeTooShardToUser()
        return userFollowCountLiveData
    }

    fun retryLoadingUserFollowCount() {
        getNumberOfFollowShardToUser()
    }

    fun getUserSessionsByDate(
            userId: String,
            startDate: Date,
            endDate: Date
    ): LiveData<Resource<List<Session>>> {
        return sessionRepository.getUserSessionsByDate(userId, startDate, endDate)
    }

    fun retryLoadingUserSessionsByDate(userId: String,
                                       startDate: Date,
                                       endDate: Date): LiveData<Resource<List<Session>>> {
        return getUserSessionsByDate(userId, startDate, endDate)
    }

    fun loadSessionById(sessionId: String): LiveData<Resource<Session>> {
        return sessionRepository.getSessionWithId(sessionId)
    }

    fun getactivitySessionLiveData(): LiveData<Resource<Session>> {
        return activitySessionLiveData
    }


}