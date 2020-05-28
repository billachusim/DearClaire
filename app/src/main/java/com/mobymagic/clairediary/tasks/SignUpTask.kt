package com.mobymagic.clairediary.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.google.firebase.auth.FirebaseUser
import com.mobymagic.clairediary.repository.AuthRepository
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Status
import com.mobymagic.clairediary.vo.User
import timber.log.Timber

class SignUpTask(
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository
) {

    fun run(user: User): LiveData<Resource<FirebaseUser>> {
        Timber.d("Signing up user: %s", user)
        val signUpResultLiveData = MediatorLiveData<Resource<FirebaseUser>>()
        val authLiveData = authRepository.signUp(user.email, user.secretCode)

        signUpResultLiveData.addSource(authLiveData) {
            if (it?.status == Status.SUCCESS) {
                Timber.d("Sign up successful, setting user id to: %s", it.data!!.uid)
                user.userId = it.data.uid
                userRepository.setLoggedInUser(user)
                signUpResultLiveData.removeSource(authLiveData)
            }

            signUpResultLiveData.value = it
        }

        return signUpResultLiveData
    }

}