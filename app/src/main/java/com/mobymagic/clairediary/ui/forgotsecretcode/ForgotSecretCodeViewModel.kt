package com.mobymagic.clairediary.ui.forgotsecretcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mobymagic.clairediary.repository.AuthRepository
import com.mobymagic.clairediary.vo.AsyncRequest
import com.mobymagic.clairediary.vo.Resource
import timber.log.Timber

class ForgotSecretCodeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val emailLiveData: MutableLiveData<String> = MutableLiveData()
    private val resetRequestLiveData: LiveData<Resource<AsyncRequest>>

    init {
        resetRequestLiveData = Transformations.switchMap(emailLiveData) { email ->
            authRepository.sendSecretCodeResetEmail(email)
        }
    }

    fun setEmail(email: String) {
        Timber.d("Setting email to: %s", email)
        emailLiveData.value = email
    }

    fun getResetRequestLiveData() = resetRequestLiveData

}