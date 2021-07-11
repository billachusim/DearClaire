package com.mobymagic.clairediary.vo.counters

/**
 * Counter to hold the number or Mee Toos that a user has recieved
 */
class UserMeeTooCount(
    @field:JvmField var userId: String,
    @field:JvmField var numberOfMeToos: Int
) {

    constructor() : this(
        "",
        0
    )

}