package com.huanli233.biliterminal2.data.account

import android.util.Log
import com.huanli233.biliterminal2.applicationScope
import com.huanli233.biliterminal2.data.setting.DataStore
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

const val DEFAULT_ACCOUNT_DATA_ID = 0L

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val cookiesDao: CookiesDao
) {

    private val dispatcher: CoroutineContext = Dispatchers.IO

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeAccount: StateFlow<AccountEntity?> = DataStore.appSettingsStateFlow
        .map {
            it?.activeAccountId
        }
        .mapLatest { id ->
            id?.let {
                accountDao.getAccountById(id)
            } ?: emptyAccount
        }.flowOn(dispatcher).stateIn(
            scope = applicationScope,
            started = Eagerly,
            initialValue = null
        )

    private val activeAccountId
        get() = DataStore.appSettings.activeAccountId

    suspend fun setActiveAccount(accountId: Long) = DataStore.editData {
        activeAccountId = accountId
    }

    suspend fun addAccount(account: AccountEntity) = withContext(dispatcher) {
        accountDao.insertAccount(account)
    }

    suspend fun removeAccount(accountId: Long) = withContext(dispatcher) {
        accountDao.deleteAccountById(accountId)
        cookiesDao.deleteCookiesByAccountId(accountId)
        if (DataStore.appSettings.activeAccountId == accountId) {
            setActiveAccount(DEFAULT_ACCOUNT_DATA_ID)
        }
    }

    suspend fun updateAccount(account: AccountEntity) = withContext(dispatcher) {
        accountDao.updateAccount(account)
    }

    suspend fun getAccountById(accountId: Long) = withContext(dispatcher) {
        accountDao.getAccountById(accountId)
    }

    fun getCookiesFlow() = cookiesDao.getCookiesFlow()

    suspend fun getCookie(name: String) = withContext(dispatcher) {
        cookiesDao.getCookieByName(name, activeAccountId)
    }

    suspend fun getCookies() = withContext(dispatcher) {
        cookiesDao.getCookiesByAccountId(activeAccountId)
    }

    suspend fun getGuestCookies() = withContext(dispatcher) {
        cookiesDao.getGuestCookies()
    }

    suspend fun addCookies(cookies: List<CookieEntity>) = withContext(dispatcher) {
        cookies.forEach {
            cookiesDao.upsertByNameAndAccountId(it)
        }
    }

}