package com.huanli233.biliwebapi.httplib

import okhttp3.FormBody
import java.net.URLDecoder

internal object HttpUtils {

    fun String.parseFormBody(): FormBody.Builder {
        val builder = FormBody.Builder()
        if (isBlank()) return builder
        split("&").forEach { pair ->

            val keyValue = pair.split("=", limit = 2)
            val encodedKey = keyValue.firstOrNull().orEmpty()
            val encodedValue = keyValue.getOrNull(1).orEmpty()

            val decodedKey = URLDecoder.decode(encodedKey, Charsets.UTF_8.name())
            val decodedValue = URLDecoder.decode(encodedValue, Charsets.UTF_8.name())

            builder.add(decodedKey, decodedValue)
        }

        return builder
    }

}