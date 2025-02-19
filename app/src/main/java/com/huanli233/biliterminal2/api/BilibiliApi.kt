package com.huanli233.biliterminal2.api

import com.huanli233.biliterminal2.util.NetWorkUtil
import com.huanli233.biliterminal2.util.SharedPreferencesUtil
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.httplib.CookieManager
import com.huanli233.biliwebapi.httplib.WbiSignKeyInfo
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

val bilibiliApi = object : BiliWebApi(
    cookieManager = CookieManager,
    wbiDataManager = WbiDataManager
) {
    override fun createHttpClient(): OkHttpClient {
        return NetWorkUtil.setOkHttpSsl(super.createHttpClient().newBuilder()).build()
    }
}

object CookieManager : CookieManager {

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookiesStr = SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, "")
        return if (cookiesStr == "") listOf() else cookiesStr.split("; ").map {
            Cookie.Builder()
                .name(it.substringBefore("="))
                .value(it.substringAfter("="))
                .domain(url.host)
                .build()
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val cookiesStr = SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, "")
        val oldCookies = (if (cookiesStr == "") mutableListOf() else mutableListOf(
            *cookiesStr.split("; ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        ))
        cookies.forEach {
            oldCookies.add("${it.name}=${it.value}")
        }
        SharedPreferencesUtil.putString(
            SharedPreferencesUtil.cookies,
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