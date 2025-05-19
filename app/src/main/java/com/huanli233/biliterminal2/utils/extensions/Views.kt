package com.huanli233.biliterminal2.utils.extensions

import android.view.View
import androidx.annotation.Px
import androidx.core.view.ViewCompat

@Suppress("NOTHING_TO_INLINE")
inline fun View.updatePaddingRelativeCompat(
    @Px left: Int = paddingLeft,
    @Px top: Int = paddingTop,
    @Px right: Int = paddingRight,
    @Px bottom: Int = paddingBottom
) {
    ViewCompat.setPaddingRelative(this, left, top, right, bottom)
}