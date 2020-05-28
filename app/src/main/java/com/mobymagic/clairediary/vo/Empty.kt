package com.mobymagic.clairediary.vo

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

data class Empty(val icon: Drawable, val message: String, val buttonText: String?) {

    companion object {

        fun create(
                context: Context,
                @DrawableRes iconRes: Int,
                @StringRes messageRes: Int,
                @StringRes buttonTextRes: Int
        ): Empty {
            val icon = ContextCompat.getDrawable(context, iconRes)!!
            val message = context.getString(messageRes)
            val buttonText = if (buttonTextRes != 0) context.getString(buttonTextRes) else null
            return Empty(icon, message, buttonText)
        }

    }
}