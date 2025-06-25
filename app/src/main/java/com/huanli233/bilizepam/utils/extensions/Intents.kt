package com.huanli233.bilizepam.utils.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.utils.MsgUtil

fun Context.startActivityOrMsg(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        MsgUtil.showMsg(getString(R.string.no_app_to_process_intent))
    }
}