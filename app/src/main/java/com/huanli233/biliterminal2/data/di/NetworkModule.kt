package com.huanli233.biliterminal2.data.di

import com.huanli233.biliterminal2.api.AppCookieManager
import com.huanli233.biliwebapi.httplib.CookieManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideCookieJar(appCookieJar: AppCookieManager): CookieManager {
        return appCookieJar
    }

}