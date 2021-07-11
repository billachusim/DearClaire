package com.mobymagic.clairediary.vo

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

/**
 * Represents a User
 */
data class User(
    @field:JvmField
    var avatarUrl: String?,
    @field:JvmField
    var fcmId: String?,
    @field:JvmField
    var gender: String,
    @field:JvmField
    var nickname: String,
    @field:JvmField
    var email: String,
    @field:JvmField
    var secretCode: String,
    @field:JvmField
    var userType: UserType = UserType.REGULAR,
    @field:JvmField
    @ServerTimestamp var timeLastUnlocked: Date? = null,
    @field:JvmField
    @ServerTimestamp var timeRegistered: Date? = null,
    @field:JvmField
    var userId: String? = null,
    @field:JvmField
    var alterEgoId: String = "",
    @field:JvmField
    var alterEgoAccessCode: String = ""
) {

    @Suppress("unused")
    constructor() : this(
        "", "", "", "", "",
        "", UserType.REGULAR, null, null, "", "", ""
    )

    constructor(email: String, secretCode: String) : this("", null, "", "", email, secretCode)

    enum class UserType {
        REGULAR, ADMIN, SUPER_ADMIN;

        companion object {

            fun isAdmin(userType: UserType?): Boolean {
                return userType == ADMIN || userType == SUPER_ADMIN
            }

            fun getNameFromUserType(userType: UserType?): String {
                when (userType) {
                    ADMIN -> return "Alter-Ego"
                    REGULAR -> return "Ego"
                    SUPER_ADMIN -> return "Super-Ego"
                }
                return "Regular"
            }

        }
    }

}