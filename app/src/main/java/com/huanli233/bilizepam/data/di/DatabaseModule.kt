package com.huanli233.bilizepam.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.huanli233.bilizepam.data.account.AccountDao
import com.huanli233.bilizepam.data.account.CookiesDao
import com.huanli233.bilizepam.data.database.AppDatabase
import com.huanli233.bilizepam.data.download.DownloadDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @JvmField
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `downloads` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `key` TEXT NOT NULL,
                    `url` TEXT NOT NULL,
                    `headersJson` TEXT,
                    `fileName` TEXT NOT NULL,
                    `mimeType` TEXT,
                    `contentUri` TEXT,
                    `status` TEXT NOT NULL,
                    `progress` INTEGER NOT NULL,
                    `downloadedBytes` INTEGER NOT NULL,
                    `totalBytes` INTEGER NOT NULL,
                    `errorMessage` TEXT,
                    `workId` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `finishedAt` INTEGER
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_downloads_key` ON `downloads` (`key`)")
        }
    }

    @JvmField
    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `downloads` ADD COLUMN `coverUrl` TEXT")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "biliterminal"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
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

    @Provides
    @Singleton
    fun provideDownloadDao(database: AppDatabase): DownloadDao {
        return database.downloadDao()
    }
}