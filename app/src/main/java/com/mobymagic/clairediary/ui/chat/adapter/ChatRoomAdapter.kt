package com.mobymagic.clairediary.ui.chat.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.ui.chat.ChatActivity
import com.mobymagic.clairediary.ui.chat.pojo.ChatRoomPojo

class ChatRoomAdapter(private val context: Context, private val chatRoomPojoList: List<ChatRoomPojo>?) : RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.chat_room_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatRoomPojo = chatRoomPojoList!![position]
        holder.cardBackGround.setBackgroundColor(Color.parseColor(chatRoomPojo.hex))
        holder.title.text = chatRoomPojo.title
        holder.session_list_content_tv.text = chatRoomPojo.text
        holder.follow.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("chatRoom", chatRoomPojo)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return chatRoomPojoList?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cardBackGround: CardView
        var title: TextView
        var session_list_content_tv: TextView
        var follow: LinearLayout

        init {
            cardBackGround = itemView.findViewById(R.id.cardBackGround)
            title = itemView.findViewById(R.id.group_title)
            session_list_content_tv = itemView.findViewById(R.id.session_list_content_tv)
            follow = itemView.findViewById(R.id.follow)
        }

    }
}
