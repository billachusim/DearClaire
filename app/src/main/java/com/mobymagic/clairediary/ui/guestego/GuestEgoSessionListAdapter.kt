package com.mobymagic.clairediary.ui.guestego

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.GuestEgoSessionItemBinding
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
        private val sessionClickCallback: (Session, Boolean) -> Unit,
        private val sessionDetailViewModel: SessionDetailViewModel,
        private val audioUtil: AudioUtil,
        private var sessionListImageAdapter: SessionDetailImageAdapter,
        private val parentFragment: Fragment

) : DataBoundListAdapter<Session, GuestEgoSessionItemBinding>(appExecutors) {

    override fun getLayoutRes() = R.layout.guest_ego_session_item
    override fun attachListeners(binding: GuestEgoSessionItemBinding) {


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

        binding.sessionListAudioView.startAudioButton.setOnClickListener {

            binding.session?.audioUrl?.let { it1 ->
                audioUtil.showAudioDialog(it1, parentFragment)
            }
        }
    }

    override fun bind(binding: GuestEgoSessionItemBinding, item: Session) {
        val context = binding.root.context
        binding.session = item
        binding.userAvailable = !TextUtils.isEmpty(userId)
        binding.isFromAlterEgo = isFromAlterEgo


        binding.sessionListMeTooCountText.text = item.meToos?.size.toString()
        binding.sessionListCommentCountText.visibility = View.VISIBLE
        binding.sessionListCommentCountText.text = "1+"

        setupSessionPhotoList(binding, context, appExecutors)

        binding.sessionListContentTv.text = item.message


        val vto: ViewTreeObserver = binding.sessionListContentTv.viewTreeObserver

        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {

            override fun onGlobalLayout() {
                val obs = binding.sessionListContentTv.viewTreeObserver
                obs.removeOnGlobalLayoutListener(this)
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

    private fun setupSessionPhotoList(binding: GuestEgoSessionItemBinding, context: Context?, appExecutors: AppExecutors) {
        val layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        binding.sessionListPhotoList.isNestedScrollingEnabled = false
        binding.sessionListPhotoList.layoutManager = layoutManager
        binding.sessionListPhotoList.addItemDecoration(
                ItemOffsetDecoration(context, R.dimen.grid_spacing_regular)
        )

        sessionListImageAdapter = SessionDetailImageAdapter(appExecutors) {
            val intent = Intent(context, GalleryActivity::class.java).apply {
                putStringArrayListExtra(GalleryActivity.ARG_IMAGES, binding.session?.imageUrls as ArrayList<String>)
            }
            context.startActivity(intent)
        }

        binding.sessionListPhotoList.adapter = sessionListImageAdapter
        sessionListImageAdapter.submitList(binding.session!!.imageUrls)
    }


}