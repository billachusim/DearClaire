package com.mobymagic.clairediary.vo

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

/**
 * Represents a comment left on a Session
 */
data class Comment(
        @field:JvmField
        var audioUrl: String?,
        @field:JvmField
        var commentId: String,
        @field:JvmField
        var flagged: Boolean = false,
        @field:JvmField
        var imageUrls: MutableList<String>,
        @field:JvmField
        var message: String,
        @field:JvmField
        @ServerTimestamp
        var timeCreated: Date?,
        @field:JvmField
        var userAvatarUrl: String,
        @field:JvmField
        var userId: String,
        @field:JvmField
        var userNickname: String,
        @field:JvmField
        var isUserAdmin: Boolean = false,
        @field:JvmField
        var alterEgoId: String? = null,
        @field:JvmField
        var numberOfThanks: Int = 0,
        @field:JvmField
        var thanks: MutableList<String> = mutableListOf()

) {

    constructor() : this(
            null,
            "",
            false,
            mutableListOf<String>(),
            "",
            null,
            "",
            "",
            "",
            false,
            null,
            0
    )

}