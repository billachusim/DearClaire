package com.mobymagic.clairediary.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.util.PrefUtil
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.SplashResult
import com.mobymagic.clairediary.vo.User
import timber.log.Timber
import java.util.concurrent.TimeUnit

var SPLASH_LAST_UNLOCKED_TIME: Long = 0

class SplashViewModel(
    private val androidUtil: AndroidUtil,
    private val appExecutors: AppExecutors,
    private val prefUtil: PrefUtil,
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    fun getSplashResult(justUnlocked: Boolean): LiveData<Resource<SplashResult>> {
        val splashLiveData: MutableLiveData<Resource<SplashResult>> = MutableLiveData()

        // Set resource into loading state
        splashLiveData.value =
            Resource.loading(androidUtil.getString(R.string.splash_loading_message))

        // Use the scheduled thread to create an artificial delay for the splash screen
        appExecutors.scheduledThread().schedule(
            {
                Timber.d("Artificial splash screen delay is up")
                val curTime = System.currentTimeMillis()

                if (justUnlocked) {
                    Timber.d("Just unlocked app, saving current time: %d", curTime)
                    SPLASH_LAST_UNLOCKED_TIME = curTime
                }

                val user = userRepository.getLoggedInUser()
                val tenMinutes = TimeUnit.MINUTES.toMillis(10)
                val curTimeMinusLastUnlockedTime = curTime - SPLASH_LAST_UNLOCKED_TIME
                val firebaseUser = firebaseAuth.currentUser

                Timber.d("User: %s, FirebaseUser: %s", user, firebaseUser)
                Timber.d("Time diff: %s, ten minutes: %s", curTimeMinusLastUnlockedTime, tenMinutes)
                // Check if the user is logged in
                if (user == null || firebaseUser == null) {
                    Timber.d("User is not logged in, open onboarding screen")
                    splashLiveData.postValue(getSplashResource(SplashResult.SplashAction.OPEN_ONBOARDING))
                } else if (user.nickname.isEmpty()) {
                    Timber.d("User doesn't have a profile yet, open create profile screen")
                    splashLiveData.postValue(getSplashResource(SplashResult.SplashAction.OPEN_CREATE_PROFILE))
                } else if (curTimeMinusLastUnlockedTime in 0 until tenMinutes) {
                    Timber.d("User has recently unlocked, open sessions home")
                    splashLiveData.postValue(
                        getSuccessResource(
                            user,
                            SplashResult.SplashAction.OPEN_SESSIONS_HOME
                        )
                    )
                } else {
                    // when the user is already logged in just open the Session home
                    Timber.d("User is logged in, open lock screen")
                    splashLiveData.postValue(
                        getSuccessResource(
                            user,
                            SplashResult.SplashAction.OPEN_SESSIONS_HOME
                        )
                    )
                }
            },
            2,
            TimeUnit.SECONDS
        )

        return splashLiveData
    }

    private fun getSplashResource(splashAction: SplashResult.SplashAction): Resource<SplashResult> {
        return Resource.success(SplashResult(action = splashAction))
    }

    private fun getSuccessResource(
        user: User,
        splashAction: SplashResult.SplashAction
    ): Resource<SplashResult> {
        return Resource.success(
            SplashResult(
                user.userId,
                user.userType,
                user.secretCode,
                splashAction
            )
        )
    }

    fun simulateSplashScreen(): LiveData<Resource<SplashResult>> {
        val splashLiveData: MutableLiveData<Resource<SplashResult>> = MutableLiveData()

        // Set resource into loading state
        splashLiveData.value =
            Resource.loading(androidUtil.getString(R.string.splash_loading_message))

        // Use the scheduled thread to create an artificial delay for the splash screen
        appExecutors.scheduledThread().schedule(
            {
                splashLiveData.postValue(getSplashResource(SplashResult.SplashAction.OPEN_SESSIONS_HOME))
            },
            1,
            TimeUnit.SECONDS
        )

        return splashLiveData
    }

}