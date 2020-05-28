package com.mobymagic.clairediary.vo

import android.text.TextUtils
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class UserActivity(

        @field: JvmField
        var userActivityId: String,

        @field:JvmField
        /**
         * the id of the user that has the activity
         */
        var userId: String?,
        @field:JvmField
        /**
         * the id of the user that engaged in the activity
         */
        var clientId: String?,
        @field:JvmField
        /**
         *the nickname of the user that engaged in the activity
         */
        var clientNickname: String,
        @field:JvmField
        /**
         * the text of the activity
         */
        var activityMessage: String,
        @field:JvmField
        /**
         * the date the activity was created
         */
        @ServerTimestamp var dateCreated: Date? = null,

        @field:JvmField
        var activityType: String,

        @field:JvmField
        var sessionId: String

) {
    constructor() : this(
            "",
            "",
            "",
            "",
            "",
            null,
            "",
            ""
    )

    companion object {
        /**
         * get representation of the user activity as string
         */
        fun getUserActivityString(userActivity: UserActivity, userId: String?): String {
            if (userActivity.clientId.equals(userId)) {
                return "You " + UserActivityType
                        .getActivityRepresentation(userActivity.activityType) + " a post"
            } else if (userActivity.userId.equals(userId) && !userActivity.clientId.equals(userId)) {
                if (TextUtils.isEmpty(userActivity.clientNickname)) {
                    return "Someone " + UserActivityType
                            .getActivityRepresentation(userActivity.activityType) + " your post"
                } else {
                    return userActivity.clientNickname + " " + UserActivityType
                            .getActivityRepresentation(userActivity.activityType) + " your post"
                }

            }
            return userId!!
        }

    }
}

