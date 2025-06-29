package com.huanli233.bilizepam.ui.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.preference.Preference
import com.huanli233.bilizepam.R

@SuppressLint("PrivateResource")
open class BasePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = getAttr(context, androidx.preference.R.attr.preferenceStyle, android.R.attr.preferenceStyle),
    defStyleRes: Int = 0
): Preference(context, attrs, defStyleAttr, defStyleRes) {
    companion object {
        fun getAttr(context: Context, attr: Int, fallbackAttr: Int): Int {
            val value = TypedValue()
            context.theme.resolveAttribute(attr, value, true)
            if (value.resourceId != 0) {
                return attr
            }
            return fallbackAttr
        }
    }
}