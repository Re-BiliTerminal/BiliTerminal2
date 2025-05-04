package com.huanli233.biliterminal2.event

import com.google.android.material.snackbar.Snackbar

data class SnackEvent(
    var message: String? = null,
    var startTime: Long = 0,
    var duration: Int = 0
) {

    constructor(message: String) : this(message, System.currentTimeMillis(), Snackbar.LENGTH_SHORT)

    constructor(message: String, startTime: Long) : this(message, startTime, Snackbar.LENGTH_SHORT)

}
