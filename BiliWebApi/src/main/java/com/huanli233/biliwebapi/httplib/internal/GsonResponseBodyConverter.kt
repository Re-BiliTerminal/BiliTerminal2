package com.huanli233.biliwebapi.httplib.internal

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonToken
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.bean.ApiData
import com.huanli233.biliwebapi.bean.ApiResponse
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.IOException

internal class GsonResponseBodyConverter<T>(
    private val apiInstance: BiliWebApi,
    private val gson: Gson,
    private val adapter: TypeAdapter<T>
) : Converter<ResponseBody, T> {
    @Throws(IOException::class)
    override fun convert(value: ResponseBody): T {
        val jsonReader = gson.newJsonReader(value.charStream())
        value.use {
            val result = adapter.read(jsonReader)
            if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw JsonIOException("JSON document was not fully consumed.")
            }
            return result.also {
                if (it is ApiResponse<*>) {
                    it.data?.let { data ->
                        if (data is ApiData) data.api = apiInstance
                    }
                }
            }
        }
    }
}