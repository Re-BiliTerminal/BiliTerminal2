package com.huanli233.bilizepam.utils.extensions

import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Px
import androidx.core.view.MarginLayoutParamsCompat
import androidx.core.view.ViewCompat

inline fun View.ifInEditMode(block: () -> Unit) {
    if (isInEditMode) block()
}

var TextView.editModeText: CharSequence
    get() = text
    set(value) {
        if (isInEditMode) text = value
    }

@JvmOverloads
fun TextView.updateCompoundDrawablesRelative(
    start: Drawable? = compoundDrawables[0],
    top: Drawable? = compoundDrawables[1],
    end: Drawable? = compoundDrawables[2],
    bottom: Drawable? = compoundDrawables[3],
) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
    setCompoundDrawablesRelative(start, top, end, bottom)
} else {
    setCompoundDrawables(start, top, end, bottom)
}

@JvmOverloads
fun TextView.updateCompoundDrawablesRelativeWithIntrinsicBounds(
    start: Drawable? = compoundDrawables[0],
    top: Drawable? = compoundDrawables[1],
    end: Drawable? = compoundDrawables[2],
    bottom: Drawable? = compoundDrawables[3],
) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
    setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom)
} else {
    setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom)
}

var ViewGroup.MarginLayoutParams.marginStartCompat: Int
    get() = MarginLayoutParamsCompat.getMarginStart(this)
    set(value) {
        MarginLayoutParamsCompat.setMarginStart(this, value)
    }
var ViewGroup.MarginLayoutParams.marginEndCompat: Int
    get() = MarginLayoutParamsCompat.getMarginEnd(this)
    set(value) {
        MarginLayoutParamsCompat.setMarginEnd(this, value)
    }

@Suppress("NOTHING_TO_INLINE")
inline fun ViewGroup.MarginLayoutParams.updateMarginsRelativeCompat(
    @Px start: Int = marginStartCompat,
    @Px top: Int = topMargin,
    @Px end: Int = marginEndCompat,
    @Px bottom: Int = bottomMargin
) {
    marginStartCompat = start
    topMargin = top
    marginEndCompat = end
    bottomMargin = bottom
}

@Suppress("NOTHING_TO_INLINE")
inline fun View.updatePaddingRelativeCompat(
    @Px start: Int = paddingStartCompat,
    @Px top: Int = paddingTop,
    @Px end: Int = paddingEndCompat,
    @Px bottom: Int = paddingBottom
) {
    ViewCompat.setPaddingRelative(this, start, top, end, bottom)
}

fun View.setBackgroundCompat(background: Drawable?) {
    ViewCompat.setBackground(this, background)
}

val View.paddingStartCompat: Int
    get() = ViewCompat.getPaddingStart(this)
val View.paddingEndCompat: Int get() = ViewCompat.getPaddingEnd(this)