package com.mobymagic.clairediary.util

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.mobymagic.clairediary.R
import com.vanniktech.emoji.EmojiEditText
import com.vanniktech.emoji.EmojiPopup

class InputUtil(private val context: Context) {

    /**
     * Checks if the given email is valid
     * @param inputEmail The email to check
     * @return true if the email is valid, false otherwise
     */
    fun isValidEmail(inputEmail: String): Boolean {
        return !TextUtils.isEmpty(inputEmail) && Patterns.EMAIL_ADDRESS.matcher(inputEmail)
            .matches()
    }

    fun setupAutoResizeInput(
        invisibleTextView: TextView,
        resizableEditText: EditText,
        hint: String
    ) {
        resizableEditText.textSize = autoSizeText(invisibleTextView.textSize)

        resizableEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                resizableEditText.textSize = autoSizeText(invisibleTextView.textSize)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = if (s?.isEmpty() != false) hint else s.toString()
                invisibleTextView.setText(text, TextView.BufferType.EDITABLE)
            }
        })
    }

    private fun autoSizeText(size: Float): Float {
        return size / (context.resources.displayMetrics.density + 0.2f)
    }

    fun setupEmojiPopup(
        rootView: View,
        toggleButton: ImageButton,
        inputView: EmojiEditText
    ): EmojiPopup {
        // Build EmojiPopup
        return EmojiPopup.Builder.fromRootView(rootView)
            .setOnEmojiPopupShownListener {
                toggleButton.setImageResource(R.drawable.ic_round_keyboard_24)
                toggleButton.contentDescription =
                    rootView.context.getString(R.string.common_content_desc_show_keyboard)
            }
            .setOnEmojiPopupDismissListener {
                toggleButton.setImageResource(R.drawable.ic_round_insert_emoticon_white_24)
                toggleButton.contentDescription =
                    rootView.context.getString(R.string.common_content_desc_show_emoji)
            }
            .build(inputView)
    }

}