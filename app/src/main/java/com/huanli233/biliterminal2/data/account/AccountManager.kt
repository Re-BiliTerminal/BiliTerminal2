package com.huanli233.biliterminal2.data.account

import android.util.Log
import com.huanli233.biliterminal2.BiliTerminal
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.applicationContext
import com.huanli233.biliterminal2.applicationScope
import com.huanli233.biliterminal2.data.di.AppDependenciesEntryPoint
import com.huanli233.biliterminal2.data.account.AccountManager.currentAccount
import com.huanli233.biliterminal2.data.setting.DataStore
import com.huanli233.biliterminal2.utils.MsgUtil
import com.huanli233.biliterminal2.utils.runOnUi
import dagger.hilt.EntryPoints
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object AccountManager {

    private val repository by lazy {
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