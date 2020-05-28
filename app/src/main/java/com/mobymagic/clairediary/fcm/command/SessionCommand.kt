package com.mobymagic.clairediary.fcm.command

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.mobymagic.clairediary.Constants
import com.mobymagic.clairediary.MainActivity
import com.mobymagic.clairediary.fcm.FcmCommand
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.util.NotificationUtil
import com.mobymagic.clairediary.util.PrefUtil
import timber.log.Timber


class SessionCommand(
        private val gson: Gson,
        private val userRepository: UserRepository,
        private val notificationUtil: NotificationUtil,
        private val prefUtil: PrefUtil
) : FcmCommand() {

    override fun execute(context: Context, type: String, extraData: String?, isAlterEgo: String?) {
        Timber.d("Received FCM message: type=$type, extraData=$extraData")

        try {
            val model = gson.fromJson(extraData, SessionCommandModel::class.java)
            if (model == null) {
                Timber.e("Failed to parse command (gson returned null).")
                return
            }

            processCommand(context, model, isAlterEgo)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e(e, "Failed to parse FCM notification command")
        }
    }

    private fun processCommand(context: Context, model: SessionCommandModel, isAlterEgo: String?) {
        val user = userRepository.getLoggedInUser()
        if (user == null || user.userId != model.userId) {
            Timber.e("User doesn't match. Ignoring session update command")
            return
        }

        if (!prefUtil.getBool(Constants.PREF_KEY_SESSION_UPDATE_NOTIFICATION_ENABLED, true)) {
            Timber.i("Sessions update notification disabled. Skipping command")
            return
        }

        val intent = MainActivity.getStartIntent(context)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra(MainActivity.EXTRA_SESSION_ID, model.sessionId)
        intent.putExtra(MainActivity.EXTRA_USER_ID, model.userId)
        intent.putExtra(MainActivity.IS_ALTER_EGO, isAlterEgo)
        val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        )

        val sessionUpdateCategory = NotificationUtil.Category.SESSION_UPDATES
        notificationUtil.notify(
                sessionUpdateCategory.notificationId, model.title, model.shortMessage,
                model.longMessage, sessionUpdateCategory, pendingIntent
        )
    }

    data class SessionCommandModel(
            val userId: String,
            val title: String,
            val shortMessage: String,
            val longMessage: String?,
            val sessionId: String,
            val commentId: String,
            val commentUserId: String
    )

}