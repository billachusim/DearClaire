package com.mobymagic.clairediary.ui.chatrooms.pojo

import android.os.Parcel
import android.os.Parcelable

open class ChatRoom : Parcelable {
    var userAvatarUrl: Any? = String()
        private set
    var text_message: String? = null
        private set
    var audio_message: String? = null
        private set
    var image_message: String? = null
        private set
    var time: String? = null
        private set
    var sender_uid: String? = null
        private set
    var sender_user_nick_name: String? = null
        private set

    var key: String? = null
        private set

    var node: String? = null
        private set

    var message_type: Int? = null
        private set

    constructor()

    constructor(text_message: String, audio_message: String, image_message: String, time: String, sender_uid: String, sender_user_nick_name: String, key: String, node: String, message_type: Int, userAvatarUrl: String) {
        this.text_message = text_message
        this.audio_message = audio_message
        this.image_message = image_message
        this.time = time
        this.sender_uid = sender_uid
        this.sender_user_nick_name = sender_user_nick_name
        this.key = key
        this.node = node
        this.message_type = message_type
        this.userAvatarUrl = userAvatarUrl

    }

    protected constructor(`in`: Parcel) {
        text_message = `in`.readString()
        audio_message = `in`.readString()
        image_message = `in`.readString()
        time = `in`.readString()
        sender_uid = `in`.readString()
        sender_user_nick_name = `in`.readString()
        key = `in`.readString()
        node = `in`.readString()
        message_type = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(text_message)
        dest.writeString(audio_message)
        dest.writeString(image_message)
        dest.writeString(time)
        dest.writeString(sender_uid)
        dest.writeString(sender_user_nick_name)
        dest.writeString(key)
        dest.writeString(node)
        dest.writeInt(message_type!!)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<ChatRoom> = object : Parcelable.Creator<ChatRoom> {
            override fun createFromParcel(`in`: Parcel): ChatRoom {
                return ChatRoom(`in`)
            }

            override fun newArray(size: Int): Array<ChatRoom?> {
                return arrayOfNulls(size)
            }
        }
    }
}