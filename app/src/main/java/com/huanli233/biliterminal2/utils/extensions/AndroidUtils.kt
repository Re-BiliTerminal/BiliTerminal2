package com.huanli233.biliterminal2.utils.extensions

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.applicationContext

inline fun <T : CharSequence> SpannableStringBuilder.appendWithSpan(
    text: T,
    flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
    spanBuilder: (T) -> Any
) {
    val lengthBefore = length
    append(text)
    val lengthAfter = length
    setSpan(spanBuilder(text), lengthBefore, lengthAfter, flags)
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.getParcelableListExtra(
    key: String
): List<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(key, T::class.java)
    } else {
        getParcelableArrayListExtra(key)
    }
}

fun RecyclerView.smoothScrollTo(position: Int) {
    layoutManager?.startSmoothScroll(LinearSmoothScroller(context).apply { targetPosition = position })
}

fun Context.dp2px(dp: Float): Int {
    return (resources.displayMetrics.density * dp + 0.5f).toInt()
}

fun dp2px(dp: Float): Int {
    return applicationContext.dp2px(dp)
}

fun Number.formatNumber(): String =
    formatNumber(applicationContext.getString(R.string.thousand), applicationContext.getString(R.string.million))