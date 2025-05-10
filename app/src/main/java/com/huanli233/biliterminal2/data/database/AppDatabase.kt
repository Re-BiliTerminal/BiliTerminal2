package com.huanli233.biliterminal2.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.huanli233.biliterminal2.data.account.AccountDao
import com.huanli233.biliterminal2.data.account.AccountEntity
import com.huanli233.biliterminal2.data.account.CookieEntity
import com.huanli233.biliterminal2.data.account.CookiesDao

@Database(
    entities = [AccountEntity::class, CookieEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun cookiesDao(): CookiesDao
}