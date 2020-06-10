package com.mobymagic.clairediary.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.ui.chat.adapter.ChatRoomAdapter
import com.mobymagic.clairediary.ui.chat.data.RoomData

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChatFragment : Fragment() {

    private lateinit var rv: RecyclerView

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
            inflater.inflate(R.layout.fragment_chat_rooms, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv = view.findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(context)
        rv.itemAnimator = DefaultItemAnimator()
    }

    override fun onStart() {
        super.onStart()
        rv.adapter = ChatRoomAdapter(requireContext(), RoomData.chatRoom)
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


}
