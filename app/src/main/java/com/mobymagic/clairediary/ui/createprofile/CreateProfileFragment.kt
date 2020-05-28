package com.mobymagic.clairediary.ui.createprofile

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.lifecycle.Observer
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentCreateProfileBinding
import com.mobymagic.clairediary.ui.clairevartar.ClaireVartarFragment
import com.mobymagic.clairediary.ui.clairevartar.ClaireVartarFragment.Companion.USER_AVARTAR_KEY
import com.mobymagic.clairediary.ui.common.ClearErrorTextWatcher
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.splash.SplashFragment
import com.mobymagic.clairediary.vo.Status
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * Page to create a new profile
 */
class CreateProfileFragment : DataBoundNavFragment<FragmentCreateProfileBinding>() {

    override var requiresAuthentication: Boolean = false
    override var openToNewUsers: Boolean = true

    private val createProfileViewModel: CreateProfileViewModel by inject()

    override fun getLayoutRes() = R.layout.fragment_create_profile

    override fun getPageTitle(): Nothing? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initForm()
        observeInputErrors()
        observeCreateProfileResult()
    }

    private fun initForm() {
        binding.userImageUrl = ""
        binding.createProfileButton.setOnClickListener {
            val nickname = binding.createProfileNicknameInput.text.toString().toLowerCase()
            val genderPos = binding.createProfileGenderSpinner.selectedItemPosition
            val genders = resources.getStringArray(R.array.create_profile_genders)
            val gender = if (genderPos == 0) null else genders[genderPos]
            val userImageUrl = binding.userImageUrl
            if (TextUtils.isEmpty(userImageUrl)) {
                createProfileViewModel.setUserProfile(nickname, gender)
            } else {
                createProfileViewModel.setUserProfile(nickname, gender, userImageUrl)
            }


        }

        binding.userImage.setOnClickListener {
            val fm = fragmentManager
            val claireVartarFragment = ClaireVartarFragment()
            claireVartarFragment.setTargetFragment(this, CLAIRE_VARTAR_REQUEST_CODE)
            if (fm != null) {
                claireVartarFragment.show(fm, "claireVartarFragment")
            }
        }

        val clearErrorTextWatcher = ClearErrorTextWatcher(binding.createProfileNicknameInputLayout)
        binding.createProfileNicknameInput.addTextChangedListener(clearErrorTextWatcher)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CLAIRE_VARTAR_REQUEST_CODE) {
            val userAvartar = data?.getStringExtra(USER_AVARTAR_KEY)
            if (!TextUtils.isEmpty(userAvartar)) {
                binding.userImageUrl = userAvartar
            }
        }
    }

    private fun observeInputErrors() {
        createProfileViewModel.getInputErrorLiveData().observe(viewLifecycleOwner, Observer {
            Timber.d("Input error: %s", it)
            when (it) {
                CreateProfileViewModel.InputError.GENDER_INVALID -> {
                    Toast.makeText(
                            activity,
                            getString(R.string.create_profile_input_error_gender_invalid),
                            Toast.LENGTH_LONG
                    ).show()
                }
                CreateProfileViewModel.InputError.NICKNAME_INVALID -> {
                    binding.createProfileNicknameInputLayout.error =
                            getString(R.string.create_profile_input_error_nickname_invalid)
                }
                CreateProfileViewModel.InputError.NICKNAME_CLAIRE_RESTRICTED -> {
                    binding.createProfileNicknameInputLayout.error =
                            getString(R.string.create_profile_input_error_nickname_claire_not_allowed)
                }
            }
        })
    }

    private fun observeCreateProfileResult() {
        createProfileViewModel.getCreateProfileResultLiveData().observe(viewLifecycleOwner, Observer {
            Timber.d("Create profile result resource: %s", it)
            binding.resource = it

            if (it?.status == Status.SUCCESS) {
                onProfileCreatedSuccessfully()
            }
        })
    }

    private fun onProfileCreatedSuccessfully() {
        getNavController().clearBackStack()
        getNavController().navigate(SplashFragment.newInstance(true))
    }

    companion object {
        val CLAIRE_VARTAR_REQUEST_CODE = 2000
        fun newInstance(): CreateProfileFragment {
            return CreateProfileFragment()
        }

    }

}
