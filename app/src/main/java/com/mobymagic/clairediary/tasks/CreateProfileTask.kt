package com.mobymagic.clairediary.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Status
import com.mobymagic.clairediary.vo.User
import timber.log.Timber

class CreateProfileTask(
        private val androidUtil: AndroidUtil,
        private val userRepository: UserRepository
) {

    fun run(user: User): LiveData<Resource<User>> {
        val createProfileLiveData: MediatorLiveData<Resource<User>> = MediatorLiveData()
        checkNicknameAvailability(user, createProfileLiveData)
        return createProfileLiveData
    }

    private fun checkNicknameAvailability(
            user: User,
            createProfileLiveData: MediatorLiveData<Resource<User>>
    ) {
        val userWithNicknameLiveData = userRepository.getUserWithNickname(user.nickname)
        createProfileLiveData.addSource(userWithNicknameLiveData) {
            Timber.d("User with nickname resource: %s", it)
            when (it?.status) {
                Status.LOADING -> {
                    createProfileLiveData.value = Resource.loading(it.message)
                }
                Status.ERROR -> {
                    createProfileLiveData.removeSource(userWithNicknameLiveData)
                    createProfileLiveData.value = Resource.error(it.message)
                }
                Status.SUCCESS -> {
                    createProfileLiveData.removeSource(userWithNicknameLiveData)
                    if (it.data == null) {
                        Timber.d("Nickname doesn't exist, creating profile")
                        saveUserProfile(user, createProfileLiveData)
                    } else {
                        Timber.w("Nickname already exists")
                        createProfileLiveData.value = Resource.error(
                                androidUtil.getString(R.string.sign_up_error_nickname_exists)
                        )
                    }
                }
            }
        }
    }

    private fun saveUserProfile(
            user: User,
            createProfileLiveData: MediatorLiveData<Resource<User>>
    ) {
        val saveUserLiveData = userRepository.addUser(user)
        createProfileLiveData.addSource(saveUserLiveData) {
            Timber.d("Save profile resource: %s", it)
            when (it?.status) {
                Status.LOADING -> {
                    createProfileLiveData.value = Resource.loading(it.message)
                }
                Status.ERROR -> {
                    createProfileLiveData.removeSource(saveUserLiveData)
                    createProfileLiveData.value = Resource.error(it.message)
                }
                Status.SUCCESS -> {
                    createProfileLiveData.removeSource(saveUserLiveData)
                    createProfileLiveData.value = Resource.success(it.data)

                    Timber.d("Profile created. Updating logged in user")
                    userRepository.setLoggedInUser(user)
                }
            }
        }
    }
}