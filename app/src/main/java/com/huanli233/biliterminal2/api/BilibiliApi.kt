package com.huanli233.biliterminal2.api

import com.huanli233.biliterminal2.util.NetWorkUtil
import com.huanli233.biliterminal2.util.SharedPreferencesUtil
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.httplib.CookieManager
import com.huanli233.biliwebapi.httplib.WbiSignKeyInfo
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.io.IOException

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
        val cookiesStr = SharedPreferencesUtil.getString(SharedPreferencesUtil.COOKIES, "")
        return if (cookiesStr == "") listOf() else cookiesStr.split("; ").map {
            Cookie.Builder()
                .name(it.substringBefore("="))
                .value(it.substringAfter("="))
                .domain(url.host)
                .build()
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val cookiesStr = SharedPreferencesUtil.getString(SharedPreferencesUtil.COOKIES, "")
        val oldCookies = (if (cookiesStr == "") mutableListOf() else mutableListOf(
            *cookiesStr.split("; ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        ))
        cookies.forEach {
            oldCookies.add("${it.name}=${it.value}")
        }
        SharedPreferencesUtil.putString(
            SharedPreferencesUtil.COOKIES,
            oldCookies.joinToString("; ")
        )
        NetWorkUtil.refreshHeaders()
    }

}

object WbiDataManager : com.huanli233.biliwebapi.httplib.WbiDataManager {
    override var wbiData: WbiSignKeyInfo
        get() = WbiSignKeyInfo(
            SharedPreferencesUtil.getString("wbi_mixin_key", ""),
            SharedPreferencesUtil.getLong("wbi_last_updated", 0),
        )
        set(value) {
            SharedPreferencesUtil.putLong("wbi_last_updated", value.lastUpdated)
            SharedPreferencesUtil.putString("wbi_mixin_key", value.mixinKey)
        }
}

fun <T> ApiResponse<T>?.toResult(): Result<T?> {
    return if (this?.code == 0) {
        Result.success(data)
    } else Result.failure(IOException("The return value is not 0: ${this?.code}"))
}

fun <T> ApiResponse<T>?.toResultNonNull(): Result<T> {
    val data = this?.data
    return if (this?.code == 0 && data != null) {
        Result.success(data)
    } else Result.failure(IOException("The return value is not 0 or data is null: code=${this?.code}, data=${data}"))
}