package com.mobymagic.clairediary.jobs

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.evernote.android.job.DailyJob
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.mobymagic.clairediary.Constants
import com.mobymagic.clairediary.MainActivity
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.util.NotificationUtil
import com.mobymagic.clairediary.util.PrefUtil
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Job that shows a reminder notification daily to the user when daily reminder is enabled
 */
class RemindDailyJob(
        private val prefUtil: PrefUtil,
        private val notificationUtil: NotificationUtil
) : DailyJob() {

    override fun onRunDailyJob(params: Params): DailyJob.DailyJobResult {
        // Check if daily reminders are enabled
        if (prefUtil.getBool(Constants.PREF_KEY_DAILY_REMINDER_ENABLED, true)) {
            notifyUser()
        }

        return DailyJobResult.SUCCESS
    }

    private fun notifyUser() {
        notificationUtil.notify(
                NotificationUtil.Category.REMINDER.notificationId,
                getReminderTitle(),
                getReminderMessage(),
                null,
                NotificationUtil.Category.REMINDER,
                getReminderPendingIntent()
        )
    }

    private fun getReminderTitle(): String {
        return context.getString(R.string.daily_reminder_title)
    }

    private fun getReminderMessage(): String {
        val messages = context.resources.getStringArray(R.array.daily_reminder_messages)
        val randomIndex = Random().nextInt(messages.size)
        return messages[randomIndex]
    }

    private fun getReminderPendingIntent(): PendingIntent {
        val intent = MainActivity.getStartIntent(context)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra(MainActivity.EXTRA_NAVIGATE_TO_CREATE_SESSION, true)
        return PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    companion object {

        const val TAG = "RemindDailyJob"
        const val DEFAULT_HOUR = 19

        fun reschedule(context: Context, hourOfDay: Long) {
            Timber.d("Rescheduling reminder job")
            JobManager.create(context).cancelAllForTag(TAG)
            schedule(context, hourOfDay)
        }

        fun schedule(context: Context, hourOfDay: Long) {
            val reminderJobRequests = JobManager.create(context).getAllJobRequestsForTag(TAG)
            Timber.d("Reminder job requests: %s", reminderJobRequests)
            if (reminderJobRequests.isEmpty()) {
                Timber.d("No remind job requests, scheduling")
                // schedule between hourOfDay and hourOfDay + 30 minutes
                val startMs = TimeUnit.HOURS.toMillis(hourOfDay)
                val endMs = TimeUnit.HOURS.toMillis(hourOfDay) + TimeUnit.MINUTES.toMillis(30)
                schedule(JobRequest.Builder(TAG).setUpdateCurrent(true), startMs, endMs)
            }
        }

    }
}