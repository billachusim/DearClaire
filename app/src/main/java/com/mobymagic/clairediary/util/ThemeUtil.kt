package com.mobymagic.clairediary.util

import android.content.Context
import android.graphics.Color
import com.mobymagic.clairediary.R

class ThemeUtil(private val context: Context) {

    fun getAlterEgoTheme(curTheme: String): AlterEgoTheme {
        val themes = context.resources.getStringArray(R.array.settings_theme_names)
        return when (curTheme) {
            themes[0] -> {
                AlterEgoTheme(Color.parseColor("#9C27B0"), Color.parseColor("#7B1FA2"))
            }
            themes[1] -> {
                AlterEgoTheme(Color.parseColor("#3F51B5"), Color.parseColor("#303F9F"))
            }
            themes[2] -> {
                AlterEgoTheme(Color.parseColor("#FF5722"), Color.parseColor("#E64A19"))
            }
            themes[3] -> {
                AlterEgoTheme(Color.parseColor("#F44336"), Color.parseColor("#D32F2F"))
            }
            themes[4] -> {
                AlterEgoTheme(Color.parseColor("#607D8B"), Color.parseColor("#455A64"))
            }
            themes[5] -> {
                AlterEgoTheme(Color.parseColor("#607D8B"), Color.parseColor("#455A64"))
            }
            themes[6] -> {
                AlterEgoTheme(Color.parseColor("#F44336"), Color.parseColor("#D32F2F"))
            }
            else -> {
                AlterEgoTheme(Color.parseColor("#9C27B0"), Color.parseColor("#7B1FA2"))
            }
        }
    }

    data class AlterEgoTheme(val primaryColor: Int, val primaryDarkColor: Int)

}