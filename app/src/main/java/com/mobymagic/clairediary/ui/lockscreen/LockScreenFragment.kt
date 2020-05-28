package com.mobymagic.clairediary.ui.lockscreen

import android.os.Bundle
import android.widget.Toast
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentLockScreenBinding
import com.mobymagic.clairediary.ui.auth.AuthViewModel
import com.mobymagic.clairediary.ui.common.ClearErrorTextWatcher
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.common.NavFragment
import com.mobymagic.clairediary.ui.forgotsecretcode.ForgotSecretCodeFragment
import com.mobymagic.clairediary.ui.splash.SplashFragment
import org.koin.android.ext.android.inject


/**
 * Lock screen page for unlocking the app periodically
 */
class LockScreenFragment : DataBoundNavFragment<FragmentLockScreenBinding>() {

    override var requiresAuthentication: Boolean = false
    override var openToNewUsers: Boolean = true
    val authViewModel: AuthViewModel by inject()

    override fun getLayoutRes() = R.layout.fragment_lock_screen

    override fun getPageTitle(): Nothing? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lockScreenUnlockButton.setOnClickListener { onUnlockButtonClicked() }

        binding.lockScreenForgotSecretCodeButton.setOnClickListener { navigateToForgotSecretCode() }

        val clearErrorTextWatcher = ClearErrorTextWatcher(binding.lockScreenSecretCodeInputLayout)
        binding.lockScreenSecretCodeInput.addTextChangedListener(clearErrorTextWatcher)

    }

    private fun onUnlockButtonClicked() {
        val userSecretCode = requireArguments().getString(ARG_SECRET_CODE)
        val inputSecretCode = binding.lockScreenSecretCodeInput.text.toString()

        // Check user secret code input
        when {
            inputSecretCode.isEmpty() -> {
                binding.lockScreenSecretCodeInputLayout.error =
                        getString(R.string.lock_screen_error_secret_code_empty)
            }
            inputSecretCode != userSecretCode -> {
                binding.lockScreenSecretCodeInputLayout.error =
                        getString(R.string.lock_screen_error_secret_code_incorrect)
            }
            else -> {
                Toast.makeText(context, R.string.lock_screen_welcome_message, Toast.LENGTH_LONG)
                        .show()
                AuthViewModel.userLoggedIn = true
                authViewModel.updateLastLogin()
                if (destination != null) {
                    getNavController().navigate(destination!!)
                } else {
                    getNavController().navigate(SplashFragment.newInstance(true))
                }
            }
        }
    }

    private fun navigateToForgotSecretCode() {
        val forgotSecretCodeFragment = ForgotSecretCodeFragment.newInstance()
        getNavController().navigate(
                forgotSecretCodeFragment,
                addToBackStack = true,
                onlyAddIfNotExist = false
        )
    }

    companion object {

        var destination: NavFragment? = null
        private const val ARG_USER_ID = "ARG_USER_ID"
        private const val ARG_SECRET_CODE = "ARG_SECRET_CODE"

        fun newInstance(userId: String, secretCode: String, destination: NavFragment): LockScreenFragment {
            val lockScreenFragment = LockScreenFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            args.putString(ARG_SECRET_CODE, secretCode)
            this.destination = destination
            lockScreenFragment.arguments = args
            return lockScreenFragment
        }

    }
}
