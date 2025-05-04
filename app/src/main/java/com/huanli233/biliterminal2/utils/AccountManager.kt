package com.huanli233.biliterminal2.utils

import com.huanli233.biliterminal2.BiliTerminal
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.applicationContext
import com.huanli233.biliterminal2.applicationScope
import com.huanli233.biliterminal2.data.account.Account
import com.huanli233.biliterminal2.data.account.AccountRepository
import com.huanli233.biliterminal2.utils.AccountManager.currentAccount
import kotlinx.coroutines.launch

object AccountManager {

    private var _currentAccount: Account? = null
    val currentAccount: Account
        get() = _currentAccount ?: Account(
            accountId = 0,
            username = "Null",
            avatarUrl = null,
            lastActiveTime = 0
        )

    init {
        applicationScope.launch {
            AccountRepository.getInstance(applicationContext).activeAccount.collect {
                _currentAccount = it
            }
        }
    }

}

inline fun requireLoggedIn(
    displayMsg: Boolean = true,
    block: (Account) -> Unit
) {
    if (currentAccount.accountId == 0L && displayMsg) {
        runOnUi {
            MsgUtil.showMsg(applicationContext.getString(R.string.not_logged_in))
        }
    } else {
        block(currentAccount)
    }
}

inline fun runIfNotLoggedIn(
    block: (() -> Unit) = { }
) {
    if (currentAccount.accountId == 0L) {
        block()
    }
}