package com.mobymagic.clairediary.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.mobymagic.clairediary.fcm.command.SessionCommand
import com.mobymagic.clairediary.fcm.command.SyncProfileCommand
import com.mobymagic.clairediary.fcm.command.TestCommand
import com.mobymagic.clairediary.repository.UserRepository
import com.mobymagic.clairediary.util.NotificationUtil
import com.mobymagic.clairediary.util.PrefUtil
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*

/**
 * Receive downstream FCM messages, examine the payload and determine what action
 * if any to take. Only known commands are executed.
 */
class FcmService : FirebaseMessagingService() {

    private val notificationUtil: NotificationUtil by inject()
    private val gson: Gson by inject()
    private val userRepository: UserRepository by inject()
    private val prefUtil: PrefUtil by inject()

    /**
     * Handle data in FCM message payload. The action field within the data determines the
     * type of Command that is initiated and executed.
     *
     * @param message Contains the message sender and a map of actions and associated extra data.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        Timber.d("Fcm Message Received: %s", message)

        // A map containing the action and extra data associated with that action.
        val data = message.data
        // Handle received FCM data messages.
        val action: String? = data[ACTION]?.toLowerCase()
        val extraData = data[EXTRA_DATA]
        var isAlterEgo: String? = "false"
        try {
            val notificationExtraData: NotificationExtraData = gson.fromJson(extraData, NotificationExtraData::class.java)
            isAlterEgo = notificationExtraData.isAlterEgo
        } catch (e: Exception) {
            Timber.e("Fcm Message Received: %s", e.message)
        }

        if (action == null) {
            Timber.e("Message received without command action")
            return
        }

        val command = MESSAGE_RECEIVERS[action]
        if (command == null) {
            Timber.e("Unknown command received: $action")
        } else {
            command.execute(this, action, extraData, isAlterEgo)
        }
    }


    init {
        // Known messages and their FCM message receivers
        val receivers = HashMap<String, FcmCommand>()
        receivers["test"] = TestCommand()
        receivers["sync_user"] = SyncProfileCommand()
        receivers["session_update"] = SessionCommand(gson, userRepository, notificationUtil, prefUtil)
        MESSAGE_RECEIVERS = Collections.unmodifiableMap(receivers)
    }

    companion object {
        private const val ACTION = "action"
        private const val EXTRA_DATA = "extraData"

        private lateinit var MESSAGE_RECEIVERS: Map<String, FcmCommand>
    }

}
