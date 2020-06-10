package com.mobymagic.clairediary.ui.sessionshome

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.navigation.NavigationView
import com.mobymagic.clairediary.Constants
import com.mobymagic.clairediary.MainActivity
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentSessionsHomeBinding
import com.mobymagic.clairediary.ui.adminsessiontypes.AdminSessionTypesFragment
import com.mobymagic.clairediary.ui.alteregologin.AlterEgoLoginFragment
import com.mobymagic.clairediary.ui.auth.AuthViewModel
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.common.NavFragment
import com.mobymagic.clairediary.ui.createsession.CreateSessionFragment
import com.mobymagic.clairediary.ui.help.HelpFragment
import com.mobymagic.clairediary.ui.settings.SettingsHomeFragment
import com.mobymagic.clairediary.ui.usersessiontypes.UserSessionTypesFragment
import com.mobymagic.clairediary.util.*
import com.mobymagic.clairediary.vo.User
import hotchemi.android.rate.AppRate
import io.multimoon.colorful.Colorful
import kotlinx.android.synthetic.main.layout_app_bar.*
import kotlinx.android.synthetic.main.layout_nav_header_session_home.view.*
import org.koin.android.ext.android.inject
import timber.log.Timber


class SessionsHomeFragment : DataBoundNavFragment<FragmentSessionsHomeBinding>(),
        NavigationView.OnNavigationItemSelectedListener {

    override var requiresAuthentication: Boolean = false
    override var openToNewUsers: Boolean = true

    private val fragmentUtil: FragmentUtils by inject()
    private val androidUtil: AndroidUtil by inject()
    private val themeUtil: ThemeUtil by inject()
    private val prefUtil: PrefUtil by inject()
    private val authViewModel: AuthViewModel by inject()

    private lateinit var userId: String
    private lateinit var userType: User.UserType

    private var selectedPage: Int? = null

    override fun getLayoutRes() = R.layout.fragment_sessions_home

    override fun getPageTitle() = getString(R.string.app_name)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.hide()

        userId = requireArguments().getString(ARG_USER_ID).toString()

        if (TextUtils.isEmpty(userId)) {
            userId = authViewModel.getUserId()
        }

//        userType = arguments!!.getSerializable(ARG_USER_TYPE) as User.UserType
        userType = authViewModel.getUserType()


        setupNavDrawer()
        toggleSessionTypesFragment(false)
        setupButtons()
        setupAppRateDialog()
    }

    private fun setupNavDrawer() {
        val toggle = ActionBarDrawerToggle(
                activity,
                binding.drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )

        binding.sessionsHomeNavView.setNavigationItemSelectedListener(this)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }


    /**
     * This functions toggles between UserSessionTypesFragment and AdminSessionTypesFragment based
     * on the current state.
     * If no fragment is being shown when this function is called, the UserSessionTypesFragment is
     * displayed to the user.
     * If this function is called and the current fragment is UserSessionTypesFragment and the user
     * is an admin, show AdminSessionTypesFragment.
     * Else show UserSessionTypesFragment
     */
    private fun toggleSessionTypesFragment(clickedOnButton: Boolean) {
        Timber.d("Toggle session types")
        val curFragment =
                childFragmentManager.findFragmentById(R.id.sessions_home_fragment_container)

        if (curFragment != null && !clickedOnButton) {
            return
        }

        Timber.d("Actually toggling session types")

        var sessionTypesFragment: androidx.fragment.app.Fragment = UserSessionTypesFragment.newInstance(userId, selectedPage!!)

        val primaryColor = Colorful().getPrimaryColor().getColorPack().normal().asInt()
        binding.appBar.toolbar.setBackgroundColor(primaryColor)

        // Check the user type and current fragment
        if (curFragment is UserSessionTypesFragment && User.UserType.isAdmin(userType)) {
            val themes = resources.getStringArray(R.array.settings_theme_names)
            val curTheme = prefUtil.getString(Constants.PREF_THEME, null) ?: themes[0]
            val alterEgoTheme = themeUtil.getAlterEgoTheme(curTheme)
            binding.appBar.toolbar.setBackgroundColor(alterEgoTheme.primaryColor)
            sessionTypesFragment = AdminSessionTypesFragment.newInstance(userId)

            if (!AuthViewModel.userLoggedIn) {
                authViewModel.getAuthRoute(newInstance(
                        "",
                        User.UserType.ADMIN,
                        false,
                        R.id.nav_session_type_assigned
                ), activity as MainActivity)
            } else {
                fragmentUtil.replace(
                        childFragmentManager,
                        R.id.sessions_home_fragment_container,
                        sessionTypesFragment,
                        true
                )
            }
        } else {
            fragmentUtil.replace(
                    childFragmentManager,
                    R.id.sessions_home_fragment_container,
                    sessionTypesFragment,
                    true
            )
        }


    }

    private fun setupButtons() {
        binding.sessionsHomeFab.setOnClickListener {
            val createSessionFragment = CreateSessionFragment.newInstance()
            getNavController().navigate(createSessionFragment, true)
        }

        binding.sessionsHomeNavView.getHeaderView(0).sessions_home_app_logo.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            if (User.UserType.isAdmin(userType)) {
                toggleSessionTypesFragment(true)
            } else {
                val frag = AlterEgoLoginFragment.newInstance(userId)
                getNavController().navigate(frag, true)
            }
        }

        binding.sessionsHomeNavView.getHeaderView(0).sessions_home_app_logo.setSpin(true)
    }

    override fun onBackPressed(): Boolean {
        val curFragment =
                childFragmentManager.findFragmentById(R.id.sessions_home_fragment_container) as NavFragment
        return if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        } else {
            return curFragment.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_share -> {
                val appName = getString(R.string.app_name)
                androidUtil.shareText(
                        requireContext(), appName,
                        getString(R.string.common_message_share_app, appName, Constants.STORE_URL)
                )
            }
            R.id.nav_how_to_use -> {
                showHelpPage()
            }
            R.id.nav_join_conv_on_instagram -> {
                androidUtil.openUrl(requireContext(), Constants.INSTAGRAM_PAGE_URL)
            }
            R.id.nav_settings -> {
                val settingsFragment = SettingsHomeFragment.newInstance()
                getNavController().navigate(settingsFragment, true)
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showHelpPage() {
        val helpFragment = HelpFragment.newInstance(userId)
        getNavController().navigate(helpFragment, true)
    }

    private fun setupAppRateDialog() {
        AppRate.with(context)
                .setInstallDays(3)
                .setRemindInterval(15)
                .monitor()

        AppRate.showRateDialogIfMeetsConditions(activity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(alterEgoReceiver,
                IntentFilter(Constants.EVENT_OPEN_ALTER_EGO)
        )
        selectedPage = arguments?.getInt(PAGE_SELECTED)
    }

    override fun onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(alterEgoReceiver)
        super.onDestroy()
    }

    private val alterEgoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            toggleSessionTypesFragment(true)
        }
    }

    companion object {

        private const val ARG_USER_ID = "ARG_USER_ID"
        private const val ARG_USER_TYPE = "ARG_USER_TYPE"
        private const val USER_LOGGED_IN = "USER_LOGGED_IN"
        private const val PAGE_SELECTED = "PAGE_SELECTED"


        fun newInstance(userId: String, userType: User.UserType, userLoggedIn: Boolean,
                        selectedPageId: Int?): SessionsHomeFragment {
            val sessionsHomeFragment = SessionsHomeFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            args.putSerializable(ARG_USER_TYPE, userType)
            if (selectedPageId != null) {
                args.putInt(PAGE_SELECTED, selectedPageId)
            }
            sessionsHomeFragment.arguments = args
            args.putSerializable(USER_LOGGED_IN, userLoggedIn)
            return sessionsHomeFragment
        }

    }

}
