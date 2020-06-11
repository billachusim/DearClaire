package com.mobymagic.clairediary.ui.geustego

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.text.TextUtils
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.GeustEgoSessionItemBinding
import com.mobymagic.clairediary.ui.common.DataBoundListAdapter
import com.mobymagic.clairediary.ui.gallery.GalleryActivity
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailImageAdapter
import com.mobymagic.clairediary.ui.sessiondetail.SessionDetailViewModel
import com.mobymagic.clairediary.util.AudioUtil
import com.mobymagic.clairediary.util.HtmlUtil
import com.mobymagic.clairediary.vo.Session
import com.mobymagic.clairediary.widgets.ItemOffsetDecoration
import timber.log.Timber
import java.util.*


class GuestEgoSessionListAdapter(
        private val appExecutors: AppExecutors,
        private val isFromAlterEgo: Boolean,
        private val userId: String,
        private val sessionClickCallback: ((Session, Boolean) -> Unit),
        private val sessionDetailViewModel: SessionDetailViewModel,
        private val audioUtil: AudioUtil,
        private var sessionListImageAdapter: SessionDetailImageAdapter,
        private val parentFragment: androidx.fragment.app.Fragment,
        private val avartarClickCallback: ((Session) -> Unit)
) : DataBoundListAdapter<Session, GeustEgoSessionItemBinding>(appExecutors) {

    override fun getLayoutRes() = R.layout.geust_ego_session_item
    override fun attachListeners(binding: GeustEgoSessionItemBinding) {


        binding.root.setOnClickListener {
            binding.session?.let { session ->
                Timber.d("Session clicked: %s", session)
                sessionClickCallback.invoke(session, false)
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
    }


    private fun showFollowingDialog(binding: GeustEgoSessionItemBinding) {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle(String.format(binding.root.context
                .getString(R.string.do_you_want_to_follow_this_session), binding.followText))
        builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
            sessionDetailViewModel.toggleFollowers(userId, binding.session!!)
            Toast.makeText(binding.root.context,
                    if (binding.followText == "unfollow")
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

    override fun bind(binding: GeustEgoSessionItemBinding, item: Session) {
        val context = binding.root.context
        binding.session = item
        binding.userAvailable = !TextUtils.isEmpty(userId)
        binding.followCount = item.followers.count()
        updateFollowText(binding, item)
        binding.isFromAlterEgo = isFromAlterEgo


        binding.sessionListMeTooCountText.text = item.meToos?.size.toString()

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
    }

    private fun setupSessionPhotoList(binding: GeustEgoSessionItemBinding, context: Context?, appExecutors: AppExecutors) {
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

    private fun updateFollowText(binding: GeustEgoSessionItemBinding, session: Session) {
        if (session.followers.contains(userId)) {
            binding.followText = "unfollow"
        } else {
            binding.followText = "follow"
        }
    }


}