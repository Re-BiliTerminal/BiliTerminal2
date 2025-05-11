package com.huanli233.biliterminal2.data.account

import android.util.Log
import com.huanli233.biliterminal2.BiliTerminal
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.applicationContext
import com.huanli233.biliterminal2.data.di.AppDependenciesEntryPoint
import com.huanli233.biliterminal2.data.account.AccountManager.currentAccount
import com.huanli233.biliterminal2.utils.MsgUtil
import com.huanli233.biliterminal2.utils.runOnUi
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
        get() = repository.activeAccount.value.also { Log.d("huanli233", "activeAccount=${it}") } ?: runBlocking { repository.activeAccount.first { it != null } }.also { Log.d("huanli233", "firstNotNull=${it}") } ?: emptyAccount

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