package com.huanli233.biliterminal2.api

import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.network.NetWorkUtil
import com.huanli233.biliterminal2.util.Preferences
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.httplib.CookieManager
import com.huanli233.biliwebapi.httplib.WbiSignKeyInfo
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

val bilibiliApi = object : BiliWebApi(
    cookieManager = CookieManager,
    wbiDataManager = WbiDataManager
) {
    override fun createHttpClient(): OkHttpClient.Builder {
        return NetWorkUtil.setOkHttpSsl(super.createHttpClient())
    }
}

object CookieManager : CookieManager {

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookiesStr = Preferences.getString(Preferences.COOKIES, "")
        return if (cookiesStr == "") listOf() else cookiesStr.split("; ").map {
            Cookie.Builder()
                .name(it.substringBefore("="))
                .value(it.substringAfter("="))
                .domain(url.host)
                .build()
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val cookiesStr = Preferences.getString(Preferences.COOKIES, "")
        val oldCookies = (if (cookiesStr == "") mutableListOf() else mutableListOf(
            *cookiesStr.split("; ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        ))
        cookies.forEach {
            oldCookies.add("${it.name}=${it.value}")
        }
        Preferences.putString(
            Preferences.COOKIES,
            oldCookies.joinToString("; ")
        )
        NetWorkUtil.refreshHeaders()
    }

}

object WbiDataManager : com.huanli233.biliwebapi.httplib.WbiDataManager {
    override var wbiData: WbiSignKeyInfo
        get() = WbiSignKeyInfo(
            Preferences.getString("wbi_mixin_key", ""),
            Preferences.getLong("wbi_last_updated", 0),
        )
        set(value) {
            Preferences.putLong("wbi_last_updated", value.lastUpdated)
            Preferences.putString("wbi_mixin_key", value.mixinKey)
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
    action: (apiException: BilibiliApiException) -> Unit = {
        MsgUtil.showMsg("")
    }
): Result<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    (exceptionOrNull() as? BilibiliApiException)?.let { action(it) }
    return this
}

class BilibiliApiException(
    val code: Int, message: String
): Exception(message) {
    override fun toString(): String {
        return "${if (cause == null) "$code $message" else cause}"
    }
}