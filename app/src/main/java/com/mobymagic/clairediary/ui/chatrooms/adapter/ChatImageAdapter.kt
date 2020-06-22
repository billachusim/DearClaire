package com.mobymagic.clairediary.ui.chatrooms.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mobymagic.clairediary.R

class ChatImageAdapter(private val mCtx: Context, private val mImageList: MutableList<String>) : RecyclerView.Adapter<ChatImageAdapter.ChatImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatImageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.chat_room_image, parent, false)
        return ChatImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatImageViewHolder, position: Int) {
        val mImage = mImageList[position]

        Glide.with(mCtx)
                .load(mImage)
                .apply(RequestOptions().placeholder(R.drawable.placeholder_image))
                .into(holder.mImage)

//        holder.mImage.setImageURI(Uri.parse(mImage))
        holder.mClear.setOnClickListener {
            val intent = Intent("clear-msg")
            LocalBroadcastManager.getInstance(mCtx).sendBroadcast(intent)

        }
    }

    override fun getItemCount(): Int {
        return mImageList.size
    }

    inner class ChatImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mImage: ImageView = itemView.findViewById(R.id.chatRoomImage)
        var mClear: ImageView = itemView.findViewById(R.id.ImageClear)

    }
}
