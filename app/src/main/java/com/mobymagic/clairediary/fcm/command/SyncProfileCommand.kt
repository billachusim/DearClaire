package com.mobymagic.clairediary.fcm.command

import android.content.Context
import com.mobymagic.clairediary.fcm.FcmCommand
import com.mobymagic.clairediary.jobs.SyncProfileJob
import timber.log.Timber

class SyncProfileCommand : FcmCommand() {

    override fun execute(context: Context, type: String, extraData: String?, isAlterEgo: String?) {
        Timber.d("Received FCM message: type=$type, extraData=$extraData")
        SyncProfileJob.schedule()
    }

}