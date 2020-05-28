package com.mobymagic.clairediary.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.mobymagic.clairediary.AppExecutors

/**
 * A generic RecyclerView adapter that uses Data Binding & DiffUtil.
 *
 * @param <T> Type of the items in the list
 * @param <V> The type of the ViewDataBinding
</V></T> */
abstract class DataBoundListAdapter<T, V : ViewDataBinding>(
        appExecutors: AppExecutors
) : ListAdapter<T, DataBoundViewHolder<V>>(
        AsyncDifferConfig.Builder<T>(
                object : DiffUtil.ItemCallback<T>() {

                    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
                        return oldItem == newItem
                    }

                    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
                        return oldItem == newItem
                    }

                }
        )
                .setBackgroundThreadExecutor(appExecutors.diskIO())
                .build()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<V> {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<V>(layoutInflater, getLayoutRes(), parent, false)
        attachListeners(binding)
        return DataBoundViewHolder(binding)
    }

    protected abstract fun attachListeners(binding: V)

    @LayoutRes
    protected abstract fun getLayoutRes(): Int

    override fun onBindViewHolder(holder: DataBoundViewHolder<V>, position: Int) {
        bind(holder.binding, getItem(position))
        holder.binding.executePendingBindings()
    }

    protected abstract fun bind(binding: V, item: T)

}