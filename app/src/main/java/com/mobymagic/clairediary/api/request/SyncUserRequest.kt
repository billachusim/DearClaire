package com.mobymagic.clairediary.api.request

import com.mobymagic.clairediary.vo.User

data class SyncUserRequest(
        private val localUser: User
)