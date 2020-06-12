package com.mobymagic.clairediary.ui.commentlist

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.FragmentCommentListBinding
import com.mobymagic.clairediary.ui.common.DataBoundNavFragment
import com.mobymagic.clairediary.ui.common.RetryCallback
import com.mobymagic.clairediary.ui.common.SharedViewModel
import com.mobymagic.clairediary.ui.guestego.GuestEgoFragment
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailFragment
import com.mobymagic.clairediary.ui.sessionlist.SessionListType
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.util.AudioUtil
import com.mobymagic.clairediary.util.ExoPlayerUtil
import com.mobymagic.clairediary.util.autoCleared
import com.mobymagic.clairediary.vo.Comment
import com.mobymagic.clairediary.vo.Session
import org.koin.android.ext.android.inject
import timber.log.Timber


class CommentListFragment : DataBoundNavFragment<FragmentCommentListBinding>() {

    private val audioUtil: AudioUtil by inject()
    private val androidUtil: AndroidUtil by inject()
    private val appExecutors: AppExecutors by inject()
    private val commentListViewModel: CommentListViewModel by inject()
    private lateinit var sharedViewModel: SharedViewModel
    private var adapter by autoCleared<CommentListAdapter>()
    private lateinit var session: Session
    private val exoPlayerUtil: ExoPlayerUtil by inject()

    override fun getLayoutRes() = R.layout.fragment_comment_list

    override fun getPageTitle(): Nothing? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        session = requireArguments().getParcelable(ARG_SESSION)!!
        super.onActivityCreated(savedInstanceState)
        initRecyclerView()
        initRecyclerViewAdapter()

        binding.retryCallback = object : RetryCallback {
            override fun retry() {
                commentListViewModel.retry()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
    }

    private fun initRecyclerViewAdapter() {
        val userId = requireArguments().getString(ARG_USER_ID)
        val tabType = requireArguments().getSerializable(ARG_TAB_TYPE) as SessionListType

        val rvAdapter = CommentListAdapter(appExecutors, userId.toString(), tabType, audioUtil, exoPlayerUtil,
                { comment ->
                    // Handle comment click
                },
                { comment ->
                    val subject = getString(R.string.app_name)
                    val message =
                            getString(R.string.session_detail_comment_share_message, comment.message)
                    androidUtil.shareText(requireContext(), subject, message)
                },
                {
                    showEditCommentView(it)
                },
                {
                    commentListViewModel.toggleThanks(userId.toString(), session, it)
                }, this, {
            if (!it.isUserAdmin) {
                getNavController().navigateToWithAuth(GuestEgoFragment.newInstance(it.userId,
                        "", it.userNickname, it.userAvatarUrl, SessionListType.EGO))
            }

        })


        binding.commentList.isNestedScrollingEnabled = false
        binding.commentList.adapter = rvAdapter
        adapter = rvAdapter
    }

    private fun showEditCommentView(comment: Comment) {
        (parentFragment as SessionDetailFragment).editComment(comment)
    }

    private fun initRecyclerView() {
        commentListViewModel.setSession(session)

        commentListViewModel.getCommentList().observe(viewLifecycleOwner, Observer { result ->
            Timber.d("Comment resource: %s", result)
            sharedViewModel.setNumberOfComments(result?.data?.size)
            binding.commentResource = result
            adapter.submitList(result?.data)
            binding.executePendingBindings()
        })
    }

    companion object {

        private const val ARG_SESSION = "ARG_SESSION"
        private const val ARG_USER_ID = "ARG_USER_ID"
        private const val ARG_TAB_TYPE = "ARG_TAB_TYPE"

        fun newInstance(
                session: Session,
                userId: String,
                tabType: SessionListType
        ): CommentListFragment {
            val commentListFragment = CommentListFragment()
            val args = Bundle()
            args.putParcelable(ARG_SESSION, session)
            args.putString(ARG_USER_ID, userId)
            args.putSerializable(ARG_TAB_TYPE, tabType)
            commentListFragment.arguments = args
            return commentListFragment
        }

    }

}
