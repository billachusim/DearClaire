package com.mobymagic.clairediary.jobs

import androidx.lifecycle.Observer
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.google.firebase.iid.FirebaseInstanceId
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.vo.AsyncRequest
import com.mobymagic.clairediary.vo.Resource
import com.mobymagic.clairediary.vo.Status
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class SyncProfileJob(
        private val appExecutor: AppExecutors,
        private val userRepository: UserRepository
) : Job() {

    override fun onRunJob(params: Params): Result {
        val countDownLatch = CountDownLatch(1)
        val loggedInUser = userRepository.getLoggedInUser()
        var result = Result.RESCHEDULE

        if (loggedInUser == null) {
            Timber.e("User not logged in. Cannot sync profile")
            // User is not logged in
            return Result.SUCCESS
        }

        appExecutor.mainThread().execute {
            val fcmId = FirebaseInstanceId.getInstance().token
            loggedInUser.fcmId = fcmId
            Timber.d("Syncing user profile: %s", loggedInUser)
            val updateUserLiveData = userRepository.updateUser(loggedInUser)
            updateUserLiveData.observeForever(object : Observer<Resource<AsyncRequest>> {

                override fun onChanged(asyncResource: Resource<AsyncRequest>?) {
                    Timber.d("User profile update resource: %s", asyncResource)

                    if (asyncResource?.status == Status.SUCCESS) {
                        Timber.i("User profile synced")
                        updateUserLiveData.removeObserver(this)
                        result = Result.SUCCESS
                        countDownLatch.countDown()
                    } else if (asyncResource?.status == Status.ERROR) {
                        Timber.e("User profile sync failed. Reschedule for later")
                        updateUserLiveData.removeObserver(this)
                        result = Result.FAILURE
                        countDownLatch.countDown()
                    }
                }

            })
        }

        try {
            countDownLatch.await()
        } catch (e: InterruptedException) {
            Timber.e(e, "Thread interrupted while waiting for update task to complete")
        }

        return result
    }

    companion object {

        const val TAG = "SyncProfileJob"

        fun schedule() {
            val startMillis = TimeUnit.MINUTES.toMillis(1)
            val endMillis = TimeUnit.MINUTES.toMillis(10)

            JobRequest.Builder(TAG)
                    .setExecutionWindow(startMillis, endMillis)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setRequirementsEnforced(true)
                    .setUpdateCurrent(true)
                    .build()
                    .schedule()
        }

        fun runNow() {
            JobRequest.Builder(TAG)
                    .startNow()
                    .build()
                    .schedule()
        }

    }

}