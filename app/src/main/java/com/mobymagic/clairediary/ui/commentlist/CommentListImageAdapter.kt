package com.mobymagic.clairediary.ui.commentlist

import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.ItemCommentImageBinding
import com.mobymagic.clairediary.ui.common.DataBoundListAdapter
import timber.log.Timber

class CommentListImageAdapter(
    appExecutors: AppExecutors,
    private val imageClickCallback: ((String) -> Unit)
) : DataBoundListAdapter<String, ItemCommentImageBinding>(appExecutors) {

    override fun attachListeners(binding: ItemCommentImageBinding) {
        binding.root.setOnClickListener {
            binding.imageUrl?.let { imageUrl ->
                Timber.d("Image clicked: %s", imageUrl)
                imageClickCallback.invoke(imageUrl)
            }
        }
    }

    override fun getLayoutRes() = R.layout.item_comment_image

    override fun bind(binding: ItemCommentImageBinding, item: String) {
        binding.imageUrl = item
    }

}