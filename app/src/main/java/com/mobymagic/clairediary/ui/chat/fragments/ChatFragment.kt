package com.mobymagic.clairediary.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.mobymagic.clairediary.R
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChatFragment : Fragment() {


    private var param1: String? = null
    private var param2: String? = null

    fun getPageTitle(): String {
        return "Dear Claire"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =// Inflate the layout for this fragment
            inflater.inflate(R.layout.fragment_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager = view.findViewById<ViewPager>(R.id.viewpager)
        setupViewPager(viewPager)
        val tabs = view.findViewById<TabLayout>(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ChatAdapter(fragmentManager)
        adapter.addFragment(ChatRoomsFragment(), "Chat Rooms")
        adapter.addFragment(PrivateChatFragment(), "Secret Chat")
        adapter.addFragment(RequestFragment(), "Chat Requests")
        viewPager.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance(userId: String) =
                ChatFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, userId)
                        putString(ARG_PARAM2, "")
                    }
                }
    }


    inner class ChatAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {

        private val mFragList = ArrayList<Fragment>()
        private val mFragTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragList[position]
        }

        override fun getCount(): Int {
            return mFragList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragList.add(fragment)
            mFragTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragTitleList[position]
        }
    }
}
