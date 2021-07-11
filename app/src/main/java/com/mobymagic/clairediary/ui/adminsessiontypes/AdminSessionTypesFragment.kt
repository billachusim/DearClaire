package com.mobymagic.clairediary.ui.adminsessiontypes

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.mobymagic.clairediary.MainActivity
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentAdminSessionTypesBinding
import com.mobymagic.clairediary.ui.alteregosplash.AlterEgoSplashFragment
import com.mobymagic.clairediary.ui.auth.AuthViewModel
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.common.PagerAdapter
import com.mobymagic.clairediary.ui.sessionlist.SessionListFragment
import com.mobymagic.clairediary.ui.sessionlist.SessionListType
import com.mobymagic.clairediary.ui.sessionshome.SessionsHomeFragment
import com.mobymagic.clairediary.util.FragmentUtils
import com.mobymagic.clairediary.vo.User
import org.koin.android.ext.android.inject
import timber.log.Timber

class AdminSessionTypesFragment : DataBoundNavFragment<FragmentAdminSessionTypesBinding>() {

    private val fragmentUtil: FragmentUtils by inject()
    private var prevMenuItem: MenuItem? = null
    private val authViewModel: AuthViewModel by inject()

    override fun getLayoutRes() = R.layout.fragment_admin_session_types

    override fun getPageTitle() = getString(R.string.common_title_alter_ego_mode)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = requireArguments().getString(ARG_USER_ID)
        if (userId != null) {
            setupPager(userId)
        }
        setupBottomNav()

        if (savedInstanceState == null) {
            Timber.d("Showing alter ego splash")
            fragmentUtil.addIfNotExist(
                childFragmentManager,
                R.id.alter_ego_session_splash_container,
                AlterEgoSplashFragment.newInstance()
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthViewModel.userLoggedIn) {
            authViewModel.getAuthRoute(
                SessionsHomeFragment.newInstance(
                    "",
                    User.UserType.ADMIN,
                    false,
                    R.id.nav_session_type_assigned
                ), activity as MainActivity
            )
        }
    }

    override fun onBackPressed(): Boolean {
        if (binding.sessionTypesPager.currentItem != 1) {
            binding.sessionTypesPager.currentItem = 1
            return true
        }

        return false
    }

    private fun setupBottomNav() {
        binding.sessionTypesTab.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_session_type_assigned -> {
                    binding.sessionTypesPager.setCurrentItem(0, true)
                }

                R.id.nav_session_type_non_assigned -> {
                    binding.sessionTypesPager.setCurrentItem(1, true)
                }
                R.id.nav_session_type_all -> {
                    binding.sessionTypesPager.setCurrentItem(2, true)
                }
            }

            true
        }
        binding.sessionTypesPager.addOnPageChangeListener(
            AdminPageChangeListener(
                authViewModel,
                activity
            )
        )
    }

    private fun setupPager(userId: String) {
        val sessionTypeItems = listOf(
            PagerAdapter.Item(
                getString(R.string.session_type_advised),
                SessionListFragment.newInstance(SessionListType.ASSIGNED, userId)
            ),
            PagerAdapter.Item(
                getString(R.string.session_type_new),
                SessionListFragment.newInstance(SessionListType.NON_ASSIGNED, userId)
            ),
            PagerAdapter.Item(
                getString(R.string.session_type_all),
                SessionListFragment.newInstance(SessionListType.ALL, userId)
            )
        )

        val pagerAdapter = PagerAdapter(childFragmentManager, sessionTypeItems)
        binding.sessionTypesPager.adapter = pagerAdapter

        binding.sessionTypesPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    prevMenuItem?.isChecked = false
                } else {
                    binding.sessionTypesTab.menu.getItem(0).isChecked = false
                }

                binding.sessionTypesTab.menu.getItem(position).isChecked = true
                prevMenuItem = binding.sessionTypesTab.menu.getItem(position)
            }

        })


    }

    companion object {

        private const val ARG_USER_ID = "ARG_USER_ID"

        fun newInstance(userId: String): AdminSessionTypesFragment {
            val sessionTypesFragment = AdminSessionTypesFragment()
            val arguments = Bundle()
            arguments.putString(ARG_USER_ID, userId)
            sessionTypesFragment.arguments = arguments
            return sessionTypesFragment
        }

    }

}
