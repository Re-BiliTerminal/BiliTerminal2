package com.huanli233.bilizepam.ui.utils.hikage.extension

import android.graphics.Typeface
import android.widget.TextView
import com.huanli233.bilizepam.ui.activity.CopyActivity
import splitties.activities.start
import splitties.intents.start

fun TextView.boldTypeFace() {
    typeface = Typeface.defaultFromStyle(Typeface.BOLD)
}

fun TextView.italicTypeFace() {
    typeface = Typeface.defaultFromStyle(Typeface.ITALIC)
}

fun TextView.boldItalicTypeFace() {
    typeface = Typeface.defaultFromStyle(Typeface.BOLD_ITALIC)
}

fun TextView.normalTypeFace() {
    typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
}

fun TextView.copyable() {
    setOnLongClickListener {
        context.start<CopyActivity> {
            putExtra("content", text)
        }
        true
    }
}