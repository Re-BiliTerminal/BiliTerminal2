package com.huanli233.biliterminal2.utils.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment

inline fun Fragment.putArgument(
    builder: Bundle.() -> Unit
): Fragment = apply {
    arguments = Bundle().apply(builder)
}