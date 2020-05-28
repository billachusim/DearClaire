package com.mobymagic.clairediary.vo.counters

/**
 * Counter to help keep track of the number of sessions a user has created
 * We would not need a distributed counter for this since sessions are not created frequently
 **/

data class UserSessionCounter(@field:JvmField var userId: String,
                              @field:JvmField var numberOfSessions: Int) {

    constructor() : this(
            "",
            0
    )

}