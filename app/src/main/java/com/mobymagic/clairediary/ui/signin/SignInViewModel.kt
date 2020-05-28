package com.mobymagic.clairediary.ui.signin

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.repository.AuthRepository
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.vo.AsyncRequest
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Status
import com.mobymagic.clairediary.vo.User
import timber.log.Timber


class SignInViewModel(
        private val androidUtil: AndroidUtil,
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository
) : ViewModel() {

    private val loginLiveData: MutableLiveData<Login> = MutableLiveData()
    private val resultLiveData: MediatorLiveData<Resource<AsyncRequest>> = MediatorLiveData()
    private var firebaseUser: FirebaseUser? = null

    init {
        resultLiveData.addSource(loginLiveData) { login ->
            resultLiveData.value =
                    Resource.loading(androidUtil.getString(R.string.common_message_loading))
            if (firebaseUser == null) {
                Timber.d("User not logged in. Signing into Firebase auth")
                signIn(login!!)
            } else {
                Timber.d("User logged in. Getting profile")
                getUserProfile(login!!, firebaseUser!!)
            }
        }
    }

    private fun signIn(login: Login) {
        val signInLiveData = authRepository.signIn(login.email, login.secretCode)
        resultLiveData.addSource(signInLiveData) { userResource ->
            Timber.d("Sign in resource changed: %s", userResource)
            when (userResource!!.status) {
                Status.LOADING -> {
                    resultLiveData.value = Resource.loading(userResource.message)
                }
                Status.ERROR -> {
                    resultLiveData.removeSource(signInLiveData)
                    resultLiveData.value = Resource.error(userResource.message)
                }
                Status.SUCCESS -> {
                    resultLiveData.removeSource(signInLiveData)
                    firebaseUser = userResource.data
                    getUserProfile(login, userResource.data!!)
                }
            }
        }
    }

    private fun getUserProfile(login: Login, firebaseUser: FirebaseUser) {
        val getProfileLiveData = userRepository.getUserWithEmail(login.email)
        resultLiveData.addSource(getProfileLiveData) { userResource ->
            Timber.d("Get profile resource changed: %s", userResource)
            when (userResource!!.status) {
                Status.LOADING -> {
                    resultLiveData.value = Resource.loading(userResource.message)
                }
                Status.ERROR -> {
                    resultLiveData.removeSource(getProfileLiveData)
                    resultLiveData.value = Resource.error(userResource.message)
                }
                Status.SUCCESS -> {
                    resultLiveData.removeSource(getProfileLiveData)
                    if (userResource.data == null) {
                        val userWithoutProfile = User(login.email, login.secretCode)
                        userWithoutProfile.userId = firebaseUser.uid
                        userRepository.setLoggedInUser(userWithoutProfile)
                        resultLiveData.value = Resource.success(AsyncRequest())
                    } else {
                        userResource.data.email = login.email
                        userResource.data.secretCode = login.secretCode
                        userRepository.setLoggedInUser(userResource.data)
                        resultLiveData.value = Resource.success(AsyncRequest())
                    }
                }
            }
        }
    }

    fun setLogin(email: String, secretCode: String) {
        val login = Login(email, secretCode)
        Timber.d("Setting login to: %s", login)
        loginLiveData.value = login
    }

    fun getSignInResultLiveData() = resultLiveData

    data class Login(val email: String, val secretCode: String)

}