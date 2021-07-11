package com.mobymagic.clairediary.ui.archivesessions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mobymagic.clairediary.repository.SessionRepository
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Session
import java.util.*

class ArchiveSessionViewModel(private val sessionRepository: SessionRepository) : ViewModel() {

    lateinit var userId: String

    fun getUserSessionsByDate(
        startDate: Date
    ): LiveData<Resource<List<Session>>> {
        return sessionRepository.getUserSessionsByDate(userId, startDate)
    }

    fun retryLoadingSessionByDate(startDate: Date) {
        getUserSessionsByDate(startDate)
    }

}