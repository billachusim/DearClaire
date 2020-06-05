package com.mobymagic.clairediary.ui.geustego


import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.MainActivity
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentGuestEgoBinding
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.ui.auth.AuthViewModel
import com.mobymagic.clairediary.ui.clairevartar.ClaireVartarFragment
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.common.EmptyCallback
import com.mobymagic.clairediary.ui.common.RetryCallback
import com.mobymagic.clairediary.ui.ego.EgoViewModel
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailFragment
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailImageAdapter
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailViewModel
import com.mobymagic.clairediary.ui.sessionlist.SessionListType
import com.mobymagic.clairediary.util.AudioUtil
import com.mobymagic.clairediary.util.autoCleared
import com.mobymagic.clairediary.vo.Empty
import com.mobymagic.clairediary.vo.Session
import com.mobymagic.clairediary.vo.Status
import com.mobymagic.clairediary.vo.User
import kotlinx.android.synthetic.main.layout_app_bar.*
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * A simple Fragment subclass.
 * Use the EgoFragment.newInstance factory method to
 * create an instance of this fragment.
 *
 */
class GuestEgoFragment : DataBoundNavFragment<FragmentGuestEgoBinding>() {


    override var requiresAuthentication: Boolean = false

    private val appExecutors: AppExecutors by inject()
    private val userRepository: UserRepository by inject()
    private val egoViewModel: EgoViewModel by inject()
    private val guestEgoViewModel: GuestEgoViewModel by inject()
    private var user: User? = null
    private val authViewModel: AuthViewModel by inject()
    private val sessionDetailViewModel: SessionDetailViewModel by inject()
    private val audioUtil: AudioUtil by inject()
    private var sessionListImageAdapter by autoCleared<SessionDetailImageAdapter>()

    private var adapter by autoCleared<GuestEgoSessionListAdapter>()


    private val bestSession: Session = Session()

    private lateinit var sessionListType: SessionListType
    private lateinit var userId: String
    private lateinit var nickname: String
    private lateinit var avatar: String

    override fun getPageTitle(): String {
        return nickname
    }

    override fun getLayoutRes() = R.layout.fragment_guest_ego

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        userId = requireArguments().getString(ARG_USER_ID).toString()
        nickname = requireArguments().getString(ARG_NICK_NAME).toString()
        avatar = requireArguments().getString(ARG_AVATAR).toString()
        sessionListType = requireArguments().getSerializable(ARG_SESSION_LIST_TYPE) as SessionListType
        user = userRepository.getLoggedInUser()
        egoViewModel.userId = userId
        if (userId == "") {
            authViewModel.authenticateForOpenViews(this, activity as MainActivity)
        } else {
            createClickListeners()
            createObservers()
            initRecyclerViewAdapter()
            setupListeners()
            observeSessions()
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        Answers().logCustom(CustomEvent("Guest Profile Opened"))

        egoViewModel.userId = userId
        user = userRepository.getLoggedInUser()
        binding.imageUrl = avatar
        binding.nickName = nickname
        binding.userType = User.UserType.getNameFromUserType(user?.userType)


    }

    companion object {

        const val CLAIRE_VARTAR_REQUEST_CODE = 1000
        private const val ARG_USER_ID = "ARG_USER_ID"
        private const val ARG_GEUST_ID = "ARG_GEUST_ID"
        private const val ARG_NICK_NAME = "ARG_NICK_NAME"
        private const val ARG_AVATAR = "ARG_AVATAR"
        private const val ARG_SESSION_LIST_TYPE = "ARG_SESSION_LIST_TYPE"

        fun newInstance(
                userId: String,
                guestId: String,
                nickname: String,
                avatar: String,
                sessionListType: SessionListType
        ): GuestEgoFragment {
            return GuestEgoFragment().apply {
                val args = Bundle()
                args.putString(ARG_USER_ID, userId)
                args.putString(ARG_GEUST_ID, guestId)
                args.putString(ARG_NICK_NAME, nickname)
                args.putString(ARG_AVATAR, avatar)
                args.putSerializable(ARG_SESSION_LIST_TYPE, sessionListType)
                arguments = args
            }
        }
    }

