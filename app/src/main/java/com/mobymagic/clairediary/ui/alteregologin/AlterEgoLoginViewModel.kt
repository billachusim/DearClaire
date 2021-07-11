package com.mobymagic.clairediary.ui.alteregologin

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Status
import com.mobymagic.clairediary.vo.User
import timber.log.Timber

class AlterEgoLoginViewModel(
    private val androidUtil: AndroidUtil,
    private val userRepository: UserRepository
) : ViewModel() {

    private val credentialLiveData: MutableLiveData<Credential> = MutableLiveData()
    private val resultLiveData: MediatorLiveData<Resource<User>> = MediatorLiveData()

    init {
        resultLiveData.addSource(credentialLiveData) { login ->
            resultLiveData.value =
                Resource.loading(androidUtil.getString(R.string.common_message_loading))
            loginIntoAlterEgo(login!!)
        }
    }

    private fun loginIntoAlterEgo(credential: Credential) {
        val getAdminLiveData =
            userRepository.getAdmin(credential.claireId, credential.accessCode)
        resultLiveData.addSource(getAdminLiveData) { adminResource ->
            Timber.d("Get profile resource changed: %s", adminResource)
            when (adminResource!!.status) {
                Status.LOADING -> {
                    resultLiveData.value = Resource.loading(adminResource.message)
                }
                Status.ERROR -> {
                    resultLiveData.removeSource(getAdminLiveData)
                    resultLiveData.value = Resource.error(adminResource.message)
                }
                Status.SUCCESS -> {
                    resultLiveData.removeSource(getAdminLiveData)

                    if (adminResource.data != null) {
                        userRepository.setLoggedInUser(adminResource.data)
                    }

                    resultLiveData.value = Resource.success(adminResource.data)
                }
            }
        }
    }

    fun setAlterEgoCredentials(claireId: String, accessCode: String) {
        val credential = Credential(claireId, accessCode)
        Timber.d("Setting credential to: %s", credential)
        credentialLiveData.value = credential
    }

    fun getLoginResultLiveData() = resultLiveData

    data class Credential(val claireId: String, val accessCode: String)

}