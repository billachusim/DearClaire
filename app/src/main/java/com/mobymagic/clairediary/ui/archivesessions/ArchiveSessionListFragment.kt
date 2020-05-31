package com.mobymagic.clairediary.ui.archivesessions

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentArchiveSessionListBinding
import com.mobymagic.clairediary.repository.SessionRepository
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.common.EmptyCallback
import com.mobymagic.clairediary.ui.common.RetryCallback
import com.mobymagic.clairediary.ui.createsession.CreateSessionFragment
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailFragment
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailImageAdapter
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailViewModel
import com.mobymagic.clairediary.ui.sessionlist.SessionListAdapter
import com.mobymagic.clairediary.ui.sessionlist.SessionListType
import com.mobymagic.clairediary.ui.sessionlist.SessionListViewModel
import com.mobymagic.clairediary.util.AudioUtil
import com.mobymagic.clairediary.util.ExoPlayerUtil
import com.mobymagic.clairediary.util.autoCleared
import com.mobymagic.clairediary.vo.Empty
import com.mobymagic.clairediary.vo.Session
import com.mobymagic.clairediary.vo.Status
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*

class ArchiveSessionListFragment : DataBoundNavFragment<FragmentArchiveSessionListBinding>() {

    private val appExecutors: AppExecutors by inject()
    private val archiveSessionViewModel: ArchiveSessionViewModel by inject()
    private val sessionDetailViewModel: SessionDetailViewModel by inject()
    private val sessionListViewModel: SessionListViewModel by inject()
    private val audioUtil: AudioUtil by inject()
    private var sessionListImageAdapter by autoCleared<SessionDetailImageAdapter>()
    private val exoPlayerUtil: ExoPlayerUtil by inject()

    //TODO: Don't call repository from  view again please
    private val sessionRepository: SessionRepository by inject()


    private var adapter by autoCleared<SessionListAdapter>()
    private lateinit var userId: String
    private lateinit var date: Date

    override fun getPageTitle(): String {
        return "Your Session"

    }

    override fun getLayoutRes() = R.layout.fragment_archive_session_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        userId = requireArguments().getString(ARG_USER_ID).toString()
        archiveSessionViewModel.userId = userId
        date = requireArguments().getSerializable(ARG_DATE) as Date

        setupListeners()
        observeSessions()

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sessionListImageAdapter = SessionDetailImageAdapter(appExecutors) {
            // TODO open a gallery
        }
        initRecyclerViewAdapter()
    }

    private fun setupListeners() {
        binding.retryCallback = object : RetryCallback {
            override fun retry() {
                archiveSessionViewModel.retryLoadingSessionByDate(date)
            }
        }

        binding.emptyCallback = object : EmptyCallback {

            override fun onEmptyButtonClicked() {
                onEmptyActionButtonClicked()
            }

        }
    }

    private fun initRecyclerViewAdapter() {
        adapter = SessionListAdapter(appExecutors, false, userId, { session, shouldOpenCommentBox ->
            val sessionDetailFragment =
                    SessionDetailFragment.newInstance(session, userId, SessionListType.DIARY, shouldOpenCommentBox)
            getNavController().navigateTo(sessionDetailFragment)
        },
                { session ->
                    @StringRes val dialogMessageRes: Int = if (session.archived) {
                        R.string.session_list_confirmation_set_unarchived
                    } else {
                        R.string.session_list_confirmation_set_archived
                    }
                    showSessionActionDialog(dialogMessageRes, session)
                },
                sessionDetailViewModel, audioUtil, exoPlayerUtil, sessionListImageAdapter, this,

                {
                },
                {
                    sessionListViewModel.getNumberOfCommentsForSessions(it)
                }
        )

        binding.sessionList.adapter = adapter
        binding.sessionList.isNestedScrollingEnabled = false
    }


    private fun observeSessions() {
        archiveSessionViewModel.getUserSessionsByDate(date).observe(viewLifecycleOwner, Observer { result ->
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
//                setupAlertView()
            }

            binding.executePendingBindings()
        })
    }

    private fun getEmptyState(): Empty? {
        val diaryIconRes = R.drawable.ic_diary_72dp
        return Empty.create(
                requireContext(), diaryIconRes, R.string.session_list_empty_diary,
                R.string.session_list_action_start_session
        )
    }

//    private fun setupAlertView() {
//        sessionListViewModel.getAlerts().observe(this, Observer { alertResource ->
//            if (alertResource?.status == Status.SUCCESS) {
//                binding.alert = alertResource.data
//            }
//        })
//        binding.sessionListDismissAlertButton.setOnClickListener {
//            sessionListViewModel.cancelAlert()
//        }
//        binding.sessionListDismissAlertButton.setVisibleOrGone(false)
//    }

    private fun onEmptyActionButtonClicked() {
        val createSessionFragment = CreateSessionFragment.newInstance()
        getNavController().navigateTo(createSessionFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioUtil.stopAudio()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioUtil.stopAudio()
    }

    private fun showSessionActionDialog(@StringRes messageRes: Int, session: Session) {
        AlertDialog.Builder(requireContext())
                .setMessage(messageRes)
                .setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .setPositiveButton(android.R.string.ok) { dialogInterface, i ->
                    // Toggle archived
                    session.archived = !session.archived
                    sessionRepository.updateSession(session, userId).observe(viewLifecycleOwner, Observer {
                        if (it?.status?.equals(Status.SUCCESS)!!) {
                            adapter.notifyDataSetChanged()
                        }
                    })
                }
                .show()
    }

    companion object {

        private const val ARG_USER_ID = "ARG_USER_ID"
        private const val ARG_DATE = "ARG_DATE"

        fun newInstance(
                userId: String,
                date: Date
        ): ArchiveSessionListFragment {
            return ArchiveSessionListFragment().apply {
                val args = Bundle()
                args.putString(ARG_USER_ID, userId)
                args.putSerializable(ARG_DATE, date)
                arguments = args
            }
        }
    }
}
