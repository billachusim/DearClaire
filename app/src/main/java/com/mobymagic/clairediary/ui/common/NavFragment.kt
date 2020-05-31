package com.mobymagic.clairediary.ui.common

import android.content.Intent
import timber.log.Timber
import java.io.Serializable

abstract class NavFragment : androidx.fragment.app.Fragment(), Serializable {

    /**
     * can this fragment be accessed without authentication?
     */
    open val requiresAuthentication = true

    /**
     * can new users access this fragment
     */
    open val openToNewUsers = false

    override fun onResume() {
        super.onResume()
        updatePageTitle()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        updatePageTitle()
    }

    private fun updatePageTitle() {
        if (isAdded) {
            val title = getPageTitle()
            if (title != null && userVisibleHint) {
                Timber.d("Page title: %s", title)
                requireActivity().title = title
            }
        }
    }

    /**
     * Called when the phone back button is pressed. Navigation fragments that want to perform
     * an action when the back button is pressed should override this method and return true.
     * The default implementation returns false
     * @return true if the back button pressed was handled by the fragment, false otherwise
     */
    open fun onBackPressed() = false

    open fun onResult(requestCode: Int, resultCode: Int, data: Intent?) = false

    fun getNavController(): NavController = (activity as NavController)

    abstract fun getPageTitle(): String?

    open fun isAlterEgoPage() = false

}