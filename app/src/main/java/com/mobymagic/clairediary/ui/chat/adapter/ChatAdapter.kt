package com.mobymagic.clairediary.ui.chat.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.ui.chat.pojo.ChatRoom
import com.vanniktech.emoji.EmojiTextView

class ChatAdapter(private val context: Context, private val chatRoomList: List<ChatRoom>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {


    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val SENT_MESSAGE = 0
    private val RECEIVED_MESSAGE = 2

    override fun getItemViewType(position: Int): Int {
        val from = chatRoomList[position].message_type
        return return when (from) {
            0 -> SENT_MESSAGE
            else -> RECEIVED_MESSAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        var view: View? = null
        //Based on view type decide which type of view to supply with viewHolder
        when (viewType) {
            SENT_MESSAGE -> view = LayoutInflater.from(parent.context).inflate(R.layout.text_message_chat, parent, false)

            RECEIVED_MESSAGE -> view = LayoutInflater.from(parent.context).inflate(R.layout.image_message_chat, parent, false)
        }
        return ChatViewHolder(view!!)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatRoom = chatRoomList[position]
        holder.comment_message_text.text = chatRoom.text_message
        holder.comment_time_text.text = chatRoom.time
        holder.comment_nickname_text.text = chatRoom.sender_user_nick_name


        if (firebaseAuth.currentUser!!.uid != chatRoom.sender_uid) {
            holder.comment_edit_button.visibility = View.INVISIBLE
        }

        getThanks(holder, chatRoom)

        holder.comment_thanks_button.setOnClickListener {
            checkForThanks(chatRoom)
        }

        holder.comment_edit_button.setOnClickListener {
            updateMessage(chatRoom.text_message!!, chatRoom.image_message!!, chatRoom.key!!)
        }

        Glide.with(context)
                .load(chatRoom.image_message)
                .apply(RequestOptions().placeholder(R.drawable.placeholder_image))
                .into(holder.postImage)

    }

    private fun updateMessage(message: String, image: String, key: String) {
        val intent = Intent("custom-message")
        intent.putExtra("message", message)
        intent.putExtra("image", image)
        intent.putExtra("key", key)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    override fun getItemCount(): Int {
        return chatRoomList.size
    }

    private fun checkForThanks(chatRoom: ChatRoom) {
        firebaseDatabase
                .getReference("messages")
                .child("group_message")
                .child(chatRoom.node!!)
                .child(chatRoom.key!!)
                .child("likes")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.hasChild(firebaseAuth.currentUser!!.uid)) {
                            removeThanks(chatRoom)
                        } else {
                            sendThanks(chatRoom)
                        }
                    }
                })
    }

    private fun sendThanks(chatRoom: ChatRoom) {

        val map = HashMap<String, Any>()
        map["from"] = firebaseAuth.currentUser!!.uid

        firebaseDatabase
                .getReference("messages")
                .child("group_message")
                .child(chatRoom.node!!)
                .child(chatRoom.key!!)
                .child("likes")
                .child(firebaseAuth.currentUser!!.uid)
                .setValue(map)
                .addOnCompleteListener {
                    if (!it.isSuccessful) {
                        Toast.makeText(context, "unable to send thanks at this time, please try again later.", Toast.LENGTH_LONG).show()
                    }
                }
    }

    private fun removeThanks(chatRoom: ChatRoom) {
        firebaseDatabase
                .getReference("messages")
                .child("group_message")
                .child(chatRoom.node!!)
                .child(chatRoom.key!!)
                .child("likes")
                .child(firebaseAuth.currentUser!!.uid)
                .removeValue()
                .addOnCompleteListener {
                    if (!it.isSuccessful) {
                        Toast.makeText(context, "${it.exception!!.message}", Toast.LENGTH_LONG).show()
                    }
                }
    }

    private fun getThanks(holder: ChatViewHolder, chatRoom: ChatRoom) {
        firebaseDatabase
                .getReference("messages")
                .child("group_message")
                .child(chatRoom.node!!)
                .child(chatRoom.key!!)
                .child("likes")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0 != null) {
                            holder.comment_thanks_count_text.text = "${p0.childrenCount}"
                        }
                    }
                })
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var comment_nickname_text: TextView = itemView.findViewById(R.id.comment_nickname_text)
        var comment_time_text: TextView = itemView.findViewById(R.id.comment_time_text)
        var comment_thanks_count_text: TextView = itemView.findViewById(R.id.comment_thanks_count_text)
        var comment_message_text: EmojiTextView = itemView.findViewById(R.id.comment_message_text)
        var comment_edit_button: ImageButton = itemView.findViewById(R.id.comment_edit_button)
        var comment_thanks_button: Button = itemView.findViewById(R.id.comment_thanks_button)
        var postImage: ImageView = itemView.findViewById(R.id.postImage)

    }
}
