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
import java.lang.reflect.Field

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
                        data.javaClass.fields.forEach { injectApiInstance(data, it) }
                    }
                }
            }
        }
    }

    fun isFieldPrimitiveOrWrapper(field: Field): Boolean {
        return field.type.isPrimitive || field.type in wrapperTypes
    }

    private val wrapperTypes = setOf(
        Boolean::class.java,
        Byte::class.java,
        Char::class.java,
        Short::class.java,
        Int::class.java,
        Long::class.java,
        Float::class.java,
        Double::class.java
    )

    private fun injectApiInstance(instance: Any, field: Field) {
        if (isFieldPrimitiveOrWrapper(field)) return
        field.isAccessible = true
        val value = field.get(instance)
        if (value is ApiData) {
            value.api = apiInstance
        }
        value.javaClass.fields.forEach {
            injectApiInstance(value, it)
        }
    }
}