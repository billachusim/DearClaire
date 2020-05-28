package com.mobymagic.clairediary.ui.common

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputLayout

class ClearErrorTextWatcher(private vararg val textInputLayouts: TextInputLayout) : TextWatcher {

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        // Clear error from input layouts when the user types
        for (textInputLayout in textInputLayouts) {
            if (textInputLayout.error != null) {
                textInputLayout.error = null
            }
        }
    }

}