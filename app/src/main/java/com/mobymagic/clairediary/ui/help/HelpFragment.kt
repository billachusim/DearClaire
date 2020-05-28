package com.mobymagic.clairediary.ui.help

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentHelpBinding
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.ui.alteregointro.AlterEgoIntroFragment
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.util.DonateUtil
import kotlinx.android.synthetic.main.layout_app_bar.*
import org.koin.android.ext.android.inject

class HelpFragment : DataBoundNavFragment<FragmentHelpBinding>() {

    override var requiresAuthentication: Boolean = false
    override var openToNewUsers: Boolean = true

    private val androidUtil: AndroidUtil by inject()
    private val donateUtil: DonateUtil by inject()
    private val userRepository: UserRepository by inject()

    override fun getLayoutRes() = R.layout.fragment_help

    override fun getPageTitle() = getString(R.string.how_claire_works_page_title)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val userId = arguments!!.getString(ARG_USER_ID)

        binding.howAlterEgoWorksCard.setOnClickListener {
            val alterEgoIntro = AlterEgoIntroFragment.newInstance(userId)
            getNavController().navigate(alterEgoIntro, true)
        }

        binding.howClaireWorksCard.setOnClickListener {
            if (binding.howClaireWorksShortText.visibility == View.VISIBLE) {
                binding.howClaireWorksLongText.visibility = View.VISIBLE
                binding.howClaireWorksShortText.visibility = View.GONE
            } else {
                binding.howClaireWorksLongText.visibility = View.GONE
                binding.howClaireWorksShortText.visibility = View.VISIBLE
            }
        }

        binding.donateSupportClaireCard.setOnClickListener {
            val curLoggedInUser = userRepository.getLoggedInUser()
            donateUtil.donate(activity!!, curLoggedInUser!!)
        }

        binding.feedbackCard.setOnClickListener {
            androidUtil.sendMail(context!!, "", "thesocialfaculty@gmail.com")
        }
    }

    companion object {

        private const val ARG_USER_ID = "ARG_USER_ID"

        fun newInstance(userId: String): HelpFragment {
            val helpFragment = HelpFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            helpFragment.arguments = args
            return helpFragment
        }

    }

}