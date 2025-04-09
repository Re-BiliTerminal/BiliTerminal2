package com.huanli233.biliwebapi.api.util

import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.ILoginInfoApi
import com.huanli233.biliwebapi.httplib.WbiSignKeyInfo
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.TreeMap

object WbiUtil {
    private val MIXIN_KEY_ENC_TAB = intArrayOf(
        46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49,
        33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40,
        61, 26, 17, 0, 1, 60, 51, 30, 4, 22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11,
        36, 20, 34, 44, 52
    )

    private fun getWbiKey(api: BiliWebApi): String {
        val wbiSignKeyInfo: WbiSignKeyInfo = api.wbiDataManager.wbiData
        val currentTimeStamp = System.currentTimeMillis()
        if (((wbiSignKeyInfo.lastUpdated < currentTimeStamp && currentTimeStamp - wbiSignKeyInfo.lastUpdated < (24 * 60 * 60 * 1000))
                    || wbiSignKeyInfo.lastUpdated >= currentTimeStamp) && wbiSignKeyInfo.mixinKey.isNotEmpty()
        ) {
            return wbiSignKeyInfo.mixinKey
        } else {
            val response = runBlocking {
                api.getApi(ILoginInfoApi::class.java).requestNavInfo()
            }
            val result = response.data?.let { navInfo ->
                getWBIMixinKey(
                    getFileFirstName(
                        getFileNameFromLink(
                            navInfo.wbiImg.imgUrl
                        )
                    ) + getFileFirstName(
                        getFileNameFromLink(
                            navInfo.wbiImg.subUrl
                        )
                    )
                )
            } ?: throw IOException("Can not get wbi mixin key")
            api.wbiDataManager.wbiData = api.wbiDataManager.wbiData.copy(
                lastUpdated = currentTimeStamp,
                mixinKey = result
            )
            return result
        }
    }

    private fun getWBIMixinKey(rawKey: String): String {
        val key = StringBuilder()
        for (i in 0..31) {
            key.append(rawKey[MIXIN_KEY_ENC_TAB[i]])
        }
        return key.toString()
    }

    private fun getFileFirstName(file: String): String {
        for (i in file.indices) {
            if (file[i] == '.') {
                return file.substring(0, i)
            }
        }
        return "fail"
    }

    fun signUrl(api: BiliWebApi, url: HttpUrl): HttpUrl {
        val wts = (System.currentTimeMillis() / 1000).toString()
        val mixinKey = getWbiKey(api)
        val builder: HttpUrl.Builder = url.newBuilder().addQueryParameter("wts", wts)
        val sortedUrl = sortQueryParameters(builder.build())
        val wRid = md5(encodeUrl(sortedUrl.query) + mixinKey)
        return url.newBuilder()
            .addQueryParameter("wts", wts)
            .addQueryParameter("w_rid", wRid)
            .build()
    }

    private fun sortQueryParameters(url: HttpUrl): HttpUrl {
        val sortedQueryParameters: MutableMap<String, MutableList<String?>> = TreeMap()

        for (i in 0 until url.querySize) {
            val name = url.queryParameterName(i)
            val value = url.queryParameterValue(i)

            if (!sortedQueryParameters.containsKey(name)) {
                sortedQueryParameters[name] = ArrayList()
            }
            sortedQueryParameters[name]!!.add(value)
        }

        val urlBuilder: HttpUrl.Builder = url.newBuilder().query(null)
        for ((key, value1) in sortedQueryParameters) {
            for (value in value1) {
                urlBuilder.addQueryParameter(key, value)
            }
        }

        return urlBuilder.build()
    }

    private fun encodeUrl(input: String?): String? {
        if (input == null) {
            return null
        }

        try {
            var encoded = URLEncoder.encode(input, "UTF-8")
            encoded = encoded.replace("+", "%20")
            val result = StringBuilder()
            var i = 0
            while (i < encoded.length) {
                val c = encoded[i]
                result.append(c)
                if (c == '%' && i + 2 < encoded.length) {
                    result.append(encoded[i + 1].uppercaseChar())
                    result.append(encoded[i + 2].uppercaseChar())
                    i += 2
                }
                i++
            }
            return result.toString()
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("UTF-8 encoding is not supported", e)
        }
    }

    private fun getFileNameFromLink(link: String): String {
        val length = link.length
        for (i in length - 1 downTo 1) {
            if (link[i] == '/') {
                return link.substring(i + 1)
            }
        }
        return "fail"
    }

    private fun md5(plainText: String): String {
        val secretBytes: ByteArray
        try {
            val md = MessageDigest.getInstance("MD5")
            md.update(plainText.toByteArray())
            secretBytes = md.digest()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
        val md5code = StringBuilder(BigInteger(1, secretBytes).toString(16))
        for (i in 0 until 32 - md5code.length) {
            md5code.insert(0, "0")
        }
        return md5code.toString()
    }
}