package com.mobymagic.clairediary.util

import android.util.Log
import com.crashlytics.android.Crashlytics

import timber.log.Timber

class TimberUtils {

    /**
     * Plants a specific Timber tree based on if app is in debug mode
     * @param isDebugMode is the app in debug mode
     */
    fun initTimber(isDebugMode: Boolean) {
        if (isDebugMode) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
    }

    /**
     * A Timber tree for release mode. It ignores Verbose, Debug and Info logs but sends Warning and Error
     * logs to Crashlytics
     */
    private class ReleaseTree : Timber.Tree() {

        override fun isLoggable(tag: String?, priority: Int): Boolean {
            return priority >= Log.WARN
        }

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            Crashlytics.log(message)

            if (t != null) {
                Crashlytics.logException(t)
            }
        }

    }

}