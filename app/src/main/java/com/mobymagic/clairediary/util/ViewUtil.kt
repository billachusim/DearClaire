package com.mobymagic.clairediary.util

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

object ViewUtil {

    fun tintDrawable(drawable: Drawable, @ColorInt color: Int): Drawable {
        val wrapDrawable = DrawableCompat.wrap(drawable).mutate()
        DrawableCompat.setTint(wrapDrawable, color)
        return wrapDrawable
    }

}