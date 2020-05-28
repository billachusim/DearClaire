package com.mobymagic.clairediary.api.response

import com.mobymagic.clairediary.vo.User

data class SyncUserResponse(
        private val user: User
)