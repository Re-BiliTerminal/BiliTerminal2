package com.huanli233.bilizepam.utils

import com.huanli233.bilizepam.ui.common.SnackbarDuration
import com.huanli233.bilizepam.ui.common.SnackbarManager

object MsgUtil {

    @JvmStatic
    fun showMsg(str: String) {
        SnackbarManager.show(str, SnackbarDuration.Short)
    }

    @JvmStatic
    fun showMsgLong(str: String) {
        SnackbarManager.show(str, SnackbarDuration.Long)
    }

}