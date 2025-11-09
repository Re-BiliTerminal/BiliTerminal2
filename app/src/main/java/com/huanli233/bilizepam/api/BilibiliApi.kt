package com.huanli233.bilizepam.api

import android.annotation.SuppressLint
import android.os.Build
import com.huanli233.biliwebapi.ApiDebugLogger
import com.huanli233.bilizepam.BiliTerminal
import com.huanli233.bilizepam.applicationScope
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.data.account.AccountRepository
import com.huanli233.bilizepam.data.account.CookieEntity
import com.huanli233.bilizepam.data.account.toCookieEntity
import com.huanli233.bilizepam.data.di.AppDependenciesEntryPoint
import com.huanli233.bilizepam.data.setting.edit
import com.huanli233.bilizepam.utils.network.SSLSocketFactoryCompat
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.httplib.CookieManager
import com.huanli233.biliwebapi.httplib.WbiSignKeyInfo
import dagger.hilt.EntryPoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private val hiltEntryPoint = EntryPoints.get(
    BiliTerminal.application,
    AppDependenciesEntryPoint::class.java
)

val bilibiliApi = object : BiliWebApi(
    cookieManager = hiltEntryPoint.cookieManager(),
    wbiDataManager = WbiDataManager
) {
    override fun createHttpClient(): OkHttpClient.Builder {
        return setOkHttpSsl(super.createHttpClient())
    }
}

@Synchronized
private fun setOkHttpSsl(okhttpBuilder: OkHttpClient.Builder): OkHttpClient.Builder {
    if (Build.VERSION.SDK_INT > 22) return okhttpBuilder
    try {
        @SuppressLint("CustomX509TrustManager") val trustAllCert: X509TrustManager =
            object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate?> {
                    return arrayOf<X509Certificate?>()
                }
            }
        val sslSocketFactory: SSLSocketFactory = SSLSocketFactoryCompat(trustAllCert)
        okhttpBuilder.sslSocketFactory(sslSocketFactory, trustAllCert)
    } catch (e: java.lang.Exception) {
        throw RuntimeException(e)
    }
    return okhttpBuilder
}

class AppCookieManager @Inject constructor(
    private val accountRepository: AccountRepository
) : CookieManager {

    val allCookies: StateFlow<List<CookieEntity>> =
        accountRepository.getCookiesFlow()
            .stateIn(
                scope = applicationScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    val accountCookies: StateFlow<List<CookieEntity>> =
        combine(
            allCookies,
            accountRepository.activeAccount
        ) { latestCookies, latestAccount ->
            latestCookies.filter { cookie ->
                cookie.accountId == null || cookie.accountId == (latestAccount?.accountId ?: 0)
            }
        }.stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val currentAccountCookiesCache = mutableListOf<CookieEntity>()

    private var activeAccountCollectorJob: Job? = null

    init {
        applicationScope.launch(Dispatchers.IO) {
            val initialActiveAccount = accountRepository.activeAccount.first { it != null }
            loadCookiesIntoCacheForAccount(initialActiveAccount?.accountId ?: 0)

            activeAccountCollectorJob = applicationScope.launch(Dispatchers.IO) {
                accountRepository.activeAccount.collect { account ->
                    loadCookiesIntoCacheForAccount(account?.accountId ?: 0)
                }
            }
        }
    }

    private suspend fun loadCookiesIntoCacheForAccount(accountId: Long) {
        val cookiesForAccount = accountRepository.getCookiesById(accountId)
        synchronized(currentAccountCookiesCache) {
            currentAccountCookiesCache.clear()
            currentAccountCookiesCache.addAll(cookiesForAccount)
        }
    }


    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        synchronized(currentAccountCookiesCache) {
            val cookies = currentAccountCookiesCache.map { it.toOkHttpCookie() }
            ApiDebugLogger.logCookiesForRequest(url, cookies)
            return cookies
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        ApiDebugLogger.logCookiesFromResponse(url, cookies)
        val uidInCookies = cookies.find { it.name == "DedeUserID" }?.value?.toLongOrNull()
        val uid = uidInCookies ?: accountRepository.activeAccount.value?.accountId ?: 0
        val cookieEntities = cookies.map { it.toCookieEntity(uid) }

        runBlocking {
            if (uidInCookies != null) {
                accountRepository.setActiveAccount(uid)
                accountRepository.activeAccount
            }
            accountRepository.addCookies(cookieEntities)

            synchronized(currentAccountCookiesCache) {
                currentAccountCookiesCache.removeAll { it.accountId == uid && cookieEntities.any { entity -> entity.name == it.name } }
                currentAccountCookiesCache.addAll(cookieEntities)
            }
        }
    }
}

object WbiDataManager : com.huanli233.biliwebapi.httplib.WbiDataManager {
    override var wbiData: WbiSignKeyInfo
        get() = WbiSignKeyInfo(
            LocalData.settings.apiCache.wbiMixinKey,
            LocalData.settings.apiCache.wbiLastUpdated,
        )
        set(value) {
            applicationScope.launch {
                LocalData.edit {
                    apiCache = apiCache.edit {
                        wbiLastUpdated = value.lastUpdated
                        wbiMixinKey = value.mixinKey
                    }
                }
            }
        }
}

fun <T> ApiResponse<T>?.toResult(): Result<T?> {
    return if (this?.code == 0) {
        Result.success(data)
    } else Result.failure(BilibiliApiException(this?.code ?: Int.MIN_VALUE, "${this?.code} ${this?.message}"))
}

fun <T> Result<ApiResponse<T>>.apiResult(): Result<T?> {
    onSuccess {
        return it.toResult()
    }
    onFailure {
        return Result.failure(it)
    }
    return Result.failure(IllegalStateException())
}

fun <T> ApiResponse<T>?.toResultNonNull(): Result<T> {
    val data = this?.data
    return if (this?.code == 0 && data != null) {
        Result.success(data)
    } else Result.failure(BilibiliApiException(this?.code ?: Int.MIN_VALUE, "code=${this?.code} ${this?.message}, data=${data}"))
}

fun <T> Result<ApiResponse<T>>.apiResultNonNull(): Result<T> {
    onSuccess {
        return it.toResultNonNull()
    }
    onFailure {
        return Result.failure(it)
    }
    return Result.failure(IllegalStateException())
}

@OptIn(ExperimentalContracts::class)
inline fun <T> Result<T>.onApiFailure(
    action: (apiException: BilibiliApiException) -> Unit
): Result<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    (exceptionOrNull() as? BilibiliApiException)?.let { action(it) }
    return this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> Result<T>.onNonApiFailure(
    action: (throwable: Throwable) -> Unit
): Result<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    exceptionOrNull()?.takeIf { it !is BilibiliApiException }?.let { action(it) }
    return this
}

class BilibiliApiException(
    val code: Int, message: String
): Exception(message) {
    override fun toString(): String {
        return "${if (cause == null) "$message" else cause}"
    }
}