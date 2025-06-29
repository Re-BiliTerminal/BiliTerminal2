package com.huanli233.bilizepam.ui.preferences

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.google.android.material.textfield.TextInputLayout
import com.huanli233.bilizepam.R

class FloatEditTextPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = getAttr(context, androidx.preference.R.attr.editTextPreferenceStyle, android.R.attr.editTextPreferenceStyle),
    defStyleRes: Int = 0
): EditTextPreference(context, attrs, defStyleAttr, defStyleRes) {

    var minValue: Float = Float.MIN_VALUE
    var maxValue: Float = Float.MAX_VALUE

    init {
        context.withStyledAttributes(attrs, R.styleable.AppFloatEditTextPreference, defStyleAttr, defStyleRes) {
            minValue = getFloat(R.styleable.AppFloatEditTextPreference_minFloatValue, Float.MIN_VALUE)
            maxValue = getFloat(R.styleable.AppFloatEditTextPreference_maxFloatValue, Float.MAX_VALUE)
        }
    }

    override fun checkText(text: String): Boolean {
        return text.isEmpty() && emptyAllowed || text.toFloatOrNull()?.let { it in minValue..maxValue } == true
    }

    override fun onCreateEditText(): TextInputLayout {
        return super.onCreateEditText().apply {
            editText?.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
    }
}