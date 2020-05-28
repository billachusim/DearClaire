package com.mobymagic.clairediary.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import com.mobymagic.clairediary.R


class AndroidUtil(private val context: Context) {

    /**
     * Get the string resource with the provided id
     * @param stringResId The id of the string to get
     * @param formatArgs Optional format arguments for string
     * @return The string text
     */
    fun getString(@StringRes stringResId: Int, vararg formatArgs: Any): String {
        return context.getString(stringResId, *formatArgs)
    }

    /**
     * Shares the given subject and message with the apps the user has installed on their device
     * @param context Any context
     * @param subject The title of the share message
     * @param message The text to share
     */
    fun shareText(
            context: Context,
            subject: String,
            message: String
    ) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, message)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)

        context.startActivity(
                Intent.createChooser(
                        intent,
                        context.getString(R.string.common_chooser_title_share_using)
                )
        )
    }

    fun openUrl(context: Context, url: String) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        context.startActivity(i)
    }

    /**
     * Opens any mail client compose screen on the device with the provided recipient and subject
     * @param context Any context
     * @param recipient The recipient/receiver of the mail
     * @param subject The title of the mail
     */
    fun sendMail(
            context: Context,
            recipient: String,
            subject: String
    ) {
        val intent = Intent(Intent.ACTION_SENDTO)
        // only email apps should handle this
        intent.data = Uri.parse("mailto:" + recipient)
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        // this will make such that when user returns to the app, our app is displayed, instead of the email app.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, R.string.error_no_email_clients, Toast.LENGTH_LONG).show()
        }
    }
}