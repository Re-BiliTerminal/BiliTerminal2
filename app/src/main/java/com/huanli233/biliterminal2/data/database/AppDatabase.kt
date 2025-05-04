package com.huanli233.biliterminal2.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.huanli233.biliterminal2.applicationContext
import com.huanli233.biliterminal2.data.account.AccountDao
import com.huanli233.biliterminal2.data.account.AccountEntity

@Database(entities = [AccountEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}

val database by lazy {
    Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java, "biliterminal"
    ).build()
}