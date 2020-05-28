package com.mobymagic.clairediary.ui.sessiondetail

import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.ItemCreateCommentImageBinding
import com.mobymagic.clairediary.ui.common.DataBoundListAdapter
import timber.log.Timber

class CreateCommentPhotoListAdapter(
        appExecutors: AppExecutors,
        private val imageClickCallback: ((String) -> Unit),
        private val removeClickCallback: ((String) -> Unit)
) : DataBoundListAdapter<String, ItemCreateCommentImageBinding>(appExecutors) {

    override fun attachListeners(binding: ItemCreateCommentImageBinding) {
        binding.root.setOnClickListener {
            binding.imageUrl?.let { imageUrl ->
                Timber.d("Image clicked: %s", imageUrl)
                imageClickCallback.invoke(imageUrl)
            }
        }
        binding.createCommentRemoveImageButton.setOnClickListener {
            binding.imageUrl?.let { imageUrl ->
                Timber.d("Remove image clicked: %s", imageUrl)
                removeClickCallback.invoke(imageUrl)
            }
        }
    }

    override fun getLayoutRes() = R.layout.item_create_comment_image

    override fun bind(binding: ItemCreateCommentImageBinding, item: String) {
        binding.imageUrl = item
    }

}