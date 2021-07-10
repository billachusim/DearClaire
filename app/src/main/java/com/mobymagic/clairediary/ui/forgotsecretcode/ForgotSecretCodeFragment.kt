package com.mobymagic.clairediary.ui.forgotsecretcode

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentForgotSecretCodeBinding
import com.mobymagic.clairediary.ui.common.ClearErrorTextWatcher
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.util.InputUtil
import org.koin.android.ext.android.inject
import timber.log.Timber


/**
 * Forgot secret code page to reset the user secret code
 */
class ForgotSecretCodeFragment : DataBoundNavFragment<FragmentForgotSecretCodeBinding>() {

    override var requiresAuthentication: Boolean = false
    override var openToNewUsers: Boolean = true

    private val forgotCodeViewModel: ForgotSecretCodeViewModel by inject()
    private val inputUtil: InputUtil by inject()

    override fun getLayoutRes() = R.layout.fragment_forgot_secret_code

    override fun getPageTitle(): Nothing? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initForm()
        listenForResult()
    }

    private fun initForm() {
        binding.forgotCodeResetButton.setOnClickListener {
            val email = binding.forgotCodeEmailInput.text.toString()
            if (isFormValid(email)) {
                forgotCodeViewModel.setEmail(email)
            }
        }

        binding.forgotCodeOpenInboxButton.setOnClickListener {
            openEmailClient()
        }

        val clearErrorTextWatcher = ClearErrorTextWatcher(binding.forgotCodeEmailInputLayout)
        binding.forgotCodeEmailInput.addTextChangedListener(clearErrorTextWatcher)
    }

    private fun isFormValid(email: String): Boolean {
        return if (email.isEmpty()) {
            binding.forgotCodeEmailInputLayout.error =
                getString(R.string.forgot_secret_code_error_email_empty)
            false
        } else if (!inputUtil.isValidEmail(email)) {
            binding.forgotCodeEmailInputLayout.error =
                getString(R.string.forgot_secret_code_error_email_invalid)
            false
        } else {
            true
        }
    }

    private fun listenForResult() {
        forgotCodeViewModel.getResetRequestLiveData().observe(this, Observer { result ->
            Timber.d("Reset secret code request resource: %s", result)
            binding.resource = result
        })
    }

    private fun openEmailClient() {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_EMAIL)
            startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e)
            Toast.makeText(
                activity,
                R.string.forgot_secret_code_error_no_email_client,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {

        fun newInstance(): ForgotSecretCodeFragment {
            return ForgotSecretCodeFragment()
        }

    }
}
