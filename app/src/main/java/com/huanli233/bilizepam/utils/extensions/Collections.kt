package com.huanli233.bilizepam.utils.extensions

fun <T> MutableCollection<T>.addReturning(data: T): T = data.also { add(it) }