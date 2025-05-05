package com.huanli233.biliterminal2.data.di

import com.huanli233.biliterminal2.data.account.AccountRepository
import com.huanli233.biliwebapi.httplib.CookieManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppDependenciesEntryPoint {
    fun cookieManager(): CookieManager
    fun accountRepository(): AccountRepository
}