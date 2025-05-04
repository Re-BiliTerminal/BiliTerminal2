package com.huanli233.biliterminal2.utils.extensions

fun <T> MutableCollection<T>.addReturning(data: T): T = data.also { add(it) }