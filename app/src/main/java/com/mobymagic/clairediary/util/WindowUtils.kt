package com.mobymagic.clairediary.util

import android.app.Activity
import android.view.Window
import android.view.WindowManager

class WindowUtils {

    /**
     * Makes the specified activity full screen. Hiding the status bar and bottom navigation
     * @param activity The activity to make full screen
     */
    fun makeFullScreen(activity: Activity) {
        // remove title and make fullScreen
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE)

        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

}
