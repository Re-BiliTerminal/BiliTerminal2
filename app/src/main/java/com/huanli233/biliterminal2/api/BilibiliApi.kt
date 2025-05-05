package com.huanli233.biliterminal2.api

import android.annotation.SuppressLint
import android.os.Build
import com.huanli233.biliterminal2.applicationContext
import com.huanli233.biliterminal2.applicationScope
import com.huanli233.biliterminal2.data.UserPreferences
import com.huanli233.biliterminal2.data.account.AccountRepository
import com.huanli233.biliterminal2.data.di.AppDependenciesEntryPoint
import com.huanli233.biliterminal2.utils.network.SSLSocketFactoryCompat
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.httplib.CookieManager
import com.huanli233.biliwebapi.httplib.WbiSignKeyInfo
import dagger.hilt.EntryPoints
import kotlinx.coroutines.Dispatchers
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
    applicationContext,
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

// TODO Flow
class AppCookieManager @Inject constructor(
    private val accountRepository: AccountRepository
) : CookieManager {

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookiesStr = runBlocking(Dispatchers.IO) {
            accountRepository.getActiveAccountToken()
        }?.cookies.orEmpty()
        return if (cookiesStr == "") listOf() else cookiesStr.split("; ").map {
            Cookie.Builder()
                .name(it.substringBefore("="))
                .value(it.substringAfter("="))
                .domain(url.host)
                .build()
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val uid = cookies.find { it.name == "DedeUserID" }?.value?.toLongOrNull() ?: -1
        val cookiesStr = runBlocking(Dispatchers.IO) {
            if (uid == -1L) {
                accountRepository.getActiveAccountToken()
            } else {
                accountRepository.getTokenForAccount(uid)
            }
        }?.cookies.orEmpty()
        val oldCookies = (if (cookiesStr == "") mutableListOf() else mutableListOf(
            *cookiesStr.split("; ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        ))
        cookies.forEach {
            oldCookies.add("${it.name}=${it.value}")
        }
        runBlocking(Dispatchers.IO) {
            if (uid == -1L) {
                accountRepository.updateActiveAccountToken {
                    it.copy(
                        cookies = oldCookies.joinToString("; ")
                    )
                }
            } else {
                accountRepository.updateAccountToken(uid) {
                    it.copy(
                        cookies = oldCookies.joinToString("; ")
                    )
                }
                accountRepository.setActiveAccount(uid)
            }
        }
    }

}

object WbiDataManager : com.huanli233.biliwebapi.httplib.WbiDataManager {
    override var wbiData: WbiSignKeyInfo
        get() = WbiSignKeyInfo(
            UserPreferences.wbiMixinKey.get(),
            UserPreferences.wbiLastUpdated.get(),
        )
        set(value) {
            applicationScope.launch {
                UserPreferences.wbiLastUpdated.set(value.lastUpdated)
                UserPreferences.wbiMixinKey.set(value.mixinKey)
            }
        }
}

fun <T> ApiResponse<T>?.toResult(): Result<T?> {
    return if (this?.code == 0) {
        Result.success(data)
    } else Result.failure(BilibiliApiException(this?.code ?: Int.MIN_VALUE, "The return value is not 0: ${this?.code}"))
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
    } else Result.failure(BilibiliApiException(this?.code ?: Int.MIN_VALUE, "The return value is not 0 or data is null: code=${this?.code}, data=${data}"))
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
        return "${if (cause == null) "$code $message" else cause}"
    }
}