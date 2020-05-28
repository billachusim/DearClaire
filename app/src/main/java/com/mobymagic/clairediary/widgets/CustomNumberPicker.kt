package com.mobymagic.clairediary.widgets

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker

class CustomNumberPicker(context: Context, attrs: AttributeSet) : NumberPicker(context, attrs) {

    override fun addView(child: View) {
        super.addView(child)
        initEditText(child)
    }

    override fun addView(child: View, index: Int) {
        super.addView(child, index)
        initEditText(child)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, index, params)
        initEditText(child)
    }

    override fun addView(child: View, params: ViewGroup.LayoutParams) {
        super.addView(child, params)
        initEditText(child)
    }

    override fun addView(child: View, width: Int, height: Int) {
        super.addView(child, width, height)
        initEditText(child)
    }

    private fun initEditText(view: View) {
        if (view is EditText) {
            view.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    try {
                        this@CustomNumberPicker.value = Integer.parseInt(s.toString())
                    } catch (ignored: NumberFormatException) {
                    }

                }

                override fun afterTextChanged(s: Editable) {}

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            })
        }
    }

}