package com.mobymagic.clairediary.ui.createsession

import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.ItemSessionFontBinding
import com.mobymagic.clairediary.ui.common.DataBoundListAdapter
import com.mobymagic.clairediary.vo.Font
import timber.log.Timber

class CreateSessionFontListAdapter(
    appExecutors: AppExecutors,
    private val fontClickCallback: ((Font) -> Unit)
) : DataBoundListAdapter<Font, ItemSessionFontBinding>(appExecutors) {

    override fun attachListeners(binding: ItemSessionFontBinding) {
        binding.root.setOnClickListener {
            Timber.d("Font clicked")
            binding.font?.let { font ->
                Timber.d("Font clicked: %s", font)
                fontClickCallback.invoke(font)
            }
        }
    }

    override fun getLayoutRes() = R.layout.item_session_font

    override fun bind(binding: ItemSessionFontBinding, item: Font) {
        binding.font = item
    }

}