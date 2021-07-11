package com.mobymagic.clairediary.ui.usersessiontypes

import android.content.Context
import androidx.viewpager.widget.ViewPager
import com.mobymagic.clairediary.MainActivity
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.ui.auth.AuthViewModel
import com.mobymagic.clairediary.ui.sessionshome.SessionsHomeFragment
import com.mobymagic.clairediary.vo.User

class UserTypeSessionOnPageChangeListener(
    val authViewModel: AuthViewModel,
    val activity: Context?, val pager: ViewPager
) : ViewPager.OnPageChangeListener {

    override fun onPageScrollStateChanged(state: Int) {
        if (state == ViewPager.SCROLL_STATE_SETTLING) {

        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        when (position) {

            0 -> {
                pager.setCurrentItem(0, true)
            }
            1 -> {
                if (!AuthViewModel.userLoggedIn) {
                    pager.setCurrentItem(0, true)
                    authViewModel.getAuthRoute(
                        SessionsHomeFragment.newInstance(
                            "",
                            User.UserType.REGULAR,
                            false,
                            R.id.nav_session_type_following
                        ), activity as MainActivity
                    )
                } else {
                    pager.setCurrentItem(1, true)
                }
            }
            2 -> {
                if (!AuthViewModel.userLoggedIn) {
                    pager.setCurrentItem(0, true)
                    authViewModel.getAuthRoute(
                        SessionsHomeFragment.newInstance(
                            "",
                            User.UserType.REGULAR,
                            false,
                            R.id.nav_session_type_diary
                        ), activity as MainActivity
                    )
                } else {
                    pager.setCurrentItem(2, true)
                }
            }

            3 -> {
                if (!AuthViewModel.userLoggedIn) {
                    pager.setCurrentItem(0, true)
                    authViewModel.getAuthRoute(
                        SessionsHomeFragment.newInstance(
                            "",
                            User.UserType.REGULAR,
                            false,
                            R.id.nav_session_type_chatrooms
                        ), activity as MainActivity
                    )
                } else {
                    pager.setCurrentItem(3, true)
                }
            }

            4 -> {
                if (!AuthViewModel.userLoggedIn) {
                    pager.setCurrentItem(0, true)
                    authViewModel.getAuthRoute(
                        SessionsHomeFragment.newInstance(
                            "",
                            User.UserType.REGULAR,
                            false,
                            R.id.nav_session_type_ego
                        ), activity as MainActivity
                    )
                } else {
                    pager.setCurrentItem(4, true)
                }
            }
        }
    }
}