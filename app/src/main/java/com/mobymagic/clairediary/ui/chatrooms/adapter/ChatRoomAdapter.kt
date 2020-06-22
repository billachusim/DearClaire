package com.mobymagic.clairediary.ui.chatrooms.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.ui.chatrooms.ChatActivity
import com.mobymagic.clairediary.ui.chatrooms.pojo.ChatRoomPojo

class ChatRoomAdapter(private val context: Context, private val chatRoomPojoList: List<ChatRoomPojo>?) : RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.chat_room_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatRoomPojo = chatRoomPojoList!![position]
        // holder.chatroomMoodMessage.text = chatRoomPojo.moodMessage
        // holder.chatroomUsersCount.text = chatRoomPojo.usersCount
        holder.cardBackGround.setBackgroundColor(Color.parseColor(chatRoomPojo.hex))
        holder.title.text = chatRoomPojo.title
        holder.sessionListContentTv.text = chatRoomPojo.text
        holder.sessionListContentTv.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("chatRoom", chatRoomPojo)
            context.startActivity(intent)
        }
        holder.enterChatroomButton.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("chatRoom", chatRoomPojo)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return chatRoomPojoList?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // var chatroomMoodMessage: TextView = itemView.findViewById(R.id.chatroom_mood_message)
        // var chatroomUsersCount: TextView = itemView.findViewById(R.id.chatroom_users_count_text)
        var cardBackGround: CardView = itemView.findViewById(R.id.cardBackGround)
        var title: TextView = itemView.findViewById(R.id.chatroom_title)
        var sessionListContentTv: TextView = itemView.findViewById(R.id.chatroom_desc_textview)
        var enterChatroomButton: TextView = itemView.findViewById(R.id.enter_chatroom_button)

    }
}
