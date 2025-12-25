package com.huanli233.bilizepam.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.huanli233.bilizepam.data.account.AccountDao
import com.huanli233.bilizepam.data.account.AccountEntity
import com.huanli233.bilizepam.data.account.CookieEntity
import com.huanli233.bilizepam.data.account.CookiesDao
import com.huanli233.bilizepam.data.download.DownloadDao
import com.huanli233.bilizepam.data.download.DownloadEntity
import com.huanli233.bilizepam.data.download.DownloadTypeConverters

@Database(
    entities = [AccountEntity::class, CookieEntity::class, DownloadEntity::class],
    version = 3
)
@TypeConverters(DownloadTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun cookiesDao(): CookiesDao
    abstract fun downloadDao(): DownloadDao
}