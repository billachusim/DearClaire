package com.mobymagic.clairediary.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.ui.common.NavFragment
import com.mobymagic.clairediary.util.FragmentUtils
import kotlinx.android.synthetic.main.layout_app_bar.*
import org.koin.android.ext.android.inject

class SettingsHomeFragment : NavFragment() {

    override var requiresAuthentication: Boolean = false
    override var openToNewUsers: Boolean = true

    override fun getPageTitle() = getString(R.string.settings_page_title)

    private val fragmentUtil: FragmentUtils by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val settingsFragment = SettingsFragment.newInstance()
        fragmentUtil.addIfNotExist(
            childFragmentManager,
            R.id.settings_fragment_container,
            settingsFragment
        )
    }

    companion object {

        fun newInstance(): SettingsHomeFragment {
            return SettingsHomeFragment()
        }

    }
}