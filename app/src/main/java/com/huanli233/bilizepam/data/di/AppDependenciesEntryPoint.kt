package com.huanli233.bilizepam.data.di

import com.huanli233.bilizepam.data.account.AccountRepository
import com.huanli233.bilizepam.data.download.DownloadDao
import com.huanli233.biliwebapi.httplib.CookieManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppDependenciesEntryPoint {
    fun cookieManager(): CookieManager
    fun accountRepository(): AccountRepository
    fun downloadDao(): DownloadDao
}