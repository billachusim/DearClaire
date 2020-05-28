package com.mobymagic.clairediary.ui.sessiondetail

import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.ItemSessionDetailImageBinding
import com.mobymagic.clairediary.ui.common.DataBoundListAdapter
import timber.log.Timber

class SessionDetailImageAdapter(
        appExecutors: AppExecutors,
        private val imageClickCallback: ((String) -> Unit)
) : DataBoundListAdapter<String, ItemSessionDetailImageBinding>(appExecutors) {

    override fun attachListeners(binding: ItemSessionDetailImageBinding) {
        binding.root.setOnClickListener {
            binding.imageUrl?.let { imageUrl ->
                Timber.d("Image clicked: %s", imageUrl)
                imageClickCallback.invoke(imageUrl)
            }
        }
    }

    override fun getLayoutRes() = R.layout.item_session_detail_image

    override fun bind(binding: ItemSessionDetailImageBinding, item: String) {
        binding.imageUrl = item
    }

}