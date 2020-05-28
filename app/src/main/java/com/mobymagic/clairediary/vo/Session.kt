package com.mobymagic.clairediary.vo

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import com.mobymagic.clairediary.util.Mood
import java.util.*

/**
 * Represents a session started by the user
 */
data class Session(
        @field:JvmField
        var archived: Boolean,
        @field:JvmField
        var audioUrl: String?,
        @field:JvmField
        var colorHex: String,
        @field:JvmField
        var featured: Boolean,
        @field:JvmField
        var flagged: Boolean,
        @field:JvmField
        var imageUrls: MutableList<String>,
        @field:JvmField
        var message: String,
        @field:JvmField
        var respondentUserId: String,
        @field:JvmField
        var sessionId: String,
        @field:JvmField
        @ServerTimestamp
        var timeCreated: Date?,
        @field:JvmField
        @ServerTimestamp
        var timeLastActivity: Date?,
        @field:JvmField
        var title: String,
        @field:JvmField
        var userAvatarUrl: String,
        @field:JvmField
        var userId: String,
        @field:JvmField
        var userNickname: String,
        @field:JvmField
        var private: Boolean,
        @field:JvmField
        var repliesEnabled: Boolean,
        @field:JvmField
        var font: String,
        @field:JvmField
        var meTooFollowCount: Int = 0,
        @field:JvmField
        var moodId: Int = Mood.NO_MODE_ID,
        @field:JvmField
        var meToos: MutableList<String> = mutableListOf(),
        @field:JvmField
        var followers: MutableList<String> = mutableListOf()

) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.createStringArrayList(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            Date(parcel.readLong()),
            Date(parcel.readLong()),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.createStringArrayList()
    )

    constructor() : this(
            false,
            null,
            "#9BDAF3",
            false,
            false,
            mutableListOf<String>(),
            "",
            "",
            "",
            null,
            null,
            "",
            "",
            "",
            "",
            false,
            true,
            "",
            0
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (archived) 1 else 0)
        parcel.writeString(audioUrl)
        parcel.writeString(colorHex)
        parcel.writeByte(if (featured) 1 else 0)
        parcel.writeByte(if (flagged) 1 else 0)
        parcel.writeStringList(imageUrls)
        parcel.writeString(message)
        parcel.writeString(respondentUserId)
        parcel.writeString(sessionId)
        parcel.writeLong(timeCreated?.time ?: 0)
        parcel.writeLong(timeLastActivity?.time ?: 0)
        parcel.writeString(title)
        parcel.writeString(userAvatarUrl)
        parcel.writeString(userId)
        parcel.writeString(userNickname)
        parcel.writeByte(if (private) 1 else 0)
        parcel.writeByte(if (private) 1 else 0)
        parcel.writeString(font)
        parcel.writeInt(meTooFollowCount)
        parcel.writeInt(moodId)
        parcel.writeStringList(meToos)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Session> {
        override fun createFromParcel(parcel: Parcel): Session {
            return Session(parcel)
        }

        override fun newArray(size: Int): Array<Session?> {
            return arrayOfNulls(size)
        }
    }

}