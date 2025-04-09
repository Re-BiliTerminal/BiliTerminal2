package com.huanli233.biliterminal2.util.extensions

import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt

@ColorInt @Suppress("NOTHING_TO_INLINE")
inline fun String.toColorIntOrNull() = runCatching {
    toColorInt()
}.getOrNull()