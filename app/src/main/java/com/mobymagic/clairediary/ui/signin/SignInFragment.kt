package com.mobymagic.clairediary.ui.signin

import android.os.Bundle
import androidx.lifecycle.Observer
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentSignInBinding
import com.mobymagic.clairediary.ui.auth.AuthViewModel
import com.mobymagic.clairediary.ui.common.ClearErrorTextWatcher
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.common.NavFragment
import com.mobymagic.clairediary.ui.forgotsecretcode.ForgotSecretCodeFragment
import com.mobymagic.clairediary.ui.splash.SplashFragment
import com.mobymagic.clairediary.util.InputUtil
import com.mobymagic.clairediary.vo.AsyncRequest
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Status
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * Sign in page for login into an existing account
 */
class SignInFragment : DataBoundNavFragment<FragmentSignInBinding>() {

    override var requiresAuthentication: Boolean = false
    override var openToNewUsers: Boolean = true

    private val signInViewModel: SignInViewModel by inject()
    private val inputUtil: InputUtil by inject()
    private val authViewModel: AuthViewModel by inject()


    override fun getLayoutRes() = R.layout.fragment_sign_in

    override fun getPageTitle(): Nothing? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initForm()
        listenForResult()
    }

    private fun initForm() {
        binding.signInButton.setOnClickListener {
            Timber.d("Sign in clicked")
            val email = binding.signInEmailInput.text.toString()
            val secretCode = binding.signInSecretCodeInput.text.toString()

            if (isFormValid(email, secretCode)) {
                Timber.d("Sign in form is valid, submitting")
                signInViewModel.setLogin(email, secretCode)
            }
        }

        binding.signInForgotSecretCodeButton.setOnClickListener {
            navigateToForgotSecretCode()
        }

        val clearErrorTextWatcher = ClearErrorTextWatcher(
                binding.signInSecretCodeInputLayout,
                binding.signInEmailInputLayout
        )
        binding.signInEmailInput.addTextChangedListener(clearErrorTextWatcher)
        binding.signInSecretCodeInput.addTextChangedListener(clearErrorTextWatcher)
    }

    private fun navigateToForgotSecretCode() {
        val forgotSecretCodeFragment = ForgotSecretCodeFragment.newInstance()

        getNavController().navigate(
                forgotSecretCodeFragment,
                true,
                false
        )
    }

    private fun isFormValid(email: String, secretCode: String): Boolean {
        return when {
            !inputUtil.isValidEmail(email) -> {
                binding.signInEmailInputLayout.error =
                        getString(R.string.sign_in_input_error_email_invalid)
                false
            }
            secretCode.length < 6 -> {
                binding.signInSecretCodeInputLayout.error =
                        getString(R.string.sign_in_input_error_secret_invalid)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun listenForResult() {
        signInViewModel.getSignInResultLiveData().observe(this, Observer { result ->
            Timber.d("Sign in result resource: %s", result)
            binding.resource = result
            handleSignUpResult(result!!)
        })
    }

    private fun handleSignUpResult(result: Resource<AsyncRequest>) {
        if (result.status == Status.SUCCESS) {
            getNavController().clearBackStack()
            getNavController().navigate(SplashFragment.newInstance(true))
            authViewModel.updateLastLogin()
            AuthViewModel.userLoggedIn = true
        }
    }

    companion object {

        var destination: NavFragment? = null

        fun newInstance(destination: NavFragment?): SignInFragment {
            this.destination = destination
            return SignInFragment()
        }

    }
}
