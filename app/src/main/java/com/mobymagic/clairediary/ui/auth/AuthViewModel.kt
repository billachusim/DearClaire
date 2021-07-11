package com.mobymagic.clairediary.ui.auth

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.MainActivity
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.ui.common.NavFragment
import com.mobymagic.clairediary.ui.createprofile.CreateProfileFragment
import com.mobymagic.clairediary.ui.lockscreen.LockScreenFragment
import com.mobymagic.clairediary.ui.onboarding.OnboardingFragment
import com.mobymagic.clairediary.ui.sessionshome.SessionsHomeFragment
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.util.PrefUtil
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.SplashResult
import com.mobymagic.clairediary.vo.User
import timber.log.Timber
import java.util.*


var SPLASH_LAST_UNLOCKED_TIME: Long = 0

class AuthViewModel(
    private val androidUtil: AndroidUtil,
    private val appExecutors: AppExecutors,
    private val prefUtil: PrefUtil,
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L

    private fun getAuthResult(): LiveData<Resource<SplashResult>> {
        val splashLiveData: MutableLiveData<Resource<SplashResult>> = MutableLiveData()

        // Set resource into loading state
        splashLiveData.value =
            Resource.loading(androidUtil.getString(R.string.splash_loading_message))

        val user = userRepository.getLoggedInUser()
        val firebaseUser = firebaseAuth.currentUser

        Timber.d("User: %s, FirebaseUser: %s", user, firebaseUser)
        // Check if the user is logged in
        if (user == null || firebaseUser == null) {
            Timber.d("User is not logged in, open onboarding screen")
            splashLiveData.postValue(getSplashResource(SplashResult.SplashAction.OPEN_ONBOARDING))
        } else if (user.nickname.isEmpty()) {
            Timber.d("User doesn't have a profile yet, open create profile screen")
            splashLiveData.postValue(getSplashResource(SplashResult.SplashAction.OPEN_CREATE_PROFILE))
        } else {


            val twentyFourHoursAgo = Date(Date().time - MILLIS_PER_DAY)
            if (user.timeLastUnlocked != null
                && user.timeLastUnlocked?.after(twentyFourHoursAgo)!!
            ) {
                userLoggedIn = true
                Timber.d("User is logged in, open  destination")
                splashLiveData.postValue(
                    getSuccessResource(
                        user,
                        SplashResult.SplashAction.OPEN_DESTINATION
                    )
                )
            } else {
                // when the user is already logged in just open the Lock scree
                Timber.d("User is logged in, open lock screen")
                splashLiveData.postValue(
                    getSuccessResource(
                        user,
                        SplashResult.SplashAction.OPEN_LOCK_SCREEN
                    )
                )
            }
        }

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

    fun getAuthRoute(finalDestination: NavFragment, owner: MainActivity) {
        getAuthResult().observe(owner, Observer { splashResource ->
            Timber.d("Splash resource: %s", splashResource)
            val splashResult = splashResource!!.data

            when (splashResult?.action) {
                SplashResult.SplashAction.OPEN_ONBOARDING -> {
                    owner.navigate(OnboardingFragment.newInstance(), true)
                }
                SplashResult.SplashAction.OPEN_CREATE_PROFILE -> {
                    owner.navigate(CreateProfileFragment.newInstance(), true)
                }
                SplashResult.SplashAction.OPEN_LOCK_SCREEN -> {
                    owner.navigate(
                        LockScreenFragment.newInstance(
                            splashResult.userId!!,
                            splashResult.secretCode!!,
                            finalDestination
                        ), true
                    )
                }
                SplashResult.SplashAction.OPEN_SESSIONS_HOME -> {
                    owner.navigate(
                        SessionsHomeFragment.newInstance(
                            splashResult.userId!!,
                            splashResult.userType,
                            true,
                            null
                        )
                    )
                }

                SplashResult.SplashAction.OPEN_DESTINATION -> {
                    owner.navigate(finalDestination, true)
                }
            }
        })
    }

    /**
     * This authentication strategy is to filter out users who are not signed logged in
     * so that they cannot access screeens that requires login
     */
    fun authenticateForOpenViews(finalDestination: NavFragment, owner: MainActivity) {
        getOpenScreenAuthResult().observe(owner, Observer { splashResource ->
            Timber.d("Splash resource: %s", splashResource)
            val splashResult = splashResource!!.data

            when (splashResult?.action) {
                SplashResult.SplashAction.OPEN_ONBOARDING -> {
                    owner.navigate(OnboardingFragment.newInstance(), true)
                }
                SplashResult.SplashAction.OPEN_CREATE_PROFILE -> {
                    owner.navigate(CreateProfileFragment.newInstance(), true)
                }
                SplashResult.SplashAction.OPEN_LOCK_SCREEN -> {
                    owner.navigate(
                        LockScreenFragment.newInstance(
                            splashResult.userId!!,
                            splashResult.secretCode!!,
                            finalDestination
                        ), true
                    )
                }
                SplashResult.SplashAction.OPEN_SESSIONS_HOME -> {
                    owner.navigate(
                        SessionsHomeFragment.newInstance(
                            splashResult.userId!!,
                            splashResult.userType,
                            true,
                            null
                        )
                    )
                }
                else -> {

                }
            }
        })
    }

    /**
     * Authentication strategy for open screens, i.e screens that doesnt require login but requires signup
     */
    private fun getOpenScreenAuthResult(): LiveData<Resource<SplashResult>> {
        val splashLiveData: MutableLiveData<Resource<SplashResult>> = MutableLiveData()

        // Set resource into loading state
        splashLiveData.value =
            Resource.loading(androidUtil.getString(R.string.splash_loading_message))

        val user = userRepository.getLoggedInUser()
        val firebaseUser = firebaseAuth.currentUser

        Timber.d("User: %s, FirebaseUser: %s", user, firebaseUser)
        // Check if the user is logged in
        if (user == null || firebaseUser == null) {
            Timber.d("User is not logged in, open onboarding screen")
            splashLiveData.postValue(getSplashResource(SplashResult.SplashAction.OPEN_ONBOARDING))
        } else if (user.nickname.isEmpty()) {
            Timber.d("User doesn't have a profile yet, open create profile screen")
            splashLiveData.postValue(getSplashResource(SplashResult.SplashAction.OPEN_CREATE_PROFILE))
        } else {
            // when the user is already logged in just open destination
            Timber.d("User is logged in, open lock screen")
            splashLiveData.postValue(
                getSuccessResource(
                    user,
                    SplashResult.SplashAction.OPEN_DESTINATION
                )
            )
        }

        return splashLiveData
    }

    fun isUserAvailable(): Boolean {
        val userId: String = userRepository.getUSerId()!!
        return !TextUtils.isEmpty(userId)
    }

    fun getUserId(): String {
        return userRepository.getUSerId()!!
    }

    fun getUserType(): User.UserType {
        return userRepository.getUserType()
    }

    fun updateLastLogin() {
        Thread(Runnable {
            val user = userRepository.getLoggedInUser()
            if (user != null) {
                user.timeLastUnlocked = Date()
                userRepository.setLoggedInUser(user)
            }
        }).start()
    }


    companion object {
        var userLoggedIn: Boolean = false
    }

}