package com.mobymagic.clairediary.api

import androidx.lifecycle.LiveData
import com.mobymagic.clairediary.api.request.SyncUserRequest
import com.mobymagic.clairediary.api.response.SyncUserResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/syncUser")
    fun syncUser(@Body request: SyncUserRequest): LiveData<ApiResponse<SyncUserResponse>>

}