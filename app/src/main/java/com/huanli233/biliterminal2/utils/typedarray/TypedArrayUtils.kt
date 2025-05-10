package com.huanli233.biliterminal2.utils.typedarray

import android.content.Context
import android.content.res.TypedArray
import android.util.TypedValue
import androidx.annotation.StyleableRes

object TypedArrayUtils {
    fun getAttr(context: Context, attr: Int, fallbackAttr: Int): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(attr, value, true)
        if (value.resourceId != 0) {
            return attr
        }
        return fallbackAttr
    }
    fun getTextArray(
        a: TypedArray, @StyleableRes index: Int,
        @StyleableRes fallbackIndex: Int
    ): Array<CharSequence>? {
        var value = a.getTextArray(index)
        if (value == null) {
            value = a.getTextArray(fallbackIndex)
        }
        return value
    }

    fun getBoolean(
        a: TypedArray, @StyleableRes index: Int,
        @StyleableRes fallbackIndex: Int, defaultValue: Boolean
    ): Boolean {
        val value = a.getBoolean(fallbackIndex, defaultValue)
        return a.getBoolean(index, value)
    }
}