package com.huanli233.biliwebapi.util

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.FieldNamingPolicy
import com.google.gson.FieldNamingStrategy
import com.google.gson.GsonBuilder
import com.google.gson.internal.Excluder
import com.google.gson.internal.bind.TypeAdapters
import com.hjq.gson.factory.constructor.MainConstructor
import com.hjq.gson.factory.data.BigDecimalTypeAdapter
import com.hjq.gson.factory.data.BooleanTypeAdapter
import com.hjq.gson.factory.data.DoubleTypeAdapter
import com.hjq.gson.factory.data.FloatTypeAdapter
import com.hjq.gson.factory.data.IntegerTypeAdapter
import com.hjq.gson.factory.data.JSONArrayTypeAdapter
import com.hjq.gson.factory.data.JSONObjectTypeAdapter
import com.hjq.gson.factory.data.LongTypeAdapter
import com.hjq.gson.factory.data.StringTypeAdapter
import com.hjq.gson.factory.element.CollectionTypeAdapterFactory
import com.hjq.gson.factory.element.MapTypeAdapterFactory
import com.hjq.gson.factory.element.ReflectiveTypeAdapterFactory
import com.hjq.gson.factory.other.AutoToNumberStrategy
import com.huanli233.biliwebapi.BiliWebApi
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Field
import java.math.BigDecimal

fun newGsonBuilder(): GsonBuilder {
    val gsonBuilder = GsonBuilder()
    val mainConstructor =
        MainConstructor(mapOf(), true, listOf())
    gsonBuilder.setObjectToNumberStrategy(AutoToNumberStrategy())
    gsonBuilder.registerTypeAdapterFactory(
        TypeAdapters.newFactory(
            String::class.java,
            StringTypeAdapter()
        )
    )
        .registerTypeAdapterFactory(
            TypeAdapters.newFactory(
                Boolean::class.javaPrimitiveType,
                Boolean::class.java,
                BooleanTypeAdapter()
            )
        )
        .registerTypeAdapterFactory(
            TypeAdapters.newFactory(
                Int::class.javaPrimitiveType,
                Int::class.java,
                IntegerTypeAdapter()
            )
        )
        .registerTypeAdapterFactory(
            TypeAdapters.newFactory(
                Long::class.javaPrimitiveType,
                Long::class.java,
                LongTypeAdapter()
            )
        )
        .registerTypeAdapterFactory(
            TypeAdapters.newFactory(
                Float::class.javaPrimitiveType,
                Float::class.java,
                FloatTypeAdapter()
            )
        )
        .registerTypeAdapterFactory(
            TypeAdapters.newFactory(
                Double::class.javaPrimitiveType,
                Double::class.java,
                DoubleTypeAdapter()
            )
        )
        .registerTypeAdapterFactory(
            TypeAdapters.newFactory(
                BigDecimal::class.java,
                BigDecimalTypeAdapter()
            )
        )
        .registerTypeAdapterFactory(CollectionTypeAdapterFactory(mainConstructor))
        .registerTypeAdapterFactory(
            ReflectiveTypeAdapterFactory(
                mainConstructor,
                BilibiliFieldNamingStrategy,
                Excluder.DEFAULT
            )
        )
        .registerTypeAdapterFactory(MapTypeAdapterFactory(mainConstructor, false))
        .registerTypeAdapterFactory(
            TypeAdapters.newFactory(
                JSONObject::class.java,
                JSONObjectTypeAdapter()
            )
        )
        .registerTypeAdapterFactory(
            TypeAdapters.newFactory(
                JSONArray::class.java,
                JSONArrayTypeAdapter()
            )
        )
    return gsonBuilder
}

internal val gson = newGsonBuilder()
    .setExclusionStrategies(object : ExclusionStrategy {
        override fun shouldSkipField(f: FieldAttributes?): Boolean = false

        override fun shouldSkipClass(clazz: Class<*>?): Boolean =
            clazz == BiliWebApi::class.java

    })
    .setFieldNamingStrategy(BilibiliFieldNamingStrategy)
    .create()

private object BilibiliFieldNamingStrategy : FieldNamingStrategy {
    override fun translateName(field: Field): String {
        if (field.isAnnotationPresent(LowerCaseUnderScore::class.java)) {
            return FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field)
        }
        return FieldNamingPolicy.IDENTITY.translateName(field)
    }
}