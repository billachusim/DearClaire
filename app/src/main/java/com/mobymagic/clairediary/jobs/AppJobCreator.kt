package com.mobymagic.clairediary.jobs

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.util.NotificationUtil
import com.mobymagic.clairediary.util.PrefUtil

class AppJobCreator(
        private val appExecutor: AppExecutors,
        private val prefUtil: PrefUtil,
        private val userRepository: UserRepository,
        private val notificationUtil: NotificationUtil
) : JobCreator {

    override fun create(tag: String): Job? {
        return when (tag) {
            RemindDailyJob.TAG -> RemindDailyJob(prefUtil, notificationUtil)
            SyncProfileJob.TAG -> SyncProfileJob(appExecutor, userRepository)
            else -> null
        }
    }

}