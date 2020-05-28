package com.mobymagic.clairediary.util

import android.os.Build

@Suppress("unused")
object VersionUtil {

    /**
     * Checks if the device have exactly Android KitKat
     * @return true if the device is on Android KitKat, false otherwise
     */
    fun isKitKat() = Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT

    /**
     * Checks if the device have Android JellyBean and above (higher versions)
     * @return true if the device has Android JellyBean and above, false otherwise
     */
    fun hasJellyBean() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN

    /**
     * Checks if the device have Android JellyBean_MR1 and above (higher versions)
     * @return true if the device has Android JellyBean_MR1 and above, false otherwise
     */
    fun hasJellyBeanMR1() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1

    /**
     * Checks if the device have Android Nougat and above (higher versions)
     * @return true if the device has Android Nougat and above, false otherwise
     */
    fun hasNougat() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    /**
     * Checks if the device have Android Oreo and above (higher versions)
     * @return true if the device has Android Oreo and above, false otherwise
     */
    fun hasOreo() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    /**
     * Checks if the device have Android Lollipop and above (higher versions)
     * @return true if the device has Android Lollipop and above, false otherwise
     */
    fun hasLollipop() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    /**
     * Checks if the device have Android Marshmallow and above (higher versions)
     * @return true if the device has Android Marshmallow and above, false otherwise
     */
    fun hasMarshmallow() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    /**
     * Checks if the device have Android KitKat and above (higher versions)
     * @return true if the device has Android KitKat and above, false otherwise
     */
    fun hasKitKat() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

}