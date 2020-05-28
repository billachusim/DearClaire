package com.mobymagic.clairediary.vo

import com.mobymagic.clairediary.R

class UserActivityType {
    companion object {
        val FOLLOW: String = "follow"
        val MEETOO: String = "meetoo"
        val THANKS: String = "thanks"
        val SESSION: String = "session"
        val COMMENT: String = "comment"
        val ADVICE: String = "advice"

        fun getActivityRepresentation(activity: String): String {
            when (activity) {
                FOLLOW -> {
                    return "followed "
                }
                MEETOO -> {
                    return "reacted to "
                }
                COMMENT -> {
                    return "commented on"
                }
                ADVICE -> {
                    return "adviced"
                }
            }
            return "followed"
        }

        fun getImageSource(userActivity: UserActivity): Int {
            when (userActivity.activityType) {
                FOLLOW -> {
                    return R.drawable.ic_outline_add_red
                }
                MEETOO -> {
                    return R.drawable.double_hearts
                }
                COMMENT -> {
                    return R.drawable.comment_icon_red
                }

                ADVICE -> {
                    return R.drawable.comment_icon_red
                }
            }
            return return R.drawable.ic_outline_add
        }
    }
}