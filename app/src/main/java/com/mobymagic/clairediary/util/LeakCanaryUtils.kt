package com.mobymagic.clairediary.util

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher

class LeakCanaryUtils(private val application: Application) {

    lateinit var refWatcher: RefWatcher
        private set

    /**
     * Returns true if the app is currently in the process dedicated to LeakCanary heap analysis.
     * App should not be init in this process.
     */
    fun isInLeakCanaryProcess(): Boolean {
        return LeakCanary.isInAnalyzerProcess(application)
    }

    /**
     * Initializes LeakCanary and start watching for memory leaks
     */
    fun initLeakCanary() {
        refWatcher = LeakCanary.install(application)
    }

}