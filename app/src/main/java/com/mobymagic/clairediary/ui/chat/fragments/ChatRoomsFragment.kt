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


class ChatRoomsFragment : Fragment() {

    private lateinit var rv: RecyclerView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_rooms, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv = view.findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(context)
        rv.itemAnimator = DefaultItemAnimator()
    }

    override fun onStart() {
        super.onStart()
        rv.adapter = ChatRoomAdapter(context!!, RoomData.chatRoom)
    }


}
