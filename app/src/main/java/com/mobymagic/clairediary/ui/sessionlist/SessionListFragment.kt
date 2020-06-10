package com.mobymagic.clairediary.ui.sessionlist

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentSessionListBinding
import com.mobymagic.clairediary.repository.SessionRepository
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.common.EmptyCallback
import com.mobymagic.clairediary.ui.common.RetryCallback
import com.mobymagic.clairediary.ui.createsession.CreateSessionFragment
import com.mobymagic.clairediary.ui.geustego.GuestEgoFragment
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailFragment
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailImageAdapter
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailViewModel
import com.mobymagic.clairediary.util.AudioUtil
import com.mobymagic.clairediary.util.ExoPlayerUtil
import com.mobymagic.clairediary.util.autoCleared
import com.mobymagic.clairediary.util.setVisibleOrGone
import com.mobymagic.clairediary.vo.Empty
import com.mobymagic.clairediary.vo.Session
import com.mobymagic.clairediary.vo.Status
import org.koin.android.ext.android.inject
import timber.log.Timber

class SessionListFragment : DataBoundNavFragment<FragmentSessionListBinding>() {

    private val appExecutors: AppExecutors by inject()
    private val sessionListViewModel: SessionListViewModel by inject()
    private val sessionRepository: SessionRepository by inject()
    private val sessionDetailViewModel: SessionDetailViewModel by inject()
    private val audioUtil: AudioUtil by inject()
    private var sessionListImageAdapter by autoCleared<SessionDetailImageAdapter>()
    private val exoPlayerUtil: ExoPlayerUtil by inject()
    private var adapter by autoCleared<SessionListAdapter>()
    private lateinit var sessionListType: SessionListType
    private lateinit var userId: String

    override fun getPageTitle(): String {
        return if (SessionListType.isAlterEgo(sessionListType)) {
            getString(R.string.common_title_alter_ego_mode)
        } else {
            getString(R.string.common_title_claire)
        }
    }

    override fun getLayoutRes() = R.layout.fragment_session_list
    private lateinit var rv: RecyclerView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        rv = view.findViewById(R.id.session_list)
        rv.layoutManager = LinearLayoutManager(context)
        rv.itemAnimator = DefaultItemAnimator()
        sessionListType = requireArguments().getSerializable(ARG_SESSION_LIST_TYPE) as SessionListType
        userId = requireArguments().getString(ARG_USER_ID).toString()
        sessionListViewModel.userId = userId

