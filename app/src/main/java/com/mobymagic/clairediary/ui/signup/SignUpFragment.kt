package com.mobymagic.clairediary.ui.signup

import android.os.Bundle
import androidx.lifecycle.Observer
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentSignUpBinding
import com.mobymagic.clairediary.ui.common.ClearErrorTextWatcher
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.splash.SplashFragment
import com.mobymagic.clairediary.vo.Status
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * Sign up page for creating a new account
 */
class SignUpFragment : DataBoundNavFragment<FragmentSignUpBinding>() {

    override var requiresAuthentication: Boolean = false
    override var openToNewUsers: Boolean = true

    private val signUpViewModel: SignUpViewModel by inject()

    override fun getLayoutRes() = R.layout.fragment_sign_up

    override fun getPageTitle(): Nothing? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initForm()
        observeInputErrors()
        observeSignUpResult()
    }

    private fun initForm() {
        binding.signUpButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val secretCode = binding.secretCodeInput.text.toString()
            signUpViewModel.setUser(email, secretCode)
        }

        val clearErrorTextWatcher = ClearErrorTextWatcher(
                binding.emailInputLayout,
                binding.secretCodeInputLayout
        )
        binding.emailInput.addTextChangedListener(clearErrorTextWatcher)
        binding.secretCodeInput.addTextChangedListener(clearErrorTextWatcher)
    }

    private fun observeInputErrors() {
        signUpViewModel.getInputErrorLiveData().observe(this, Observer {
            Timber.d("Input error: %s", it)
            when (it) {
                SignUpViewModel.InputError.INVALID_EMAIL -> {
                    binding.emailInputLayout.error =
                            getString(R.string.sign_up_input_error_email_invalid)
                }
                SignUpViewModel.InputError.INVALID_SECRET_CODE -> {
                    binding.secretCodeInputLayout.error =
                            getString(R.string.sign_up_input_error_secret_invalid)
                }
            }
        })
    }

    private fun observeSignUpResult() {
        signUpViewModel.getSignUpResultLiveData().observe(this, Observer {
            Timber.d("Sign up result resource: %s", it)
            binding.resource = it

            if (it?.status == Status.SUCCESS) {
                onSignUpSuccessful()
            }
        })
    }

    private fun onSignUpSuccessful() {
        getNavController().clearBackStack()
        getNavController().navigate(SplashFragment.newInstance(true))
    }

    companion object {

        fun newInstance(): SignUpFragment {
            return SignUpFragment()
        }

    }

}
