package com.mobymagic.clairediary.fcm.command

import android.content.Context
import com.mobymagic.clairediary.fcm.FcmCommand
import timber.log.Timber

class TestCommand : FcmCommand() {

    override fun execute(context: Context, type: String, extraData: String?, isAlterEgo: String?) {
        Timber.d("Received FCM message: type=$type, extraData=$extraData")
    }

}