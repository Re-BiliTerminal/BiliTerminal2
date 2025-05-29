package com.huanli233.biliterminal2.ui.utils.hikage.extension

import android.graphics.Typeface
import android.widget.TextView

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