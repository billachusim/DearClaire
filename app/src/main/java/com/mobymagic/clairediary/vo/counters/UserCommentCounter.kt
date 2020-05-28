package com.mobymagic.clairediary.vo.counters

class UserCommentCounter(@field:JvmField var userId: String,
                         @field:JvmField var numberOfComments: Int) {
    constructor() : this(
            "",
            0
    )
}