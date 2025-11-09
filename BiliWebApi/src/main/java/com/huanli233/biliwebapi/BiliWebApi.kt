package com.huanli233.biliwebapi

import android.util.Log
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
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
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
                    .addInterceptor(ApiDebugInterceptor())
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

class ApiDebugInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Log request details
        if (ApiDebugSettings.isRequestLoggingEnabled()) {
            ApiDebugLogger.logFullRequest(request)
        }

        // Proceed with the request
        val response = chain.proceed(request)

        // Log response details
        if (ApiDebugSettings.isResponseLoggingEnabled()) {
            ApiDebugLogger.logFullResponse(response)
        }

        return response
    }
}

object ApiDebugSettings {
    val ENABLE_API_DEBUG = true
    const val ENABLE_REQUEST_LOGGING = true
    const val ENABLE_RESPONSE_LOGGING = true
    const val ENABLE_COOKIE_LOGGING = true

    fun isDebugEnabled(): Boolean = ENABLE_API_DEBUG
    fun isRequestLoggingEnabled(): Boolean = ENABLE_API_DEBUG && ENABLE_REQUEST_LOGGING
    fun isResponseLoggingEnabled(): Boolean = ENABLE_API_DEBUG && ENABLE_RESPONSE_LOGGING
    fun isCookieLoggingEnabled(): Boolean = ENABLE_API_DEBUG && ENABLE_COOKIE_LOGGING
}

object ApiDebugLogger {
    private const val TAG = "ApiDebugLogger"

    fun logCookiesForRequest(url: HttpUrl, cookies: List<Cookie>) {
        if (!ApiDebugSettings.isCookieLoggingEnabled()) return

        Log.d(TAG, "Loading cookies for request: ${url.host}")
        if (cookies.isEmpty()) {
            Log.w(TAG, "  No cookies available for this request")
        } else {
            Log.d(TAG, "  Found ${cookies.size} cookies:")
            cookies.forEach { cookie ->
                Log.d(TAG, "    ${cookie.name}=${cookie.value} (domain: ${cookie.domain})")
            }
        }
    }

    fun logCookiesFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (!ApiDebugSettings.isCookieLoggingEnabled()) return

