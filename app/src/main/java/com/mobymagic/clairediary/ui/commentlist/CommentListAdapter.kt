package com.mobymagic.clairediary.ui.commentlist

import android.content.Intent
import android.text.TextUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.ItemCommentBinding
import com.mobymagic.clairediary.ui.common.DataBoundListAdapter
import com.mobymagic.clairediary.ui.gallery.GalleryActivity
import com.mobymagic.clairediary.ui.sessionlist.SessionListType
import com.mobymagic.clairediary.util.AudioUtil
import com.mobymagic.clairediary.util.ExoPlayerUtil
import com.mobymagic.clairediary.vo.Comment
import com.mobymagic.clairediary.widgets.ItemOffsetDecoration
import timber.log.Timber
import java.util.*

class CommentListAdapter(
    private val appExecutors: AppExecutors,
    private val userId: String,
    private val tabType: SessionListType,
    private val audioUtil: AudioUtil,
    private val exoPlayerUtil: ExoPlayerUtil,
    private val commentClickCallback: ((Comment) -> Unit),
    private val shareClickCallback: ((Comment) -> Unit),
    private val editClickCallback: ((Comment) -> Unit),
    private val thanksCallback: ((Comment) -> Unit),
    private val sourceFragment: androidx.fragment.app.Fragment,
    private val avatarClickedCallBack: ((Comment) -> Unit)
) : DataBoundListAdapter<Comment, ItemCommentBinding>(appExecutors) {


    override fun attachListeners(binding: ItemCommentBinding) {
        binding.root.setOnClickListener {
            binding.comment?.let { comment ->
                Timber.d("Comment clicked: %s", comment)
                commentClickCallback.invoke(comment)
            }
        }
        binding.commentShareButton.setOnClickListener {
            binding.comment?.let { comment ->
                Timber.d("Share click for comment: %s", comment)
                shareClickCallback.invoke(comment)
            }
        }
        binding.commentEditButton.setOnClickListener {
            binding.comment?.let {
                Timber.d("Edit click for comment: %s", it)
                editClickCallback.invoke(it)
            }
        }
        binding.commentThanksButton.setOnClickListener {
            binding.comment?.let {
                Timber.d("Toggle thanks clicked for comment: %s", it)
                thanksCallback.invoke(it)
                notifyDataSetChanged()
            }
        }
        binding.userAvailable = !TextUtils.isEmpty(userId)
    }

    override fun getLayoutRes() = R.layout.item_comment

    override fun bind(binding: ItemCommentBinding, item: Comment) {
        binding.comment = item
        binding.showEditButton = item.userId == userId

        if (tabType == SessionListType.ALL) {
            if (item.isUserAdmin) {
                binding.commentNicknameText.text = item.alterEgoId
            } else {
                binding.commentNicknameText.text = item.userNickname
            }
        } else if (item.isUserAdmin) {
            binding.commentNicknameText.text =
                binding.root.context.getString(R.string.app_name_as_claire)
        } else {
            binding.commentNicknameText.text = item.userNickname
        }

        val adapter = CommentListImageAdapter(appExecutors) { imageUrl ->
            val intent = Intent(binding.root.context, GalleryActivity::class.java).apply {
                putStringArrayListExtra(
                    GalleryActivity.ARG_IMAGES,
                    binding.comment?.imageUrls as ArrayList<String>
                )
            }
            binding.root.context.startActivity(intent)
        }

        val layoutManager = LinearLayoutManager(
            binding.root.context,
            LinearLayoutManager.HORIZONTAL, false
        )
        //binding.photoList.isNestedScrollingEnabled = false
        binding.commentPhotoList.layoutManager = layoutManager
        binding.commentPhotoList.addItemDecoration(
            ItemOffsetDecoration(
                binding.root.context,
                R.dimen.grid_spacing_regular
            )
        )

        binding.commentPhotoList.adapter = adapter
        adapter.submitList(item.imageUrls)

        binding.commentAudioView.startAudioButton.setOnClickListener {
            item.audioUrl?.let { audioUrl ->
                audioUtil.showAudioDialog(audioUrl, sourceFragment)
            }
        }

        binding.commentUserImage.setOnClickListener {
            avatarClickedCallBack.invoke(binding.comment!!)
        }

        binding.commentNicknameText.setOnClickListener {
            avatarClickedCallBack.invoke(binding.comment!!)
        }

    }
}