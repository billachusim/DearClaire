package com.mobymagic.clairediary.ui.clairevartar

import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.ClairevartarItemBinding
import com.mobymagic.clairediary.ui.common.DataBoundListAdapter
import com.mobymagic.clairediary.vo.ClaireVartar
import timber.log.Timber

class ClaireVartarAdapter(
    private val appExecutors: AppExecutors,
    private val avartarItemClickCallback: ((ClaireVartar) -> Unit)
) : DataBoundListAdapter<ClaireVartar, ClairevartarItemBinding>(appExecutors) {


    override fun attachListeners(binding: ClairevartarItemBinding) {
        binding.root.setOnClickListener {
            binding.clairevartar?.let { clairevartar ->
                Timber.d("Clairevartar clicked: %s", clairevartar)
                avartarItemClickCallback.invoke(clairevartar)
            }
        }
    }

    override fun getLayoutRes() = R.layout.clairevartar_item

    override fun bind(binding: ClairevartarItemBinding, item: ClaireVartar) {
        binding.clairevartar = item

    }
}