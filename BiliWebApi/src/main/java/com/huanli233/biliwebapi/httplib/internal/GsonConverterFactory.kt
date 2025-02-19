package com.huanli233.biliwebapi.httplib.internal

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.huanli233.biliwebapi.BiliWebApi
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * A [converter][Converter.Factory] which uses Gson for JSON.
 *
 *
 * Because Gson is so flexible in the types it supports, this converter assumes that it can
 * handle all types. If you are mixing JSON serialization with something else (such as protocol
 * buffers), you must [add this][Retrofit.Builder.addConverterFactory] last to allow the other converters a chance to see their types.
 */
internal class GsonConverterFactory private constructor(
    private val apiInstance: BiliWebApi,
    private val gson: Gson
) : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type, annotations: Array<Annotation>, retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val adapter = gson.getAdapter(TypeToken.get(type))
        return GsonResponseBodyConverter(apiInstance, gson, adapter)
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        val adapter = gson.getAdapter(TypeToken.get(type))
        return GsonRequestBodyConverter(gson, adapter)
    }

    companion object {
        /**
         * Create an instance using `gson` for conversion. Encoding to JSON and decoding from JSON
         * (when no charset is specified by a header) will use UTF-8.
         */
        /**
         * Create an instance using a default [Gson] instance for conversion. Encoding to JSON and
         * decoding from JSON (when no charset is specified by a header) will use UTF-8.
         */
        @JvmOverloads  // Guarding public API nullability.
        fun create(apiInstance: BiliWebApi, gson: Gson = Gson()): GsonConverterFactory {
            return GsonConverterFactory(apiInstance, gson)
        }
    }
}
