package com.mobymagic.clairediary.ui.createsession

import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.ItemCreateSessionImageBinding
import com.mobymagic.clairediary.ui.common.DataBoundListAdapter
import timber.log.Timber

class CreateSessionPhotoListAdapter(
        appExecutors: AppExecutors,
        private val imageClickCallback: ((String) -> Unit),
        private val removeClickCallback: ((String) -> Unit)
) : DataBoundListAdapter<String, ItemCreateSessionImageBinding>(appExecutors) {

    override fun attachListeners(binding: ItemCreateSessionImageBinding) {
        binding.root.setOnClickListener {
            binding.imageUrl?.let { imageUrl ->
                Timber.d("Image clicked: %s", imageUrl)
                imageClickCallback.invoke(imageUrl)
            }
        }
        binding.createSessionRemoveImageButton.setOnClickListener {
            binding.imageUrl?.let { imageUrl ->
                Timber.d("Remove image clicked: %s", imageUrl)
                removeClickCallback.invoke(imageUrl)
            }
        }
    }

    override fun getLayoutRes() = R.layout.item_create_session_image

    override fun bind(binding: ItemCreateSessionImageBinding, item: String) {
        binding.imageUrl = item
    }

}