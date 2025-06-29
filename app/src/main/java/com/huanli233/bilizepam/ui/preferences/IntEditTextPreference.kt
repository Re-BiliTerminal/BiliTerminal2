package com.huanli233.bilizepam.ui.preferences

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.google.android.material.textfield.TextInputLayout
import com.huanli233.bilizepam.R

class IntEditTextPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = getAttr(context, androidx.preference.R.attr.editTextPreferenceStyle, android.R.attr.editTextPreferenceStyle),
    defStyleRes: Int = 0
): EditTextPreference(context, attrs, defStyleAttr, defStyleRes) {

    var minValue: Int = Int.MIN_VALUE
    var maxValue: Int = Int.MAX_VALUE

    init {
        context.withStyledAttributes(attrs, R.styleable.AppIntEditTextPreference, defStyleAttr, defStyleRes) {
            minValue = getInt(R.styleable.AppIntEditTextPreference_minIntValue, Int.MIN_VALUE)
            maxValue = getInt(R.styleable.AppIntEditTextPreference_maxIntValue, Int.MAX_VALUE)
        }
    }

    override fun checkText(text: String): Boolean {
        return text.isEmpty() && emptyAllowed || text.toIntOrNull()?.let { it in minValue..maxValue } == true
    }

    override fun onCreateEditText(): TextInputLayout {
        return super.onCreateEditText().apply {
            editText?.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }
}