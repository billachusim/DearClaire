package com.mobymagic.clairediary.ui.common

import androidx.fragment.app.FragmentStatePagerAdapter

class PagerAdapter(
        fm: androidx.fragment.app.FragmentManager,
        private val items: List<Item>
) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int) = items[position].page

    override fun getCount() = items.size

    override fun getPageTitle(position: Int) = items[position].title

    data class Item(val title: String? = null, val page: androidx.fragment.app.Fragment)

}