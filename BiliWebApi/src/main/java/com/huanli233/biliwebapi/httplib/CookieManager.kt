package com.huanli233.biliwebapi.httplib

import okhttp3.Cookie
import okhttp3.HttpUrl

interface CookieManager {

    fun loadForRequest(url: HttpUrl): List<Cookie>
    fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>)

}