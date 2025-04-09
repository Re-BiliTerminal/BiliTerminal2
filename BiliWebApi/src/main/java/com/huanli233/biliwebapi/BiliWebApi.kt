package com.huanli233.biliwebapi

import com.google.gson.Gson
import com.huanli233.biliwebapi.bean.ApiResponse
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
import kotlin.reflect.KClass

open class BiliWebApi(
    internal val cookieManager: CookieManager,
    internal val wbiDataManager: WbiDataManager
) {

    val gson: Gson
        get() = com.huanli233.biliwebapi.util.gson

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
    fun <T: Any> getApi(clazz: Class<T>): T {
        return apiObjectsMap.getOrPut(clazz) { createApi(clazz) } as T
    }

    inline fun <reified T: Any> api(): T = getApi(T::class.java)

    suspend inline fun <T: Any, R> api(
        service: Class<T>,
        action: suspend T.() -> ApiResponse<R>
    ): Result<ApiResponse<R>> = runCatching {
        getApi(service).action()
    }

    suspend inline fun <T: Any, R> api(
        service: KClass<T>,
        action: suspend T.() -> ApiResponse<R>
    ): Result<ApiResponse<R>> = api(service.java, action)

//    suspend inline fun <reified T: Any, R> api(
//        action: suspend T.() -> ApiResponse<R>
//    ): Result<ApiResponse<R>> = runCatching {
//        api<T>().action()
//    }

}