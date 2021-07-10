package com.mobymagic.clairediary.ui.createprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.tasks.CreateProfileTask
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.User
import timber.log.Timber

private const val RESTRICTED_NICKNAME_CONTENT = "claire"

class CreateProfileViewModel(
    private val userRepository: UserRepository,
    private val createProfileTask: CreateProfileTask
) : ViewModel() {

    private val userLiveData: MutableLiveData<User> = MutableLiveData()
    private val inputErrorLiveData: MutableLiveData<InputError> = MutableLiveData()
    private val createProfileResultLiveData: LiveData<Resource<User>>

    init {
        createProfileResultLiveData = Transformations.switchMap(userLiveData) {
            Timber.d("User changed: %s", it)
            createProfileTask.run(it)
        }
    }

    fun setUserProfile(nickname: String, gender: String?, userImageUrl: String? = null) {
        val user = userRepository.getLoggedInUser()!!
        when {
            nickname.length < 3 -> {
                Timber.w("Rejecting user nickname because it is invalid")
                inputErrorLiveData.value = InputError.NICKNAME_INVALID
            }
            nickname.contains(RESTRICTED_NICKNAME_CONTENT, true) -> {
                Timber.w("Rejecting user nickname because it contains restricted content")
                inputErrorLiveData.value = InputError.NICKNAME_CLAIRE_RESTRICTED
            }
            gender == null -> {
                Timber.w("Rejecting user gender because it is null")
                inputErrorLiveData.value = InputError.GENDER_INVALID
            }
            else -> {
                Timber.d("User profile details valid, updating user")
                user.nickname = nickname
                user.gender = gender
                if (userImageUrl != null) {
                    user.avatarUrl = userImageUrl
                }
                userLiveData.value = user
            }
        }
    }

    fun getCreateProfileResultLiveData() = createProfileResultLiveData

    fun getInputErrorLiveData() = inputErrorLiveData

    enum class InputError {
        GENDER_INVALID, NICKNAME_INVALID, NICKNAME_CLAIRE_RESTRICTED;
    }

}