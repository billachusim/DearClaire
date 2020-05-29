package com.mobymagic.clairediary.ui.ego


import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.mobymagic.clairediary.MainActivity
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentEgoBinding
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.ui.auth.AuthViewModel
import com.mobymagic.clairediary.ui.clairevartar.ClaireVartarFragment
import com.mobymagic.clairediary.ui.clairevartar.ClaireVartarFragment.Companion.USER_AVARTAR_KEY
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailFragment
import com.mobymagic.clairediary.ui.sessionlist.SessionListType
import com.mobymagic.clairediary.vo.Session
import com.mobymagic.clairediary.vo.Status
import com.mobymagic.clairediary.vo.User
import org.koin.android.ext.android.inject
import timber.log.Timber


/**
 * A simple [Fragment] subclass.
 * Use the [EgoFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class EgoFragment : DataBoundNavFragment<FragmentEgoBinding>() {


    private val userRepository: UserRepository by inject()
    private val egoViewModel: EgoViewModel by inject()
    private var user: User? = null
    private val authViewModel: AuthViewModel by inject()

    private val bestSession: Session = Session()

    private lateinit var userId: String

    private lateinit var mEgoPagerAdapter: EgoPagerAdapter


    override fun getPageTitle(): String {
        return "Ego"
    }

    override fun getLayoutRes() = R.layout.fragment_ego

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        userId = requireArguments().getString(ARG_USER_ID)
        egoViewModel.userId = userId
        if (userId == "") {
            authViewModel.authenticateForOpenViews(this, activity as MainActivity)
        } else {
            createClickListeners()
            createObservers()
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        egoViewModel.userId = userId
        user = userRepository.getLoggedInUser()
        binding.imageUrl = user?.avatarUrl
        binding.nickName = user?.nickname
        binding.userType = User.UserType.getNameFromUserType(user?.userType)
        mEgoPagerAdapter = EgoPagerAdapter(childFragmentManager, userId)
        binding.frag2Tabs.setupWithViewPager(binding.activityArchiveViewPager)
        binding.activityArchiveViewPager.adapter = mEgoPagerAdapter

    }

    companion object {

        val CLAIRE_VARTAR_REQUEST_CODE = 1000
        private const val ARG_USER_ID = "ARG_USER_ID"

        fun newInstance(
                userId: String
        ): EgoFragment {
            return EgoFragment().apply {
                val args = Bundle()
                args.putString(ARG_USER_ID, userId)
                arguments = args
            }
        }
    }


    // Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
    class EgoPagerAdapter(fm: androidx.fragment.app.FragmentManager, private val userId: String) : androidx.fragment.app.FragmentPagerAdapter(fm) {

        override fun getCount(): Int = 2

        override fun getItem(i: Int): Fragment {
            if (i == 0) {
                return EgoActivityFragment.newInstance(userId)
            } else if (i == 1) {
                return EgoArchiveFragment.newInstance(userId)
            }
            throw IllegalArgumentException("Unknown Tab")
        }

        override fun getPageTitle(position: Int): CharSequence {
            return getNameFromPosition(position)
        }

        private fun getNameFromPosition(position: Int): String {
            if (position == 0) {
                return "Activity"
            } else if (position == 1) {
                return "Archive"
            }
            throw IllegalArgumentException("Unknown Tab")
        }
    }

    private fun createObservers() {
        egoViewModel.initializeMediatorLiveData()

        egoViewModel.getUserSessionCount().observe(viewLifecycleOwner, Observer {
            if (it?.status?.equals(Status.SUCCESS)!!) {
                if (it.data?.size == null || it.data.isEmpty()) {
                    binding.numberOfSessions = "---"
                } else {
                    binding.numberOfSessions = it.data.get(0).numberOfSessions.toString()
                }

            } else if (it.status.equals(Status.ERROR) || it.status == Status.LOADING) {
                binding.numberOfSessions = "---"
            }
        })

        egoViewModel.getUserBestSession().observe(viewLifecycleOwner, Observer {
            if (it?.status?.equals(Status.LOADING)!!) {
                bestSession.message = "Loading Best Session"
                binding.bestSession = bestSession
                binding.bestSessionTextView.setOnClickListener {

                }
            } else if (it.status == Status.ERROR) {
                bestSession.message = "An error ocurred while loading Session," +
                        " please click to try agian"
                binding.bestSession = bestSession
                binding.bestSessionTextView.setOnClickListener {
                    egoViewModel.retryLoadingBestSession()
                }
            } else if (it.status.equals(Status.SUCCESS)) {
                if (it.data == null || it.data.isEmpty()) {
                    bestSession.message = "Unavailable"
                } else if (it.data.isNotEmpty()) {
                    val bestSession: Session = it.data.get(0)
                    binding.bestSession = bestSession
//                    binding.bestSessionLikeCount.text = it.data.get(0).followers.count()
                    binding.bestSessionTextView.setOnClickListener {

                        getNavController()
                                .navigate(SessionDetailFragment.newInstance(bestSession,
                                        userId, SessionListType.ARCHIVED), true)
                    }
                }

            }

        })

        egoViewModel.getNumberOfCommentsByUser(userId).observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.LOADING -> {
                    binding.numberOfComments = "---"
                }

                Status.SUCCESS -> {
                    if (it.data != null && it.data.isNotEmpty()) {
                        binding.numberOfComments = it.data.get(0).numberOfComments.toString()
                    } else {
                        binding.numberOfComments = "---"
                    }

                }
                Status.ERROR -> {
                    binding.numberOfComments = "---"
                }
            }
        })

        egoViewModel.getUserFollowCount().observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.LOADING -> {
                    binding.numberOfMeeToos = "---"
                }

                Status.SUCCESS -> {
                    if (it.data != null) {
                        binding.numberOfMeeToos = it.data.toString()
                    } else {
                        binding.numberOfMeeToos = "---"
                    }

                }
                Status.ERROR -> {
                    binding.numberOfMeeToos = "---"
                }
            }
        })
    }

    private fun createClickListeners() {
        val showClaireVartarDialog = View.OnClickListener {
            val fm = fragmentManager
            val claireVartarFragment = ClaireVartarFragment()
            claireVartarFragment.setTargetFragment(this, CLAIRE_VARTAR_REQUEST_CODE)
            if (fm != null) {
                claireVartarFragment.show(fm, "claireVartarFragment")
            }
        }
        binding.userAvartar.setOnClickListener(showClaireVartarDialog)
        binding.editUserAvartar.setOnClickListener(showClaireVartarDialog)

        binding.nicknameTextView.setOnClickListener {
            binding.nicknameViewSwitcher.showNext()
        }
        binding.editUserNickname.setOnClickListener {
            binding.nicknameViewSwitcher.showNext()
        }
        binding.saveNicknameButton.setOnClickListener { it ->
            if (validateUsernameInput()) {
                if (user != null) {
                    user?.nickname = binding.editNicknameInput.text.toString()
                    userRepository.updateUser(user!!).observe(viewLifecycleOwner, Observer {
                        if (it?.status?.equals(Status.SUCCESS)!!) {
                            Toast.makeText(activity,
                                    "Nick name saved successfully",
                                    Toast.LENGTH_SHORT).show()
                            refreshUser()
                        } else if (it.status == Status.ERROR) {
                            Toast.makeText(activity,
                                    "An error Occurred while saving your nickname",
                                    Toast.LENGTH_SHORT).show()
                        }
                        binding.nicknameViewSwitcher.showPrevious()
                    })
                }

                binding.nicknameViewSwitcher.showPrevious()
            }

            binding.cancelNicknameButton.setOnClickListener {
                binding.nicknameViewSwitcher.showPrevious()
            }


        }
    }

    private fun refreshUser() {
        user = userRepository.getLoggedInUser()
        binding.nickName = user?.nickname
        binding.imageUrl = user?.avatarUrl
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CLAIRE_VARTAR_REQUEST_CODE) {
            val userAvartar = data?.getStringExtra(USER_AVARTAR_KEY)
            if (!TextUtils.isEmpty(userAvartar)) {
                // I dont think there is any way the user can be null but just check
                if (user != null) {
                    user?.avatarUrl = userAvartar
                    userRepository.updateUser(user!!).observe(this, Observer {
                        if (it?.status?.equals(Status.SUCCESS)!!) {
                            Toast.makeText(activity,
                                    "Clairevartar saved successfully",
                                    Toast.LENGTH_SHORT).show()
                            refreshUser()
                        } else if (it.status.equals(Status.ERROR)) {
                            Toast.makeText(activity,
                                    "An error Occured while saving your clairevartar",
                                    Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }
    }

    private fun validateUsernameInput(): Boolean {
        val username = binding.editNicknameInput.text.toString()
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(context, "You username cannot contain be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        if (username.contains("claire", false)) {
            Toast.makeText(context, "You username cannot contain claire", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}