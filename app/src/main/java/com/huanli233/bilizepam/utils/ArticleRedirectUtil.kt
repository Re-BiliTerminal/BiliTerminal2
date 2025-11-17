package com.huanli233.bilizepam.utils

import com.huanli233.biliwebapi.httplib.BilibiliApiInterceptor
import com.huanli233.bilizepam.api.bilibiliApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object ArticleRedirectUtil {
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(BilibiliApiInterceptor(bilibiliApi))
            .followRedirects(false)
            .build()
    }
    
    suspend fun convertCvidToOpusId(cvid: Long): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.bilibili.com/read/cv$cvid/"
            val request = Request.Builder()
                .url(url)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val location = response.header("Location")
                    ?: return@withContext Result.failure(Exception("No Location header in redirect response"))

                val opusIdRegex = """https://www.bilibili.com/opus/(\d+)""".toRegex()
                val matchResult = opusIdRegex.find(location)

                if (matchResult != null) {
                    Result.success(matchResult.groupValues[1].toLong())
                } else {
                    Result.failure(Exception("Failed to extract opus ID from Location: $location"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
