package com.huanli233.bilizepam.data.account

import com.huanli233.bilizepam.BiliTerminal
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.applicationContext
import com.huanli233.bilizepam.data.di.AppDependenciesEntryPoint
import com.huanli233.bilizepam.data.account.AccountManager.currentAccount
import com.huanli233.bilizepam.utils.MsgUtil
import com.huanli233.bilizepam.utils.runOnUi
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object AccountManager {

    val repository by lazy {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            BiliTerminal.application,
            AppDependenciesEntryPoint::class.java
        )
        hiltEntryPoint.accountRepository()
    }

    val currentAccount: AccountEntity
        get() = repository.activeAccount.value ?: runBlocking { repository.activeAccount.first { it != null } } ?: emptyAccount

    fun loggedIn() = currentAccount.accountId != 0L

}

inline fun requireLoggedIn(
    displayMsg: Boolean = true,
    block: (AccountEntity) -> Unit
) {
    if (!AccountManager.loggedIn() && displayMsg) {
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
    if (!AccountManager.loggedIn()) {
        block()
    }
}