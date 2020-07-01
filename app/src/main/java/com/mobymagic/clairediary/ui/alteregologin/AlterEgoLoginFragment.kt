package com.mobymagic.clairediary.ui.alteregologin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mobymagic.clairediary.Constants
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentAlterEgoLoginBinding
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.ui.alteregointro.AlterEgoIntroFragment
import com.mobymagic.clairediary.ui.alteregosplash.AlterEgoSplashFragment
import com.mobymagic.clairediary.ui.common.ClearErrorTextWatcher
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.util.DonateUtil
import com.mobymagic.clairediary.util.FragmentUtils
import com.mobymagic.clairediary.util.PrefUtil
import com.mobymagic.clairediary.util.ThemeUtil
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Status
import com.mobymagic.clairediary.vo.User
import org.koin.android.ext.android.inject
import timber.log.Timber

class AlterEgoLoginFragment : DataBoundNavFragment<FragmentAlterEgoLoginBinding>() {

    private val fragmentUtil: FragmentUtils by inject()
    private val themeUtil: ThemeUtil by inject()
    private val donateUtil: DonateUtil by inject()
    private val userRepository: UserRepository by inject()
    private val prefUtil: PrefUtil by inject()
    private val signInViewModel: AlterEgoLoginViewModel by inject()
    private lateinit var userId: String

    override fun getLayoutRes() = R.layout.fragment_alter_ego_login

    override fun getPageTitle(): Nothing? = null

    override fun isAlterEgoPage() = true

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        userId = requireArguments().getString(ARG_USER_ID).toString()
        initForm()
        observeLoginResult()

        val themes = resources.getStringArray(R.array.settings_theme_names)
        val curTheme = prefUtil.getString(Constants.PREF_THEME, null) ?: themes[0]
        val alterEgoTheme = themeUtil.getAlterEgoTheme(curTheme)
        binding.alterEgoLoginContainer.setBackgroundColor(alterEgoTheme.primaryColor)

        fragmentUtil.addIfNotExist(
                childFragmentManager,
                R.id.alter_ego_login_splash_container,
                AlterEgoSplashFragment.newInstance()
        )
    }

    private fun initForm() {
        binding.alterEgoLoginButton.setOnClickListener {
            Timber.d("Login clicked")
            val claireId = binding.alterEgoLoginIdInput.text.toString()
            val accessCode = binding.alterEgoLoginAccessCodeInput.text.toString()

            if (isFormValid(claireId, accessCode)) {
                Timber.d("Login form is valid, submitting")
                signInViewModel.setAlterEgoCredentials(claireId, accessCode)
            }
        }

        binding.alterEgoRequestAccessButton.setOnClickListener {
            val introPage = AlterEgoIntroFragment.newInstance(userId)
            getNavController().navigate(introPage)
        }

        binding.alterEgoDonateButton.setOnClickListener {
            val curLoggedInUser = userRepository.getLoggedInUser()
            donateUtil.donate(requireActivity(), curLoggedInUser!!)
        }

        val clearErrorTextWatcher = ClearErrorTextWatcher(
                binding.alterEgoLoginIdInputLayout,
                binding.alterEgoLoginAccessCodeInputLayout
        )
        binding.alterEgoLoginIdInput.addTextChangedListener(clearErrorTextWatcher)
        binding.alterEgoLoginAccessCodeInput.addTextChangedListener(clearErrorTextWatcher)
    }

    private fun isFormValid(claireId: String, accessCode: String): Boolean {
        return when {
            claireId.length < 6 -> {
                binding.alterEgoLoginIdInputLayout.error =
                        getString(R.string.alter_ego_input_error_claire_id_invalid)
                false
            }
            accessCode.length < 4 -> {
                binding.alterEgoLoginAccessCodeInputLayout.error =
                        getString(R.string.alter_ego_input_error_access_code_invalid)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun observeLoginResult() {
        signInViewModel.getLoginResultLiveData().observe(viewLifecycleOwner, Observer { userResource ->
            Timber.d("Login resource: %s", userResource)
            binding.resource = userResource
            handleSignUpResult(userResource!!)
        })
    }

    private fun handleSignUpResult(userResource: Resource<User>) {
        if (userResource.status == Status.SUCCESS) {
            if (userResource.data == null) {
                Toast.makeText(requireContext(), R.string.alter_ego_login_incorrect, Toast.LENGTH_LONG)
                        .show()
            } else {
                Toast.makeText(requireContext(), R.string.alter_ego_login_correct, Toast.LENGTH_LONG)
                        .show()

                val intent = Intent(Constants.EVENT_OPEN_ALTER_EGO)
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                getNavController().removeFromBackstack(this)
                getNavController().remove(this)
            }
        }
    }

    companion object {

        private const val ARG_USER_ID = "ARG_USER_ID"

        fun newInstance(userId: String): AlterEgoLoginFragment {
            val loginFragment = AlterEgoLoginFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            loginFragment.arguments = args
            return loginFragment
        }

    }
}

