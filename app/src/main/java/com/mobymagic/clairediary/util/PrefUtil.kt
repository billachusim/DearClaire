package com.mobymagic.clairediary.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * A simple SharedPreferences wrapper that allows easy setting and getting of values
 */
@Suppress("unused")
class PrefUtil(context: Context) {

    private val sp: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

    fun setString(key: String, value: String?) {
        sp.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String?): String? {
        return sp.getString(key, defaultValue)
    }

    fun setBool(key: String, value: Boolean) {
        sp.edit().putBoolean(key, value).apply()
    }

    fun getBool(key: String, defaultValue: Boolean): Boolean {
        return sp.getBoolean(key, defaultValue)
    }

    fun setInt(key: String, value: Int) {
        sp.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return sp.getInt(key, defaultValue)
    }

    fun setLong(key: String, value: Long) {
        sp.edit().putLong(key, value).apply()
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return sp.getLong(key, defaultValue)
    }

    fun setFloat(key: String, value: Float) {
        sp.edit().putFloat(key, value).apply()
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return sp.getFloat(key, defaultValue)
    }

}