        setupListeners()
        observeSessions()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sessionListImageAdapter = SessionDetailImageAdapter(appExecutors) { imageUrl ->
            // TODO open a gallery
        }
        initRecyclerViewAdapter()
    }

    private fun setupListeners() {
        binding.retryCallback = object : RetryCallback {
            override fun retry() {
                sessionListViewModel.retry()
            }
        }

        binding.emptyCallback = object : EmptyCallback {

            override fun onEmptyButtonClicked() {
                onEmptyActionButtonClicked()
            }

        }
    }

    private fun initRecyclerViewAdapter() {
        val fromAlterEgo = SessionListType.isAlterEgo(sessionListType)
        adapter = SessionListAdapter(appExecutors, fromAlterEgo, userId, { session, shouldOpenCommentBox ->
            val sessionDetailFragment =
                    SessionDetailFragment.newInstance(session, userId, sessionListType, shouldOpenCommentBox)
            getNavController().navigateTo(sessionDetailFragment)
        },
                { session ->
                    @StringRes val dialogMessageRes: Int = if (fromAlterEgo) {
                        if (session.featured) {
                            R.string.session_list_confirmation_set_unfeatured
                        } else {
                            R.string.session_list_confirmation_set_featured
                        }
                    } else {
                        if (session.archived) {
                            R.string.session_list_confirmation_set_unarchived
                        } else {
                            R.string.session_list_confirmation_set_archived
                        }
                    }

                    showSessionActionDialog(dialogMessageRes, session)
                },
                sessionDetailViewModel, audioUtil, exoPlayerUtil, sessionListImageAdapter, this,

                { session ->
                    getNavController().navigateToWithAuth(GuestEgoFragment.newInstance(session.userId.toString(),
                            "", session.userNickname.toString(), session.userAvatarUrl.toString(), sessionListType))
                },
                {
                    sessionListViewModel.getNumberOfCommentsForSessions(it)
                }
        )

        binding.sessionList.adapter = adapter
        binding.sessionList.isNestedScrollingEnabled = false
    }

    private fun showSessionActionDialog(@StringRes messageRes: Int, session: Session) {
        AlertDialog.Builder(requireContext())
                .setMessage(messageRes)
                .setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .setPositiveButton(android.R.string.ok) { dialogInterface, i ->
                    if (SessionListType.isAlterEgo(sessionListType)) {
                        // Toggle featured
                        session.featured = !session.featured
                    } else {
                        // Toggle archived
                        session.archived = !session.archived
                    }
                    sessionRepository.updateSession(session, userId)
                }
                .show()
    }

    private fun observeSessions() {
        sessionListViewModel.setSessionRequest(sessionListType, userId, null)
        sessionListViewModel.getSessions().observe(viewLifecycleOwner, Observer { result ->
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

            binding.executePendingBindings()
        })
    }

    private fun getEmptyState(): Empty? {
        val diaryIconRes = R.drawable.ic_diary_72dp
        return when (sessionListType) {
            SessionListType.ARCHIVED -> {
                Empty.create(requireContext(), diaryIconRes, R.string.session_list_empty_archived, 0)
            }
            SessionListType.TRENDING -> {
                Empty.create(requireContext(), diaryIconRes, R.string.session_list_empty_featured, 0)
            }
            SessionListType.DIARY -> {
                Empty.create(
                        requireContext(), diaryIconRes, R.string.session_list_empty_diary,
                        R.string.session_list_action_start_session
                )
            }

            SessionListType.FOLLOWING -> {
                Empty.create(
                        requireContext(), diaryIconRes, R.string.session_list_empty_following,
                        0
                )
            }
            SessionListType.NON_ASSIGNED -> {
                Empty.create(requireContext(), diaryIconRes, R.string.session_list_empty_non_assigned, 0)
            }
            SessionListType.ASSIGNED -> {
                Empty.create(requireContext(), diaryIconRes, R.string.session_list_empty_assigned, 0)
            }
            SessionListType.FLAGGED -> {
                Empty.create(requireContext(), diaryIconRes, R.string.session_list_empty_flagged, 0)
            }
            SessionListType.ALL -> {
                Empty.create(requireContext(), diaryIconRes, R.string.session_list_empty_all, 0)
            }
        }
    }

    private fun setupAlertView() {
        sessionListViewModel.getAlerts().observe(viewLifecycleOwner, Observer { alertResource ->
            if (alertResource?.status == Status.SUCCESS) {
                binding.alert = alertResource.data
            }
        })
        binding.sessionListDismissAlertButton.setOnClickListener {
            sessionListViewModel.cancelAlert()
        }
        binding.sessionListDismissAlertButton.setVisibleOrGone(false)
    }

    private fun onEmptyActionButtonClicked() {
        when (sessionListType) {
            SessionListType.DIARY -> {
                val createSessionFragment = CreateSessionFragment.newInstance()
                getNavController().navigateTo(createSessionFragment)
            }
            SessionListType.ARCHIVED -> {
                // Do nothing
            }
            SessionListType.TRENDING -> {
                // Do nothing
            }
            SessionListType.NON_ASSIGNED -> {
                // Do nothing
            }
            SessionListType.ASSIGNED -> {
                // Do nothing
            }
            SessionListType.FLAGGED -> {
                // Do nothing
            }
            SessionListType.ALL -> {
                // Do nothing
            }
        }
    }

    override fun onPause() {
        super.onPause()
        audioUtil.stopAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioUtil.stopAudio()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioUtil.stopAudio()
    }

    companion object {

        private const val ARG_SESSION_LIST_TYPE = "ARG_SESSION_LIST_TYPE"
        private const val ARG_USER_ID = "ARG_USER_ID"

        fun newInstance(
                sessionListType: SessionListType,
                userId: String
        ): SessionListFragment {
            return SessionListFragment().apply {
                val args = Bundle()
                args.putSerializable(ARG_SESSION_LIST_TYPE, sessionListType)
                args.putString(ARG_USER_ID, userId)
                arguments = args
            }
        }
    }
}
