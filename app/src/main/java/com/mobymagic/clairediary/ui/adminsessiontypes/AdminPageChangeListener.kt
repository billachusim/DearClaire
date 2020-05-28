package com.mobymagic.clairediary.ui.adminsessiontypes

import android.content.Context
import androidx.viewpager.widget.ViewPager
import com.mobymagic.clairediary.MainActivity
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.ui.auth.AuthViewModel
import com.mobymagic.clairediary.ui.sessionshome.SessionsHomeFragment
import com.mobymagic.clairediary.vo.User

class AdminPageChangeListener(val authViewModel: AuthViewModel,
                              val activity: Context?) : ViewPager.OnPageChangeListener {

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        when (position) {
            0 -> {
                if (!AuthViewModel.userLoggedIn) {
                    authViewModel.getAuthRoute(SessionsHomeFragment.newInstance(
                            "",
                            User.UserType.ADMIN,
                            false,
                            R.id.nav_session_type_assigned
                    ), activity as MainActivity)
                }
            }

            1 -> {
                if (!AuthViewModel.userLoggedIn) {
                    authViewModel.getAuthRoute(SessionsHomeFragment.newInstance(
                            "",
                            User.UserType.ADMIN,
                            false,
                            R.id.nav_session_type_non_assigned
                    ), activity as MainActivity)
                }
            }
            2 -> {
                if (!AuthViewModel.userLoggedIn) {
                    authViewModel.getAuthRoute(SessionsHomeFragment.newInstance(
                            "",
                            User.UserType.ADMIN,
                            false,
                            R.id.nav_session_type_all
                    ), activity as MainActivity)
                }
            }
        }
    }
}