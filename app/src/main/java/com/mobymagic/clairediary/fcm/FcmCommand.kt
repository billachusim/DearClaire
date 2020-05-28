package com.mobymagic.clairediary.fcm

import android.content.Context

/**
 * Represents the client response when an FCM ping is received. Each type of FCM ping should have
 * an FcmCommand implementation associated with it.
 */
abstract class FcmCommand {

    /**
     * Defines behavior when FCM is received.
     */
    abstract fun execute(context: Context, type: String, extraData: String?, isAlterEgo: String?)

}