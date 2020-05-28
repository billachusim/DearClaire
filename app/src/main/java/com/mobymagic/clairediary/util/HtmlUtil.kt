package com.mobymagic.clairediary.util

import android.text.Html
import android.text.Spanned

object HtmlUtil {

    /**
     * Helper method that uses the new Html.fromHtml on Nougat and above devices while using
     * the deprecated one on Lower devices with API version lower than Nougat
     * @param text The html text
     * @return A spanned html text
     */
    @Suppress("DEPRECATION")
    fun fromHtml(text: String): Spanned {
        return if (VersionUtil.hasNougat()) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(text)
        }
    }

}