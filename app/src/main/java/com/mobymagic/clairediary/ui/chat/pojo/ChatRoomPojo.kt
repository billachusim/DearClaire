package com.mobymagic.clairediary.ui.chat.pojo

import android.os.Parcel
import android.os.Parcelable

class ChatRoomPojo : Parcelable {

    var title: String? = null
        private set
    var font: String? = null
        private set
    var hex: String? = null
        private set
    var text: String? = null
        private set

    constructor()

    constructor(title: String, font: String, hex: String, text: String) {
        this.title = title
        this.font = font
        this.hex = hex
        this.text = text
    }

    protected constructor(`in`: Parcel) {
        title = `in`.readString()
        font = `in`.readString()
        hex = `in`.readString()
        text = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeString(font)
        dest.writeString(hex)
        dest.writeString(text)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<ChatRoomPojo> = object : Parcelable.Creator<ChatRoomPojo> {
            override fun createFromParcel(`in`: Parcel): ChatRoomPojo {
                return ChatRoomPojo(`in`)
            }

            override fun newArray(size: Int): Array<ChatRoomPojo?> {
                return arrayOfNulls(size)
            }
        }
    }
}
