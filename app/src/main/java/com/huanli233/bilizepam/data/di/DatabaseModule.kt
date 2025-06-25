package com.huanli233.bilizepam.data.di

import android.content.Context
import androidx.room.Room
import com.huanli233.bilizepam.data.account.AccountDao
import com.huanli233.bilizepam.data.account.CookiesDao
import com.huanli233.bilizepam.data.database.AppDatabase
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
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    @Singleton
    fun provideCookiesDao(database: AppDatabase): CookiesDao {
        return database.cookiesDao()
    }
}