        Log.d(TAG, "Saving cookies from response: ${url.host}")
        if (cookies.isEmpty()) {
            Log.d(TAG, "  No cookies in response")
        } else {
            Log.d(TAG, "  Received ${cookies.size} cookies:")
            cookies.forEach { cookie ->
                Log.d(TAG, "    ${cookie.name}=${cookie.value} (domain: ${cookie.domain}, expires: ${cookie.expiresAt})")
            }
        }
    }

    fun logAuthenticationStatus(hasValidCookies: Boolean, userId: Long?) {
        if (!ApiDebugSettings.isCookieLoggingEnabled()) return

        Log.d(TAG, "Authentication status:")
        Log.d(TAG, "  Has valid cookies: $hasValidCookies")
        Log.d(TAG, "  User ID: ${userId ?: "Not logged in"}")

        if (!hasValidCookies) {
            Log.w(TAG, "  WARNING: No valid authentication cookies found!")
            Log.w(TAG, "  This may cause 403 (Access Denied) errors")
        }
    }

    fun logRequestHeaders(request: Request) {
        if (!ApiDebugSettings.isRequestLoggingEnabled()) return

        Log.d(TAG, "Request Headers for ${request.method} ${request.url}:")
        val headers = request.headers
        if (headers.size == 0) {
            Log.d(TAG, "  No headers")
        } else {
            for (i in 0 until headers.size) {
                val name = headers.name(i)
                val value = if (name.lowercase().contains("cookie") ||
                    name.lowercase().contains("authorization") ||
                    name.lowercase().contains("token")) {
                    "[REDACTED]"
                } else {
                    headers.value(i)
                }
                Log.d(TAG, "  $name: $value")
            }
        }
    }

    fun logResponseHeaders(response: Response) {
        if (!ApiDebugSettings.isResponseLoggingEnabled()) return

        Log.d(TAG, "Response Headers for ${response.request.url}:")
        Log.d(TAG, "  Status: ${response.code} ${response.message}")
        val headers = response.headers
        if (headers.size == 0) {
            Log.d(TAG, "  No headers")
        } else {
            for (i in 0 until headers.size) {
                val name = headers.name(i)
                val value = if (name.lowercase().contains("set-cookie")) {
                    "[COOKIE_DATA]"
                } else {
                    headers.value(i)
                }
                Log.d(TAG, "  $name: $value")
            }
        }
    }

    fun logFullRequest(request: Request) {
        if (!ApiDebugSettings.isRequestLoggingEnabled()) return

        Log.d(TAG, "=== Full Request Debug ===")
        Log.d(TAG, "URL: ${request.url}")
        Log.d(TAG, "Method: ${request.method}")

        // Log headers
        logRequestHeaders(request)

        // Log body if present
        request.body?.let { body ->
            Log.d(TAG, "Body Content-Type: ${body.contentType()}")
            Log.d(TAG, "Body Size: ${body.contentLength()} bytes")
        }
        Log.d(TAG, "=== End Request Debug ===")
    }

    fun logFullResponse(response: Response) {
        if (!ApiDebugSettings.isResponseLoggingEnabled()) return

        Log.d(TAG, "=== Full Response Debug ===")
        Log.d(TAG, "URL: ${response.request.url}")
        Log.d(TAG, "Status: ${response.code} ${response.message}")
        Log.d(TAG, "Protocol: ${response.protocol}")

        // Log headers
        logResponseHeaders(response)

        // Log response body for error responses or pagination requests
        if (response.code != 200 || response.request.url.toString().contains("pagination_str")) {
            logResponseBody(response)
        }

        Log.d(TAG, "=== End Response Debug ===")
    }

    fun logResponseBody(response: Response) {
        if (!ApiDebugSettings.isResponseLoggingEnabled()) return

        try {
            val responseBody = response.peekBody(Long.MAX_VALUE)
            val bodyString = responseBody.string()
            
            Log.d(TAG, "Response Body:")
            if (bodyString.length > 2000) {
                Log.d(TAG, "  ${bodyString.take(2000)}... (truncated)")
            } else {
                Log.d(TAG, "  $bodyString")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read response body: ${e.message}")
        }
    }

    fun logWbiSigningProcess(originalUrl: String, params: Map<String, String>, wbiKey: String, finalSignature: String) {
        if (!ApiDebugSettings.isRequestLoggingEnabled()) return
        
        Log.d(TAG, "=== WBI Signing Process ===")
        Log.d(TAG, "Original URL: $originalUrl")
        Log.d(TAG, "WBI Key: $wbiKey")
        Log.d(TAG, "Parameters before signing:")
        params.toSortedMap().forEach { (key, value) ->
            Log.d(TAG, "  $key = $value")
        }
        Log.d(TAG, "Final w_rid signature: $finalSignature")
        Log.d(TAG, "=== End WBI Signing ===")
    }
    
    fun logWbiKeyInfo(imgKey: String, subKey: String, mixinKey: String, lastUpdated: Long) {
        if (!ApiDebugSettings.isRequestLoggingEnabled()) return
        
        Log.d(TAG, "=== WBI Key Info ===")
        Log.d(TAG, "IMG Key: $imgKey")
        Log.d(TAG, "SUB Key: $subKey") 
        Log.d(TAG, "Mixin Key: $mixinKey")
        Log.d(TAG, "Last Updated: $lastUpdated")
        Log.d(TAG, "Key Age: ${System.currentTimeMillis() - lastUpdated}ms")
        if (System.currentTimeMillis() - lastUpdated > 24 * 60 * 60 * 1000) {
            Log.w(TAG, "WARNING: WBI keys are older than 24 hours, may need refresh")
        }
        Log.d(TAG, "=== End WBI Key Info ===")
    }
    
    fun logWbiError(error: String, params: Map<String, String>? = null) {
        Log.e(TAG, "=== WBI Error ===")
        Log.e(TAG, "Error: $error")
        params?.let {
            Log.e(TAG, "Parameters:")
            it.toSortedMap().forEach { (key, value) ->
                Log.e(TAG, "  $key = $value")
            }
        }
        Log.e(TAG, "=== End WBI Error ===")
    }

    fun logApiError(apiName: String, code: Int, message: String?) {
        Log.e(TAG, "API Error in $apiName:")
        Log.e(TAG, "  Code: $code")
        Log.e(TAG, "  Message: $message")

        when (code) {
            -403 -> {
                Log.e(TAG, "  Diagnosis: Access denied - likely authentication or WBI signature issue")
                Log.e(TAG, "  Solution: Check if user is logged in, cookies are valid, and WBI signature is correct")
                Log.e(TAG, "  Check: Verify User-Agent, Referer, Origin headers and WBI key freshness")
            }
            -400 -> {
                Log.e(TAG, "  Diagnosis: Bad request - check request parameters or WBI signature")
            }
            -404 -> {
                Log.e(TAG, "  Diagnosis: Resource not found - check if video/content exists")
            }
            -412 -> {
                Log.e(TAG, "  Diagnosis: Precondition failed - likely missing or invalid headers/signature")
            }
            else -> {
                Log.e(TAG, "  Diagnosis: Unknown error code")
            }
        }
    }
}
