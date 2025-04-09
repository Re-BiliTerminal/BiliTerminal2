@file:Suppress("NOTHING_TO_INLINE")
package com.huanli233.biliterminal2.util.extensions

import android.view.View
import android.widget.ImageView
import com.huanli233.biliterminal2.R

inline fun View.gone() {
    visibility = View.GONE
}
inline fun View.visible() {
    visibility = View.VISIBLE
}
inline fun View.invisible() {
    visibility = View.INVISIBLE
}

inline fun ImageView.showError() {
    setImageResource(R.mipmap.loading_2233_error)
}