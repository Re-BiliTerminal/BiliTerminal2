package com.huanli233.biliwebapi

import com.huanli233.biliwebapi.httplib.BilibiliApiInterceptor
import com.huanli233.biliwebapi.httplib.CookieManager
import com.huanli233.biliwebapi.httplib.Domains
import com.huanli233.biliwebapi.httplib.Protocols
import com.huanli233.biliwebapi.httplib.WbiDataManager
import com.huanli233.biliwebapi.httplib.internal.GsonConverterFactory
import com.huanli233.biliwebapi.util.gson
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit

open class BiliWebApi(
    internal val cookieManager: CookieManager,
    internal val wbiDataManager: WbiDataManager
) {

    val client: OkHttpClient by lazy { createHttpClient().build() }
    protected val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(this, gson))
            .baseUrl(Protocols.HTTPS + Domains.BASE_API_URL)
            .build()
    }
    private val apiObjectsMap = mutableMapOf<Class<*>, Any>()

    protected open fun createHttpClient(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .cookieJar(
                object : CookieJar {
                    override fun loadForRequest(url: HttpUrl): List<Cookie> {
                        return cookieManager.loadForRequest(url)
                    }

                    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                        return cookieManager.saveFromResponse(url, cookies)
                    }
                }
            )
            .addInterceptor(BilibiliApiInterceptor(this))
    }

    protected fun <T> createApi(clazz: Class<T>): T =
        retrofit.create(clazz)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getApi(clazz: Class<T>): T {
        return apiObjectsMap.getOrPut(clazz) { createApi(clazz) } as T
    }

}