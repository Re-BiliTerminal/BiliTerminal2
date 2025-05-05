package com.huanli233.biliterminal2.data.di

import android.content.Context
import androidx.room.Room
import com.huanli233.biliterminal2.data.account.AccountDao
import com.huanli233.biliterminal2.data.account.AccountSecureStorage
import com.huanli233.biliterminal2.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "biliterminal"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAccountSecureStorage(
        @ApplicationContext context: Context
    ): AccountSecureStorage {
        return AccountSecureStorage(context)
    }

    @Provides
    @Singleton
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.accountDao()
    }
}