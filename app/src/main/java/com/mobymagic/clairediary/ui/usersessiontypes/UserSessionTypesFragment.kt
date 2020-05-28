package com.mobymagic.clairediary.ui.usersessiontypes

import android.os.Bundle
import android.view.MenuItem
import androidx.viewpager.widget.ViewPager
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentUserSessionTypesBinding
import com.mobymagic.clairediary.ui.auth.AuthViewModel
import com.mobymagic.clairediary.ui.chat.fragments.ChatFragment
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.common.PagerAdapter
import com.mobymagic.clairediary.ui.ego.EgoFragment
import com.mobymagic.clairediary.ui.sessionlist.SessionListFragment
import com.mobymagic.clairediary.ui.sessionlist.SessionListType
import org.koin.android.ext.android.inject

class UserSessionTypesFragment : DataBoundNavFragment<FragmentUserSessionTypesBinding>() {

    private var prevMenuItem: MenuItem? = null

    override fun getLayoutRes() = R.layout.fragment_user_session_types

    override fun getPageTitle(): Nothing? = null

    private val authViewModel: AuthViewModel by inject()

    private var selectedPage: Int? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val userId = requireArguments().getString(ARG_USER_ID)
        setupPager(userId)
        setupBottomNav()
        selectedPage = arguments?.getInt(PAGE_SELECTED)
        if (selectedPage != null) {
            binding.sessionTypesTab.selectedItemId = selectedPage!!
        }
    }

    override fun onBackPressed(): Boolean {
        if (binding.sessionTypesPager.currentItem != 0) {
            binding.sessionTypesPager.currentItem = 0
            return true
        }

        return false
    }

    private fun setupBottomNav() {
        binding.sessionTypesPager.addOnPageChangeListener(UserTypeSessionOnPageChangeListener(authViewModel, context, binding.sessionTypesPager))
        binding.sessionTypesTab.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_session_type_featured -> {
                    binding.sessionTypesPager.setCurrentItem(0, true)
                }
                R.id.nav_session_type_following -> {

                    binding.sessionTypesPager.setCurrentItem(1, true)
                }
                R.id.nav_session_type_diary -> {
                    binding.sessionTypesPager.setCurrentItem(2, true)
                }

                R.id.nav_session_type_chat -> {
                    binding.sessionTypesPager.setCurrentItem(3, true)
                }

                R.id.nav_session_type_archived -> {

                    binding.sessionTypesPager.setCurrentItem(4, true)
                }

            }

            true
        }

    }

    private fun setupPager(userId: String) {
        val sessionTypeItems = listOf(
                PagerAdapter.Item(
                        getString(R.string.session_type_assigned),
                        SessionListFragment.newInstance(SessionListType.TRENDING, userId)
                ),
                PagerAdapter.Item(
                        getString(R.string.session_type_following),
                        SessionListFragment.newInstance(SessionListType.FOLLOWING, userId)
                ),
                PagerAdapter.Item(
                        getString(R.string.session_type_diary),
                        SessionListFragment.newInstance(SessionListType.DIARY, userId)
                ),

                PagerAdapter.Item(
                        getString(R.string.session_type_chat),
                        ChatFragment.newInstance(userId)
                ),

                PagerAdapter.Item(
                        getString(R.string.session_type_archived),
                        EgoFragment.newInstance(userId)
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
        private const val PAGE_SELECTED = "PAGE_SELECTED"

        fun newInstance(userId: String, selectedPageId: Int): UserSessionTypesFragment {
            val sessionTypesFragment = UserSessionTypesFragment()
            val arguments = Bundle()
            arguments.putString(ARG_USER_ID, userId)
            arguments.putInt(PAGE_SELECTED, selectedPageId)
            sessionTypesFragment.arguments = arguments
            return sessionTypesFragment
        }


    }

}
