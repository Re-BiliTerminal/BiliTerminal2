package com.huanli233.biliterminal2.util

import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.contextNotNull
import com.huanli233.biliterminal2.util.AccountManager.currentAccount

object AccountManager {

    val currentAccount: Account
        get() {
            return Account(
                Preferences.getLong("mid", 0)
            )
        }

}

inline fun requireLoggedIn(
    displayMsg: Boolean = true,
    block: (Account) -> Unit
) {
    if (currentAccount.mid == 0L && displayMsg) {
        runOnUi {
            MsgUtil.showMsg(contextNotNull.getString(R.string.not_logged_in))
        }
    } else {
        block(currentAccount)
    }
}

inline fun runIfNotLoggedIn(
    block: (() -> Unit) = { }
) {
    if (currentAccount.mid == 0L) {
        block()
    }
}

data class Account(
    val mid: Long
)