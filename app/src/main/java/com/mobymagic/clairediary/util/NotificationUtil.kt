package com.mobymagic.clairediary.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.mobymagic.clairediary.R
import timber.log.Timber

class NotificationUtil(private val context: Context) {

    private val notifyManager: NotificationManager

    init {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notifyManager = notificationManager
    }

    /**
     * Notifies the user with the specified arguments
     *
     * @param notifyId The notification id
     * @param title The title of the notification
     * @param shortMessage The short message of the notification
     * @param longMessage The long message of the notification
     * @param category The notification category
     * @param pendingIntent The notification pending intent
     */
    fun notify(
            notifyId: Int,
            title: String = context.getString(R.string.app_name),
            shortMessage: String,
            longMessage: String?,
            category: Category,
            pendingIntent: PendingIntent
    ) {

        val notificationBuilder = NotificationCompat.Builder(context, category.id)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle(title)
                .setContentText(shortMessage)
                .setPriority(category.priority)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(context, R.color.theme_primary))
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setCategory(category.type)
                .setVisibility(category.visibility)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        if (longMessage != null) {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(longMessage))
        }

        notifyManager.notify(notifyId, notificationBuilder.build())
    }

    /**
     * Create all the notification channels used by the app
     */
    fun createNotificationChannels() {
        if (VersionUtil.hasOreo()) {
            Timber.d("Creating notification channels on Android Oreo+ device")
            // Create the reminder channel
            val reminderChannel = NotificationChannel(
                    Category.REMINDER.id,
                    context.getString(Category.REMINDER.title),
                    Category.REMINDER.importance
            )
            reminderChannel.description = context.getString(Category.REMINDER.description)
            notifyManager.createNotificationChannel(reminderChannel)

            // Create the session updates channel
            val sessionUpdatesChannel = NotificationChannel(
                    Category.SESSION_UPDATES.id,
                    context.getString(Category.SESSION_UPDATES.title),
                    Category.SESSION_UPDATES.importance
            )
            sessionUpdatesChannel.description =
                    context.getString(Category.SESSION_UPDATES.description)
            notifyManager.createNotificationChannel(sessionUpdatesChannel)

            // Create the announcement channel
            val announcementChannel = NotificationChannel(
                    Category.ANNOUNCEMENT.id,
                    context.getString(Category.ANNOUNCEMENT.title),
                    Category.ANNOUNCEMENT.importance
            )
            announcementChannel.description = context.getString(Category.ANNOUNCEMENT.description)
            notifyManager.createNotificationChannel(announcementChannel)
        }
    }

    enum class Category(
            val notificationId: Int,
            val id: String,
            val title: Int,
            val description: Int,
            val priority: Int,
            val importance: Int,
            val visibility: Int,
            val type: String
    ) {
        REMINDER(
                1,
                "REMINDER",
                R.string.notification_channel_name_reminder,
                R.string.notification_channel_desc_reminder,
                NotificationCompat.PRIORITY_DEFAULT,
                NotificationManager.IMPORTANCE_DEFAULT,
                NotificationCompat.VISIBILITY_PUBLIC,
                NotificationCompat.CATEGORY_REMINDER
        ),
        SESSION_UPDATES(
                2,
                "SESSION_UPDATES",
                R.string.notification_channel_name_session_update,
                R.string.notification_channel_desc_session_update,
                NotificationCompat.PRIORITY_MAX,
                NotificationManager.IMPORTANCE_MAX,
                NotificationCompat.VISIBILITY_SECRET,
                NotificationCompat.CATEGORY_MESSAGE
        ),
        ANNOUNCEMENT(
                3,
                "ANNOUNCEMENT",
                R.string.notification_channel_name_announcements,
                R.string.notification_channel_desc_announcements,
                NotificationCompat.PRIORITY_LOW,
                NotificationManager.IMPORTANCE_LOW,
                NotificationCompat.VISIBILITY_SECRET,
                NotificationCompat.CATEGORY_PROMO
        )
    }

}
