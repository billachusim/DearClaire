package com.mobymagic.clairediary.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.mobymagic.clairediary.tasks.SignUpTask
import com.mobymagic.clairediary.util.InputUtil
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.User
import timber.log.Timber

class SignUpViewModel(
        private val inputUtil: InputUtil,
        private val signUpTask: SignUpTask
) : ViewModel() {

    private val newUserLiveData: MutableLiveData<User> = MutableLiveData()
    private val inputErrorLiveData: MutableLiveData<InputError> = MutableLiveData()
    private val signUpLiveData: LiveData<Resource<FirebaseUser>>

    init {
        signUpLiveData = Transformations.switchMap(newUserLiveData) {
            Timber.d("User changed: %s", it)
            signUpTask.run(it)
        }
    }

    fun setUser(email: String, secretCode: String) {
        Timber.d("Setting user email to %s and secretCode to %s", email, secretCode)
        if (!inputUtil.isValidEmail(email)) {
            Timber.w("Rejecting user email because it is invalid")
            inputErrorLiveData.value = InputError.INVALID_EMAIL
        } else if (secretCode.length < 6) {
            Timber.w("Rejecting user secret code because it is invalid")
            inputErrorLiveData.value = InputError.INVALID_SECRET_CODE
        } else {
            Timber.d("User email and secret code valid, updating user")
            val user = User(email, secretCode)
            newUserLiveData.value = user
        }
    }

    fun getInputErrorLiveData(): LiveData<InputError> = inputErrorLiveData

    fun getSignUpResultLiveData(): LiveData<Resource<FirebaseUser>> = signUpLiveData

    enum class InputError {
        INVALID_EMAIL, INVALID_SECRET_CODE
    }

}