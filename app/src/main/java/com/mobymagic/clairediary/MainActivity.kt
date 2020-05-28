package com.mobymagic.clairediary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.mobymagic.clairediary.ui.auth.AuthViewModel
import com.mobymagic.clairediary.ui.common.NavController
import com.mobymagic.clairediary.ui.common.NavFragment
import com.mobymagic.clairediary.ui.createsession.CreateSessionFragment
import com.mobymagic.clairediary.ui.loadsession.LoadSessionFragment
import com.mobymagic.clairediary.ui.splash.SplashFragment
import com.mobymagic.clairediary.util.*
import io.fabric.sdk.android.Fabric
import io.multimoon.colorful.CAppCompatActivity
import io.multimoon.colorful.Colorful
import org.koin.android.ext.android.inject
import timber.log.Timber


/**
 * Single Activity in app
 */
class MainActivity : CAppCompatActivity(), NavController,
        androidx.fragment.app.FragmentManager.OnBackStackChangedListener {


    private val fragmentUtil: FragmentUtils by inject()
    private val themeUtil: ThemeUtil by inject()
    private val prefUtil: PrefUtil by inject()
    private var mIsResumed: Boolean = false
    private val authViewModel: AuthViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_main)
        val fontRequest: FontRequest = FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs_prod)
        EmojiCompat.init(FontRequestEmojiCompatConfig(applicationContext, fontRequest)
                .setReplaceAll(true)
                .registerInitCallback(EmojiInitCallBack()))
//        val config = BundledEmojiCompatConfig(this)
//        EmojiCompat.init(config)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        Answers.getInstance().logCustom(CustomEvent("App Opened"))
        openStartDestination()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleStartIntent()
    }

    override fun onResume() {
        super.onResume()
        mIsResumed = true
        handleStartIntent()
        supportFragmentManager.addOnBackStackChangedListener(this)
    }

    override fun onPause() {
        super.onPause()
        mIsResumed = false
    }

    private fun openStartDestination() {
        navigate(SplashFragment.newInstance(), false, true)
    }

    override fun clearBackStack() {
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun navigate(
            destination: NavFragment,
            addToBackStack: Boolean,
            onlyAddIfNotExist: Boolean,
            fragmentAnimation: FragmentUtils.FragmentAnimation
    ) {
        if (!destination.requiresAuthentication || AuthViewModel.userLoggedIn) {

            if (!destination.openToNewUsers && !authViewModel.isUserAvailable()) {
                authViewModel.authenticateForOpenViews(destination, this)
            } else {
                Timber.d("Navigating to: %s", destination)
                fragmentUtil.replace(
                        supportFragmentManager,
                        R.id.main_fragment_container,
                        destination,
                        addToBackStack,
                        onlyAddIfNotExist,
                        fragmentAnimation
                )
            }
        } else {
            authViewModel.getAuthRoute(destination, this)
        }
    }

    override fun navigateTo(destination: NavFragment) {
        Timber.d("Navigating to: %s", destination)
        fragmentUtil.add(
                supportFragmentManager,
                R.id.main_fragment_container,
                destination,
                true
        )
    }

    override fun navigateToWithAuth(destination: NavFragment) {
        if (!destination.requiresAuthentication || AuthViewModel.userLoggedIn) {

            if (!destination.openToNewUsers && !authViewModel.isUserAvailable()) {
                authViewModel.authenticateForOpenViews(destination, this)
            } else {
                Timber.d("Navigating to: %s", destination)
                fragmentUtil.add(
                        supportFragmentManager,
                        R.id.main_fragment_container,
                        destination,
                        true
                )
            }
        } else {
            authViewModel.getAuthRoute(destination, this)
        }
    }

    override fun remove(navFragment: NavFragment) {
        if (navFragment.isAdded) {
            supportFragmentManager.beginTransaction().remove(navFragment).commitAllowingStateLoss()
        }
    }

    override fun removeFromBackstack(destination: NavFragment) {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onBackPressed() {
        val curFragment = supportFragmentManager.findFragmentById(R.id.main_fragment_container)
        if (curFragment != null && curFragment is NavFragment) {
            if (curFragment.onBackPressed()) {
                Timber.d("Current fragment handled the back button, ignoring")
                return
            }
        }

        Timber.d("Back button wasn't handled by fragment, using default activity implementation")
        return super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val curFragment = supportFragmentManager.findFragmentById(R.id.main_fragment_container)
        if (curFragment != null && curFragment is NavFragment) {
            curFragment.onResult(requestCode, resultCode, data)
        }
    }

    override fun onBackStackChanged() {
        val curFragment = supportFragmentManager.findFragmentById(R.id.main_fragment_container)
        if (curFragment != null && curFragment is NavFragment) {
            if (curFragment.isAlterEgoPage()) {
                val themes = resources.getStringArray(R.array.settings_theme_names)
                val curTheme = prefUtil.getString(Constants.PREF_THEME, null) ?: themes[0]
                val alterEgoTheme = themeUtil.getAlterEgoTheme(curTheme)
                if (VersionUtil.hasLollipop()) {
                    window.statusBarColor = alterEgoTheme.primaryDarkColor
                }
            } else {
                val primaryColorDark = Colorful().getPrimaryColor().getColorPack().dark().asInt()
                if (VersionUtil.hasLollipop()) {
                    window.statusBarColor = primaryColorDark
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun handleStartIntent() {
        if (!mIsResumed) {
            // we are called from onResume, so defer until then
            return
        }

        when {
            intent.hasExtra(EXTRA_SESSION_ID) -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
                val userId = intent.getStringExtra(EXTRA_USER_ID)
                val isAlterEgo = intent.getStringExtra(IS_ALTER_EGO)
                val loadSessionFragment = LoadSessionFragment.newInstance(userId, sessionId, isAlterEgo)
                navigate(loadSessionFragment, true)
            }
            intent.hasExtra(EXTRA_NAVIGATE_TO_CREATE_SESSION) -> {
                val createSessionFragment = CreateSessionFragment.newInstance()
                navigate(createSessionFragment, true)
            }
        }

        intent.removeExtra(EXTRA_SESSION_ID)
        intent.removeExtra(EXTRA_NAVIGATE_TO_CREATE_SESSION)
        intent = intent
    }

    companion object {

        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
        const val IS_ALTER_EGO = "IS_ALTER_EGO"
        const val EXTRA_NAVIGATE_TO_CREATE_SESSION = "EXTRA_NAVIGATE_TO_CREATE_SESSION"

        fun getStartIntent(context: Context) = Intent(context, MainActivity::class.java)

    }
}

