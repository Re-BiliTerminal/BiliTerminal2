package com.huanli233.bilizepam.data.di

import com.huanli233.bilizepam.api.AppCookieManager
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