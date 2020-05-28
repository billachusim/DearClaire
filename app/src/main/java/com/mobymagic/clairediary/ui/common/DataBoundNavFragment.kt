package com.mobymagic.clairediary.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.mobymagic.clairediary.util.autoCleared

abstract class DataBoundNavFragment<T : ViewDataBinding> : NavFragment() {

    var binding by autoCleared<T>()

    @LayoutRes
    abstract fun getLayoutRes(): Int

    final override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, getLayoutRes(), container, false)
        return binding.root
    }

}