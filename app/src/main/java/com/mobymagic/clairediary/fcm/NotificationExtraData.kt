package com.mobymagic.clairediary.fcm

data class NotificationExtraData(
    @field:JvmField
    var isAlterEgo: String?
) {
    constructor() : this(
        "false"
    )
}
