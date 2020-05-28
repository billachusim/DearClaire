package com.mobymagic.clairediary.ui.alteregosplash

import android.os.Bundle
import android.os.Handler
import com.mobymagic.clairediary.Constants
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentAlterEgoSplashBinding
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.sessionshome.SessionsHomeFragment
import com.mobymagic.clairediary.util.PrefUtil
import com.mobymagic.clairediary.util.ThemeUtil
import com.mobymagic.clairediary.vo.User
import org.koin.android.ext.android.inject
import timber.log.Timber

class AlterEgoSplashFragment : DataBoundNavFragment<FragmentAlterEgoSplashBinding>() {

    private val themeUtil: ThemeUtil by inject()
    private val prefUtil: PrefUtil by inject()

    private val splashRunnable = Runnable {
        try {
            if (isAdded) {
                parentFragment?.childFragmentManager?.beginTransaction()?.remove(this)?.commit()
            }
        } catch (ex: Exception) {
            // go back home
            Timber.e(ex)
            getNavController().navigate(SessionsHomeFragment.newInstance(
                    "",
                    User.UserType.REGULAR,
                    false,
                    R.id.nav_session_type_featured
            ))
        }
    }

    override fun getLayoutRes() = R.layout.fragment_alter_ego_splash

    override fun getPageTitle(): Nothing? = null

    override fun isAlterEgoPage() = true

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val themes = resources.getStringArray(R.array.settings_theme_names)
        val curTheme = prefUtil.getString(Constants.PREF_THEME, null) ?: themes[0]
        val alterEgoTheme = themeUtil.getAlterEgoTheme(curTheme)
        binding.alterEgoSplashScreenContainer.setBackgroundColor(alterEgoTheme.primaryColor)

        Handler().postDelayed(splashRunnable, Constants.SPLASH_DELAY)
    }

    companion object {

        fun newInstance(): AlterEgoSplashFragment {
            return AlterEgoSplashFragment()
        }

    }

}
