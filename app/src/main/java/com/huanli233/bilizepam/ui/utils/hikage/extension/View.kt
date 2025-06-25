package com.huanli233.bilizepam.ui.utils.hikage.extension

import android.view.View

fun View.visibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

fun View.invisibleIf(condition: Boolean) {
    visibility = if (condition) View.INVISIBLE else View.VISIBLE
}