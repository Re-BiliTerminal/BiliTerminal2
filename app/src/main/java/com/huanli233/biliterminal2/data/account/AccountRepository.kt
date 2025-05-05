package com.huanli233.biliterminal2.data.account

import android.content.Context
import com.huanli233.biliterminal2.applicationScope
import com.huanli233.biliterminal2.data.PreferenceKeys
import com.huanli233.biliterminal2.data.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

const val DEFAULT_ACCOUNT_DATA_ID = 0L

class AccountRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountDao: AccountDao,
    private val accountSecureStorage: AccountSecureStorage,
) {

    private val ioDispatcher: CoroutineContext = Dispatchers.IO

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeAccount: StateFlow<Account?> = accountSecureStorage.getActiveAccountIdFlow()
        .flatMapLatest { activeAccountId ->
            if (activeAccountId != null) {
                accountDao.getAccountById(activeAccountId)
                    ?.let { flowOf(it.toAccount()) }
                    ?: flowOf(null)
            } else {
                flowOf(null)
            }
        }.flowOn(ioDispatcher).stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private fun AccountSecureStorage.getActiveAccountIdFlow(): Flow<Long?> {
        return UserPreferences.impl.data().map {
            it[PreferenceKeys.ACTIVE_ACCOUNT_ID.first]
        }
    }

    suspend fun addAccount(account: Account, tokenData: AccountTokenData) = withContext(ioDispatcher) {
        accountDao.insertAccount(account.toEntity())
        accountSecureStorage.saveTokenData(account.accountId, tokenData)
    }

    suspend fun removeAccount(accountId: Long) = withContext(ioDispatcher) {
        accountDao.deleteAccountById(accountId)
        accountSecureStorage.deleteAccountData(accountId)
        if (UserPreferences.activeAccountId.get() == accountId) {
            setActiveAccount(DEFAULT_ACCOUNT_DATA_ID)
        }
    }

    suspend fun setActiveAccount(accountId: Long) = withContext(ioDispatcher) {
        UserPreferences.activeAccountId.set(accountId)
    }

    suspend fun getTokenForAccount(accountId: Long): AccountTokenData? = withContext(ioDispatcher) {
        accountSecureStorage.getTokenData(accountId)
    }

    suspend fun saveTokenForAccount(accountId: Long, tokenData: AccountTokenData) = withContext(ioDispatcher) {
        accountSecureStorage.saveTokenData(accountId, tokenData)
    }

    suspend fun getActiveAccountToken(): AccountTokenData? = withContext(ioDispatcher) {
        val activeId = UserPreferences.activeAccountId.get()
        accountSecureStorage.getTokenData(activeId)
    }

    suspend fun saveActiveAccountToken(tokenData: AccountTokenData) = withContext(ioDispatcher) {
        val activeId = UserPreferences.activeAccountId.get()
        accountSecureStorage.saveTokenData(activeId, tokenData)
    }

    suspend inline fun updateAccountToken(
        accountId: Long,
        block: (AccountTokenData) -> AccountTokenData
    ) {
        getTokenForAccount(accountId)?.let {
            saveTokenForAccount(
                accountId,
                block(it)
            )
        }
    }

    suspend inline fun updateActiveAccountToken(
        block: (AccountTokenData) -> AccountTokenData
    ) {
        saveActiveAccountToken(
            block((getActiveAccountToken() ?: AccountTokenData()))
        )
    }

}

fun AccountEntity.toAccount(): Account {
    return Account(accountId, username, avatarUrl, lastActiveTime)
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(accountId, username, avatarUrl, lastActiveTime)
}