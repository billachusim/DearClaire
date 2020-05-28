package com.mobymagic.clairediary.util

import android.os.StrictMode
import timber.log.Timber

class StrictModeUtils {

    /**
     * Enables strict mode only if app is in debug mode
     * @param isDebugMode is the app in debug mode
     */
    fun initStrictMode(isDebugMode: Boolean) {
        if (isDebugMode) {
            StrictMode.setThreadPolicy(getThreadPolicy())
            StrictMode.setVmPolicy(getVmThreadPolicy())
            Timber.d("Strict mode enabled for debug build")
        }
    }

    private fun getThreadPolicy(): StrictMode.ThreadPolicy {
        return StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
    }

    private fun getVmThreadPolicy(): StrictMode.VmPolicy {
        return StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build()
    }

}