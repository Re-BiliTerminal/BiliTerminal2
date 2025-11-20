package com.huanli233.bilizepam.data.repository

import android.content.Context
import com.huanli233.biliwebapi.api.interfaces.ILoginApi
import com.huanli233.bilizepam.api.apiResultNonNull
import com.huanli233.bilizepam.api.bilibiliApi
import com.huanli233.biliwebapi.api.interfaces.IUserApi
import com.huanli233.biliwebapi.bean.user.NavUserInfo
import com.huanli233.biliwebapi.bean.user.UserArticle
import com.huanli233.biliwebapi.bean.user.UserCard
import com.huanli233.biliwebapi.bean.user.UserCardInfo
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.bilizepam.api.apiResult
import com.huanli233.bilizepam.data.account.AccountManager
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    suspend fun getMyInfo(): Result<NavUserInfo> {
        return bilibiliApi.api(IUserApi::class) {
            getMyInfo()
        }.apiResultNonNull()
    }

    suspend fun getUserCard(mid: Long): Result<UserCardInfo> {
        return bilibiliApi.api(IUserApi::class) {
            getCard(mid.toString())
        }.apiResultNonNull()
    }

    suspend fun getNotice(mid: Long): Result<String> {
        return bilibiliApi.api(IUserApi::class) {
            getNotice(mid.toString())
        }.apiResultNonNull()
    }

    suspend fun getUserVideos(mid: Long, page: Int): Result<List<VideoInfo>> {
        return bilibiliApi.api(IUserApi::class) {
            getUserVideos(mid, page)
        }.apiResultNonNull().map { it.list.vlist }
    }

    suspend fun getUserArticles(mid: Long, page: Int): Result<List<UserArticle>> {
        return bilibiliApi.api(IUserApi::class) {
            getUserArticles(mid, page)
        }.apiResultNonNull().map { it.articles }
    }

    suspend fun followUser(mid: Long, isFollow: Boolean): Result<Int> {
        val act = if (isFollow) 1 else 2
        
        return bilibiliApi.api(IUserApi::class) {
            followUser(mid, act)
        }.apiResult().map { 0 }
    }
    
    suspend fun logout(): Result<Unit> {
        return bilibiliApi.api(ILoginApi::class) {
            exitLogin(bilibiliApi.cookieManager.loadForRequest("https://www.bilibili.com".toHttpUrl()).find { it.name == "bili_jct" }?.value.orEmpty())
        }.apiResult().map {
            AccountManager.repository.removeAccount(
                AccountManager.currentAccount.accountId
            )
            AccountManager.repository.setActiveAccount(0)
        }
    }

}
