package com.mobymagic.clairediary.ui.sessionlist

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.ItemSessionBinding
import com.mobymagic.clairediary.ui.common.DataBoundListAdapter
import com.mobymagic.clairediary.ui.gallery.GalleryActivity
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailImageAdapter
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailViewModel
import com.mobymagic.clairediary.util.*
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Session
import com.mobymagic.clairediary.vo.Status
import com.mobymagic.clairediary.widgets.ItemOffsetDecoration
import timber.log.Timber
import java.util.*


class SessionListAdapter(
        private val appExecutors: AppExecutors,
        private val isFromAlterEgo: Boolean,
        private val userId: String,
        private val sessionClickCallback: ((Session, Boolean) -> Unit),
        private val actionClickCallback: ((Session) -> Unit),
        private val sessionDetailViewModel: SessionDetailViewModel,
        private val audioUtil: AudioUtil,
        private val exoPlayerUtil: ExoPlayerUtil,
        private var sessionListImageAdapter: SessionDetailImageAdapter,
        private val parentFragment: androidx.fragment.app.Fragment,
        private val avartarClickCallback: ((Session) -> Unit),
        private val getCommentCountCallBack: ((String) -> LiveData<Resource<Int>>)
) : DataBoundListAdapter<Session, ItemSessionBinding>(appExecutors) {

    init {
        sessionDetailViewModel.setFromalterEgo(isFromAlterEgo)
    }

    override fun getLayoutRes() = R.layout.item_session
    override fun attachListeners(binding: ItemSessionBinding) {

        binding.root.setOnClickListener {
            binding.session?.let { session ->
                Timber.d("Session clicked: %s", session)
                sessionClickCallback.invoke(session, false)
            }
        }
        binding.sessionActionButton.setOnClickListener {
            binding.session?.let { session ->
                Timber.d("Action button clicked for session: %s", session)
                actionClickCallback.invoke(session)
            }
        }

        binding.sessionListMeTooButton.setOnClickListener {
            binding.session = binding.session?.let { session ->
                sessionDetailViewModel.toggleMeToo(userId, session)
            }
        }
        binding.sessionListCommentButton.setOnClickListener {
            binding.session?.let { session ->
                Timber.d("Session clicked: %s", session)
                sessionClickCallback.invoke(session, true)
            }
        }
        binding.sessionListFollow.setOnClickListener {

            binding.session?.let { session ->
                if (userId == session.userId) {
                    Toast.makeText(binding.root.context,
                            binding.root.context.getString(R.string.cannot_follow_your_own_session), Toast.LENGTH_LONG).show()
                } else {
                    showFollowingDialog(binding)
                }
            }
        }

        binding.sessionListAudioView.startAudioButton.setOnClickListener {

            binding.session?.audioUrl?.let { it1 ->
                audioUtil.showAudioDialog(it1, parentFragment)
            }
        }

        binding.sessionDetailUserImage.setOnClickListener { it ->
            binding.session.let {
                avartarClickCallback.invoke(it!!)
            }
        }
    }


    private fun showFollowingDialog(binding: ItemSessionBinding) {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle(String.format(binding.root.context
                .getString(R.string.do_you_want_to_follow_this_session), binding.followText))
        builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
            sessionDetailViewModel.toggleFollowers(userId, binding.session!!)
            Toast.makeText(binding.root.context,
                    if (binding.followText.equals("unfollow"))
                        "UnFollowed Diary Session" else {
                        "Following Diary Session"
                    }, Toast.LENGTH_LONG).show()
            updateFollowText(binding, binding.session!!)
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("No") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
        }

        builder.show()
    }

    override fun bind(binding: ItemSessionBinding, item: Session) {
        val context = binding.root.context
        binding.session = item
        binding.userAvailable = !TextUtils.isEmpty(userId)
        binding.followCount = item.followers.count()
        updateFollowText(binding, item)
        binding.isFromAlterEgo = isFromAlterEgo

        binding.sessionActionButton.visibility = View.VISIBLE

        binding.sessionListMeTooCountText.text = item.meToos.size.toString()

        setupSessionPhotoList(binding, context, appExecutors)

        binding.sessionListContentTv.text = item.message

        val vto: ViewTreeObserver = binding.sessionListContentTv.viewTreeObserver

        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {

            override fun onGlobalLayout() {
                val obs = binding.sessionListContentTv.viewTreeObserver
                obs.removeGlobalOnLayoutListener(this)
                if (binding.sessionListContentTv.lineCount > 5) {
                    val lineEndIndex = binding.sessionListContentTv.layout.getLineEnd(4)
                    val text = binding.sessionListContentTv.text.subSequence(0, lineEndIndex - 4)
                    var finalText = text.toString()
                    finalText += "<b>... more</b> "
                    binding.sessionListContentTv.text = HtmlUtil.fromHtml(finalText)
                }
            }
        })



        if (isFromAlterEgo) {
            if (item.private) {
                binding.sessionActionButton.visibility = View.GONE
            } else {
                if (item.featured) {
                    binding.sessionActionButton.setImageResource(R.drawable.round_star_border_white_24)
                    binding.sessionActionButton.contentDescription =
                            context.getString(R.string.session_list_action_unfeature)
                } else {
                    val drawable = ContextCompat.getDrawable(context, R.drawable.round_star_white_24)!!
                    val color = ContextCompat.getColor(context, R.color.inactive_icon_light)
                    val tintedDrawable = ViewUtil.tintDrawable(drawable, color)
                    binding.sessionActionButton.setImageDrawable(tintedDrawable)
                    binding.sessionActionButton.contentDescription =
                            context.getString(R.string.session_list_action_feature)
                }
            }
        } else {
            if (item.archived) {
                binding.sessionActionButton.setImageResource(R.drawable.ic_round_unarchive_24)
                binding.sessionActionButton.contentDescription =
                        context.getString(R.string.session_list_action_unarchive)
            } else {
                binding.sessionActionButton.setImageResource(R.drawable.ic_round_archive_24)
                binding.sessionActionButton.contentDescription =
                        context.getString(R.string.session_list_action_archive)
            }

            binding.sessionActionButton.setVisibleOrGone(item.userId == userId)
        }
        try {
            getCommentCountCallBack.invoke(item.sessionId).observe(context as LifecycleOwner, Observer {
                when (it?.status) {
                    Status.SUCCESS -> {
                        if (it.data != null) {
                            binding.sessionListCommentCountText.visibility = View.VISIBLE
                            binding.sessionListCommentCountText.text = it.data.toString()
                        }
                    }
                    else -> {
                        binding.sessionListCommentCountText.visibility = View.GONE
                    }
                }
            })
        } catch (ex: Exception) {

        }
    }

    private fun setupSessionPhotoList(binding: ItemSessionBinding, context: Context?, appExecutors: AppExecutors) {
        val layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        binding.sessionListPhotoList.isNestedScrollingEnabled = false
        binding.sessionListPhotoList.layoutManager = layoutManager
        binding.sessionListPhotoList.addItemDecoration(
                ItemOffsetDecoration(context, R.dimen.grid_spacing_regular)
        )

        sessionListImageAdapter = SessionDetailImageAdapter(appExecutors) { imageUrl ->
            val intent = Intent(context, GalleryActivity::class.java).apply {
                putStringArrayListExtra(GalleryActivity.ARG_IMAGES, binding.session?.imageUrls as ArrayList<String>)
            }
            context.startActivity(intent)
        }

        binding.sessionListPhotoList.adapter = sessionListImageAdapter
        sessionListImageAdapter.submitList(binding.session!!.imageUrls)
    }

    private fun updateFollowText(binding: ItemSessionBinding, session: Session) {
        if (session.followers.contains(userId)) {
            binding.followText = "unfollow"
        } else {
            binding.followText = "follow"
        }
    }

    private fun updateActionButton(binding: ItemSessionBinding, session: Session) {
        if (session.archived) {
            binding.sessionActionButton.setImageResource(R.drawable.ic_archive_white_24dp)
        } else {
            binding.sessionActionButton.setImageResource(R.drawable.ic_round_unarchive_24)
        }
    }


}