    private fun initRecyclerViewAdapter() {
        val fromAlterEgo = SessionListType.isAlterEgo(sessionListType)
        sessionListImageAdapter = SessionDetailImageAdapter(appExecutors) {

        }
        adapter = GuestEgoSessionListAdapter(appExecutors, fromAlterEgo, user?.userId!!, { session, shouldOpenCommentBox ->
            val sessionDetailFragment =
                    SessionDetailFragment.newInstance(session, user?.userId!!, sessionListType, shouldOpenCommentBox)
            getNavController().navigateTo(sessionDetailFragment)
        }, sessionDetailViewModel, audioUtil, sessionListImageAdapter, this,
                {
                }
        )

        binding.sessionList.adapter = adapter
        binding.sessionList.isNestedScrollingEnabled = false
    }

    private fun observeSessions() {
        guestEgoViewModel.setSessionRequest(sessionListType, userId, null)
        guestEgoViewModel.getSessions().observe(viewLifecycleOwner, Observer { result ->
            Timber.d("Session list resource: %s", result)
            binding.sessionResource = result

            if (result?.status == Status.SUCCESS) {
                adapter.submitList(result.data)
            }

            // Check if resource was successful but list is empty
            if (result?.status == Status.SUCCESS && (result.data == null || result.data.isEmpty())) {
                binding.empty = getEmptyState()
            } else if (result?.status == Status.SUCCESS) {
                // Hide empty view
                binding.empty = null
                // Setup the alert view
                setupAlertView()
            }
        })
    }

    private fun getEmptyState(): Empty? {
        val diaryIconRes = R.drawable.ic_diary_72dp
        return Empty.create(requireContext(), diaryIconRes, R.string.no_public_sessions, 0)
    }

    private fun createObservers() {

        guestEgoViewModel.getUser(userId).observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    if (it.data != null) {
                        binding.nickName = it.data.nickname
                        binding.imageUrl = it.data.avatarUrl
                        binding.userType = User.UserType.getNameFromUserType(it.data.userType)
                    }

                }
                Status.ERROR -> {

                }
            }
        })
        egoViewModel.initializeMediatorLiveData()

        egoViewModel.getUserSessionCount().observe(viewLifecycleOwner, Observer {
            if (it?.status?.equals(Status.SUCCESS)!!) {
                if (it.data?.size == null || it.data.isEmpty()) {
                    binding.numberOfSessions = "---"
                } else {
                    binding.numberOfSessions = it.data[0].numberOfSessions.toString()
                }

            } else if (it.status == Status.ERROR || it.status == Status.LOADING) {
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
            } else if (it.status == Status.SUCCESS) {
                if (it.data == null || it.data.isEmpty()) {
                    bestSession.message = "Unavailable"
                } else if (it.data.isNotEmpty()) {
                    val bestSession: Session = it.data[0]
                    binding.bestSession = bestSession
//                    binding.bestSessionLikeCount.text = it.data.get(0).followers.count()
                    binding.bestSessionTextView.setOnClickListener {

                        getNavController()
                                .navigate(SessionDetailFragment.newInstance(bestSession,
                                        user?.userId!!, SessionListType.ARCHIVED), true)
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
                        binding.numberOfComments = it.data[0].numberOfComments.toString()
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
        View.OnClickListener {
            val fm = fragmentManager
            val claireVartarFragment = ClaireVartarFragment()
            claireVartarFragment.setTargetFragment(this, CLAIRE_VARTAR_REQUEST_CODE)
            if (fm != null) {
                claireVartarFragment.show(fm, "claireVartarFragment")
            }
        }

    }

    private fun setupListeners() {
        binding.retryCallback = object : RetryCallback {
            override fun retry() {
                guestEgoViewModel.retry()
            }
        }

        binding.emptyCallback = object : EmptyCallback {

            override fun onEmptyButtonClicked() {
            }

        }
    }

    private fun setupAlertView() {
        guestEgoViewModel.getAlerts().observe(viewLifecycleOwner, Observer { alertResource ->
            if (alertResource?.status == Status.SUCCESS) {
                binding.alert = alertResource.data
            }
        })
//        binding.sessionListDismissAlertButton.setOnClickListener {
//            guestEgoViewModel.cancelAlert()
//        }
//        binding.sessionListDismissAlertButton.setVisibleOrGone(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioUtil.stopAudio()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioUtil.stopAudio()
    }

}
