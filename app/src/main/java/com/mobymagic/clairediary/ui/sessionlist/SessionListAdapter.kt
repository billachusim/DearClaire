package com.mobymagic.clairediary.ui.sessionlist

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.mobymagic.clairediary.util.AudioUtil
import com.mobymagic.clairediary.util.HtmlUtil
import com.mobymagic.clairediary.util.ViewUtil
import com.mobymagic.clairediary.util.setVisibleOrGone
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
        private val sessionClickCallback: (Session, Boolean) -> Unit,
        private val actionClickCallback: (Session) -> Unit,
        private val sessionDetailViewModel: SessionDetailViewModel,
        private val audioUtil: AudioUtil,
        private var sessionListImageAdapter: SessionDetailImageAdapter,
        private val parentFragment: Fragment,
        private val avatarClickCallback: (Session) -> Unit,
        private val getCommentCountCallBack: (String) -> LiveData<Resource<Int>>

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

        binding.sessionListAudioView.startAudioButton.setOnClickListener {

            binding.session?.audioUrl?.let { it1 ->
                audioUtil.showAudioDialog(it1, parentFragment)
            }
        }

        binding.sessionDetailUserImage.setOnClickListener {
            binding.session.let {
                avatarClickCallback.invoke(it!!)
            }
        }

        binding.sessionDetailNicknameText.setOnClickListener {
            binding.session.let {
                avatarClickCallback.invoke(it!!)
            }
        }
    }

    override fun bind(binding: ItemSessionBinding, item: Session) {
        val context = binding.root.context
        binding.session = item
        binding.userAvailable = !TextUtils.isEmpty(userId)
        binding.followCount = item.followers.count()
        updateFollowText(binding, item)
        binding.isFromAlterEgo = isFromAlterEgo

        binding.sessionActionButton.visibility = View.VISIBLE

        binding.sessionListMeTooCountText.text = item.meToos!!.size.toString()

        setupSessionPhotoList(binding, context, appExecutors)

        binding.sessionListContentTv.text = item.message

        val vto: ViewTreeObserver = binding.sessionListContentTv.viewTreeObserver

        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {

            override fun onGlobalLayout() {
                val obs = binding.sessionListContentTv.viewTreeObserver
                obs.removeOnGlobalLayoutListener(this)
                if (binding.sessionListContentTv.lineCount > 6) {
                    val lineEndIndex = binding.sessionListContentTv.layout.getLineEnd(5)
                    val text = binding.sessionListContentTv.text.subSequence(0, lineEndIndex - 5)
                    var finalText = text.toString()
                    finalText += "<b>... more</b> "
                    binding.sessionListContentTv.text = HtmlUtil.fromHtml(finalText)
                }
            }
        })

        // Bind session list action buttons
        if (userId == item.userId) {
            if (item.private) {
                binding.sessionListTrendingButton.visibility = View.GONE
            } else {
                if (item.featured) {
                    binding.sessionListTrendingButton.visibility = View.VISIBLE
                    binding.sessionListTrendingButton.setImageResource(R.drawable.round_star_white_24)
                    binding.sessionListTrendingButton.contentDescription =
                            context.getString(R.string.session_list_action_unfeature)
                } else {
                    val drawable = ContextCompat.getDrawable(context, R.drawable.round_star_white_24)!!
                    val color = ContextCompat.getColor(context, R.color.inactive_icon_light)
                    val tintedDrawable = ViewUtil.tintDrawable(drawable, color)
                    binding.sessionListTrendingButton.setImageDrawable(tintedDrawable)
                    binding.sessionListTrendingButton.contentDescription =
                            context.getString(R.string.session_list_action_feature)
                }
            }
        } else {
            if (userId != item.userId) {
                binding.sessionListTrendingButton.visibility = View.INVISIBLE
            }
        }

        binding.sessionListTrendingButton.setOnClickListener {
            if (item.featured) {
                Toast.makeText(binding.root.context,
                        binding.root.context.getString(R.string.your_session_is_trending), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(binding.root.context,
                        binding.root.context.getString(R.string.ask_claire_to_trend_this_session), Toast.LENGTH_LONG).show()
            }
        }

        if (isFromAlterEgo) {
            if (item.private) {
                binding.sessionActionButton.visibility = View.GONE
            } else {
                if (item.featured) {
                    binding.sessionActionButton.visibility = View.VISIBLE
                    binding.sessionActionButton.setImageResource(R.drawable.round_star_white_24)
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
            getCommentCountCallBack.invoke(item.sessionId.toString()).observe(context as LifecycleOwner, Observer {
                when (it?.status) {
                    Status.SUCCESS -> {
                        if (it.data != null) {
                            binding.sessionListCommentCountText.visibility = View.VISIBLE
                            binding.sessionListCommentCountText.text = it.data.toString() + "+"
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

        sessionListImageAdapter = SessionDetailImageAdapter(appExecutors) {
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
            binding.followText = "Unfollow"
        } else {
            binding.followText = "Follow"
        }
    }


}