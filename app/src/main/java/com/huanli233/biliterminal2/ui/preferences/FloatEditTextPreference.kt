package com.huanli233.biliterminal2.ui.preferences

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import androidx.preference.Preference
import com.google.android.material.textfield.TextInputLayout
import com.huanli233.biliterminal2.R

class FloatEditTextPreference(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
): EditTextPreference(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0, 0)
    constructor(context: Context): this(context, null)

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