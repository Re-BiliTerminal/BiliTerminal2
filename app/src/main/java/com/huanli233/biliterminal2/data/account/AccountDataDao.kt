package com.huanli233.biliterminal2.data.account

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE accountId = :accountId LIMIT 1")
    suspend fun getAccountById(accountId: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE accountId = :accountId")
    suspend fun deleteAccountById(accountId: Long)
}

@Dao
interface CookiesDao {

    @Query("SELECT * FROM cookies")
    fun getCookiesFlow(): Flow<List<CookieEntity>>

    @Query("SELECT * FROM cookies WHERE accountId = :accountId")
    suspend fun getCookiesByAccountId(accountId: Long): List<CookieEntity>

    @Query("SELECT * FROM cookies WHERE accountId IS NULL OR accountId = 0")
    suspend fun getGuestCookies(): List<CookieEntity>

    @Query("DELETE FROM cookies WHERE accountId = :accountId")
    suspend fun deleteCookiesByAccountId(accountId: Long)

    @Query("select * from cookies where name=:name and accountId=:accountId")
    suspend fun getCookieByName(name: String, accountId: Long?): CookieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCookies(vararg cookie: CookieEntity)

    @Delete
    suspend fun deleteCookie(cookie: CookieEntity)

    @Query("DELETE FROM cookies WHERE name = :name AND domain = :domain AND path = :path")
    suspend fun deleteCookieByInfo(name: String, domain: String, path: String)

    @Query("SELECT * FROM cookies WHERE name = :name AND accountId = :accountId")
    suspend fun findCookiesByNameAndAccountId(name: String, accountId: Long?): List<CookieEntity>

    @Transaction
    suspend fun upsertByNameAndAccountId(cookie: CookieEntity) {
        val existingCookies = findCookiesByNameAndAccountId(cookie.name, cookie.accountId)

        existingCookies.forEach { existing ->
            deleteCookieByInfo(existing.name, existing.domain.orEmpty(), existing.path.orEmpty())
        }

        insertCookies(cookie)
    }

}