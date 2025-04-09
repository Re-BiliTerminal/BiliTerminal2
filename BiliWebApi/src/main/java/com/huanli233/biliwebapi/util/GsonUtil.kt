package com.huanli233.biliwebapi.util

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.FieldNamingPolicy
import com.google.gson.FieldNamingStrategy
import com.google.gson.GsonBuilder
import com.huanli233.biliwebapi.BiliWebApi
import java.lang.reflect.Field

internal val gson = GsonBuilder()
    .setExclusionStrategies(object : ExclusionStrategy {
        override fun shouldSkipField(f: FieldAttributes?): Boolean = false

        override fun shouldSkipClass(clazz: Class<*>?): Boolean =
            clazz == BiliWebApi::class.java

    })
    .setFieldNamingStrategy(BilibiliFieldNamingStrategy)
    .setLenient()
    .create()

private object BilibiliFieldNamingStrategy : FieldNamingStrategy {
    override fun translateName(field: Field): String {
        if (field.isAnnotationPresent(LowerCaseUnderScore::class.java)) {
            return FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field)
        }
        return FieldNamingPolicy.IDENTITY.translateName(field)
    }
}