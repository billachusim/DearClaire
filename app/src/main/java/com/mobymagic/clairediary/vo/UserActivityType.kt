package com.mobymagic.clairediary.vo

import com.mobymagic.clairediary.R

class UserActivityType {
    companion object {
        const val FOLLOW: String = "Follow"
        const val MEETOO: String = "Me2"
        const val THANKS: String = "Thanks"
        const val COMMENT: String = "Comment"
        const val ADVISE: String = "Advise"


        fun getActivityRepresentation(activity: String): String {
            when (activity) {
                FOLLOW -> {
                    return "followed"
                }
                MEETOO -> {
                    return "Me2'd"
                }
                COMMENT -> {
                    return "advised"
                }
                ADVISE -> {
                    return "advised"
                }
                THANKS -> {
                    return "thanked"
                }
            }
            return activity + "ed"
        }

        fun getImageSource(userActivity: UserActivity): Int {
            when (userActivity.activityType) {
                FOLLOW -> {
                    return R.drawable.ic_outline_add_red
                }
                MEETOO -> {
                    return R.drawable.ic_cherry_blossom
                }
                COMMENT -> {
                    return R.drawable.comment_icon_red
                }
                THANKS -> {
                    return R.drawable.double_hearts
                }
                ADVISE -> {
                    return R.drawable.comment_icon_red
                }
            }
            return R.drawable.double_hearts
        }
    }
}