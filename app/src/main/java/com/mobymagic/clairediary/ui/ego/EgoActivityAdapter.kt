package com.mobymagic.clairediary.ui.ego

import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.databinding.EgoActivityItemBinding
import com.mobymagic.clairediary.ui.common.DataBoundListAdapter
import com.mobymagic.clairediary.vo.UserActivity
import com.mobymagic.clairediary.vo.UserActivityType


class EgoActivityAdapter(
        appExecutors: AppExecutors,
        private val itemClickedCallBack: ((String?) -> Unit),
        private val userId: String?) : DataBoundListAdapter<UserActivity, EgoActivityItemBinding>(appExecutors) {

    override fun getLayoutRes() = R.layout.ego_activity_item
    override fun attachListeners(binding: EgoActivityItemBinding) {
        binding.root.setOnClickListener {
            itemClickedCallBack.invoke(binding.userActivity?.sessionId)
        }
    }

    override fun bind(binding: EgoActivityItemBinding, item: UserActivity) {
        val context = binding.root.context
        binding.userActivity = item
        binding.userActivityText = UserActivity.getUserActivityString(item, userId)
        binding.imagePath = UserActivityType.getImageSource(item)
    }
}