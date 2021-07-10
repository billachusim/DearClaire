package com.mobymagic.clairediary.ui.onboarding

import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentOnboardingBinding
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.common.NavFragment
import com.mobymagic.clairediary.ui.signin.SignInFragment
import com.mobymagic.clairediary.ui.signup.SignUpFragment

class OnboardingFragment : DataBoundNavFragment<FragmentOnboardingBinding>() {

    override var requiresAuthentication: Boolean = false
    override var openToNewUsers: Boolean = true

    override fun getLayoutRes() = R.layout.fragment_onboarding

    override fun getPageTitle(): Nothing? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        animateSignUpButtonHint()

        binding.onboardingSignInButton.setOnClickListener {
            val signInFragment = SignInFragment.newInstance(null)
            navigateToDestination(signInFragment)
        }

        binding.onboardingSignUpButton.setOnClickListener {
            val signUpFragment = SignUpFragment.newInstance()
            navigateToDestination(signUpFragment)
        }
    }

    private fun animateSignUpButtonHint() {
        val flashAnimation = AlphaAnimation(0f, 0.2f)
        flashAnimation.duration =
            resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        flashAnimation.interpolator = AccelerateDecelerateInterpolator()
        flashAnimation.repeatCount = Animation.INFINITE
        flashAnimation.repeatMode = Animation.REVERSE
        binding.onboardingSignUpButtonHint.startAnimation(flashAnimation)
    }

    private fun navigateToDestination(destination: NavFragment) {
        getNavController().navigate(
            destination,
            true,
            false
        )
    }

    companion object {

        fun newInstance(): OnboardingFragment {
            return OnboardingFragment()
        }

    }

}
