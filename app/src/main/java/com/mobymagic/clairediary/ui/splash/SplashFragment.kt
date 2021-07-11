package com.mobymagic.clairediary.ui.splash

import android.os.Bundle
import androidx.lifecycle.Observer
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentSplashBinding
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.onboarding.OnboardingFragment
import com.mobymagic.clairediary.ui.sessionshome.SessionsHomeFragment
import com.mobymagic.clairediary.vo.SplashResult
import com.mobymagic.clairediary.vo.User
import org.koin.android.ext.android.inject

/**
 * A simple Fragment that shows a splash screen with an artificial delay then navigates to the
 * right screen
 */
class SplashFragment : DataBoundNavFragment<FragmentSplashBinding>() {

    override var requiresAuthentication: Boolean = false
    override var openToNewUsers: Boolean = true

    private val splashViewModel: SplashViewModel by inject()

    override fun getLayoutRes() = R.layout.fragment_splash

    override fun getPageTitle(): Nothing? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //just simulate the splash screen and navigate to the home screen, irrespective of the user log in status
        splashViewModel.simulateSplashScreen()
            .observe(requireActivity(), Observer { splashResource ->
                val splashResult = splashResource!!.data
                when (splashResult?.action) {
                    SplashResult.SplashAction.OPEN_SESSIONS_HOME -> {
                        getNavController().navigate(OnboardingFragment.newInstance())
                        getNavController().navigate(
                            SessionsHomeFragment.newInstance(
                                "",
                                User.UserType.REGULAR,
                                false,
                                null
                            )
                        )
                    }
                }
            })
//        splashViewModel.getSplashResult(justUnlocked).observe(this, Observer { splashResource ->
//            Timber.d("Splash resource: %s", splashResource)
//            val splashResult = splashResource!!.data

//            when (splashResult?.action) {
//                SplashResult.SplashAction.OPEN_ONBOARDING -> {
//                    getNavController().navigate(OnboardingFragment.newInstance())
//                }
//                SplashResult.SplashAction.OPEN_CREATE_PROFILE -> {
//                    getNavController().navigate(CreateProfileFragment.newInstance())
//                }
//                SplashResult.SplashAction.OPEN_LOCK_SCREEN -> {
//                    getNavController().navigate(
//                            LockScreenFragment.newInstance(
//                                    splashResult.userId!!,
//                                    splashResult.secretCode!!, SessionsHomeFragment.newInstance(
//                                    "",
//                                    User.UserType.REGULAR,
//                                    false
//                            )
//                            )
//                    )
//                }
//                SplashResult.SplashAction.OPEN_SESSIONS_HOME -> {
//                    getNavController().navigate(
//                            SessionsHomeFragment.newInstance(
//                                    splashResult.userId!!,
//                                    splashResult.userType, true
//                            )
//                    )
//                }
//            }
//        })

    }


    companion object {

        private const val ARG_JUST_UNLOCKED = "ARG_JUST_UNLOCKED"

        fun newInstance(justUnlocked: Boolean = false): SplashFragment {
            val splashFragment = SplashFragment()
            val args = Bundle()
            args.putBoolean(ARG_JUST_UNLOCKED, justUnlocked)
            splashFragment.arguments = args
            return splashFragment
        }

    }
}
