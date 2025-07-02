package com.huanli233.bilizepam.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import android.R as AndroidR

@ColorInt
fun Context.getThemeColor(@AttrRes attrResId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrResId, typedValue, true)
    return typedValue.data
}

fun Context.getThemeDimension(@AttrRes attrResId: Int): Float {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrResId, typedValue, true)
    return TypedValue.complexToDimension(typedValue.data, resources.displayMetrics)
}

fun Context.getThemeDrawable(@AttrRes attrResId: Int): Drawable? {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrResId, typedValue, true)
    if (typedValue.resourceId != 0) {
        return ContextCompat.getDrawable(this, typedValue.resourceId)
    }
    return null
}

fun Context.getThemeString(@AttrRes attrResId: Int): String? {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrResId, typedValue, true)
    if (typedValue.resourceId != 0) {
        return resources.getString(typedValue.resourceId)
    }
    return null
}

fun Context.getThemeBoolean(@AttrRes attrResId: Int, defaultValue: Boolean = false): Boolean {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrResId, typedValue, true)
    if (typedValue.type == TypedValue.TYPE_INT_BOOLEAN) {
        return typedValue.data != 0
    }
    return defaultValue
}


@ColorInt
fun View.getThemeColor(@AttrRes attrResId: Int): Int = context.getThemeColor(attrResId)

fun View.getThemeDimension(@AttrRes attrResId: Int): Float = context.getThemeDimension(attrResId)

fun View.getThemeDrawable(@AttrRes attrResId: Int): Drawable? = context.getThemeDrawable(attrResId)

fun View.getThemeString(@AttrRes attrResId: Int): String? = context.getThemeString(attrResId)

fun View.getThemeBoolean(@AttrRes attrResId: Int, defaultValue: Boolean = false): Boolean =
    context.getThemeBoolean(attrResId, defaultValue)

val View.selectableItemBackground
    get() = getThemeDrawable(AndroidR.attr.selectableItemBackground)