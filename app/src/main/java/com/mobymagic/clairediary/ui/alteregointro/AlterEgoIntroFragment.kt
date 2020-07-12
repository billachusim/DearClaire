package com.mobymagic.clairediary.ui.alteregointro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cleveroad.splittransformation.TransformationAdapterWrapper
import com.mobymagic.clairediary.Constants
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentAlterEgoIntroBinding
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.ui.alteregoorientation.AlterEgoOrientationFragment
import com.mobymagic.clairediary.ui.alteregosplash.AlterEgoSplashFragment
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.common.PagerAdapter
import com.mobymagic.clairediary.util.DonateUtil
import com.mobymagic.clairediary.util.PrefUtil
import com.mobymagic.clairediary.util.ThemeUtil
import kotlinx.android.synthetic.main.layout_app_bar.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class AlterEgoIntroFragment : DataBoundNavFragment<FragmentAlterEgoIntroBinding>() {

    private val donateUtil: DonateUtil by inject()
    private val userRepository: UserRepository by inject()
    private val themeUtil: ThemeUtil by inject()
    private val prefUtil: PrefUtil by inject()
    private lateinit var userId: String

    override fun getLayoutRes() = R.layout.fragment_alter_ego_intro

    override fun getPageTitle() = getString(R.string.common_title_alter_ego_mode)

    override fun isAlterEgoPage() = true

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val themes = resources.getStringArray(R.array.settings_theme_names)
        val curTheme = prefUtil.getString(Constants.PREF_THEME, null) ?: themes[0]
        val alterEgoTheme = themeUtil.getAlterEgoTheme(curTheme)
        binding.appBar.toolbar.setBackgroundColor(alterEgoTheme.primaryColor)
        activity?.setTitle(R.string.common_title_alter_ego_mode)
        userId = requireArguments().getString(ARG_USER_ID, null)
        setupPager()
        setupAccessCodeCheck()

        binding.alterEgoIntroDonateButton.setOnClickListener { onDonateClicked() }
        binding.alterEgoIntroRequestAccessButton.setOnClickListener { onRequestAccessClicked() }
    }

    private fun setupPager() {
        val pages = getPages()
        val slideAdapter = PagerAdapter(childFragmentManager, pages)

        val wrapper = TransformationAdapterWrapper.wrap(requireContext(), slideAdapter).build()
        binding.alterEgoIntroPager.adapter = wrapper
        binding.alterEgoIntroPager.setPageTransformer(false, wrapper)

        binding.alterEgoIntroPageIndicator.initializeWith(binding.alterEgoIntroPager)
    }

    private fun setupAccessCodeCheck() {
        userRepository.getLoggedInUser()
        /*if (prefUtil.getBool(Constants.PREF_KEY_COMPLETED_ALTER_EGO_ORIENTATION, false) &&
            User.UserType.isAdmin(curLoggedInUser?.userType)
        ) {
            getNavController().remove(this)
        } else if (prefUtil.getBool(Constants.PREF_KEY_ALTER_EGO_HAS_DONATED, false)) {
            getNavController().navigate(AlterEgoOrientationFragment.newInstance(userId), true)
            getNavController().remove(this)
        }*/
    }

    private fun getPages(): List<PagerAdapter.Item> {
        return listOf(
                PagerAdapter.Item(
                        null,
                        AlterEgoIntroSlideFragment.newInstance(
                                R.drawable.alter_ego_slide_1,
                                R.string.alter_ego_intro_slide_1_text
                        )
                ),
                PagerAdapter.Item(
                        null,
                        AlterEgoIntroSlideFragment.newInstance(
                                R.drawable.alter_ego_slide_2,
                                R.string.alter_ego_intro_slide_2_text
                        )
                ),
                PagerAdapter.Item(
                        null,
                        AlterEgoIntroSlideFragment.newInstance(
                                R.drawable.alter_ego_slide_3,
                                R.string.alter_ego_intro_slide_3_text
                        )
                ),
                PagerAdapter.Item(
                        null,
                        AlterEgoIntroSlideFragment.newInstance(
                                R.drawable.alter_ego_slide_4,
                                R.string.alter_ego_intro_slide_4_text
                        )
                )
        )
    }

    private fun onDonateClicked() {
        val curLoggedInUser = userRepository.getLoggedInUser()
        donateUtil.donate(requireActivity(), curLoggedInUser!!)
    }

    private fun onRequestAccessClicked() {
        val alterEgoOrientation = AlterEgoOrientationFragment.newInstance(userId)
        getNavController().navigate(alterEgoOrientation, true)
    }

    override fun onBackPressed(): Boolean {
        val nextFragment = AlterEgoSplashFragment.newInstance()
        getNavController().navigate(nextFragment, false)
        return if (nextFragment != nextFragment) {
            Timber.d("Current fragment handled the back button, ignoring")
            return false
        } else {
            true
        }
    }

    companion object {

        private const val ARG_USER_ID = "ARG_USER_ID"

        fun newInstance(userId: String): AlterEgoIntroFragment {
            return AlterEgoIntroFragment().apply {
                val args = Bundle()
                args.putString(ARG_USER_ID, userId)
                arguments = args
            }
        }
    }
}
