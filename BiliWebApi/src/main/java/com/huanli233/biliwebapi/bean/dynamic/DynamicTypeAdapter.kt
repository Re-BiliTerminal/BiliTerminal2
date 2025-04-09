package com.huanli233.biliwebapi.bean.dynamic

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.huanli233.biliwebapi.bean.opus.ModulesDeserializer
import java.lang.reflect.Type

class DynamicMajorAdapter: JsonSerializer<DynamicMajor>, JsonDeserializer<DynamicMajor> {
    override fun serialize(
        src: DynamicMajor,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return context.serialize(src, typeOfSrc).asJsonObject.apply {
            remove("content")
        }
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): DynamicMajor? {
        return context.deserialize<DynamicMajor?>(json, typeOfT).apply {
            json.asJsonObject.addProperty("content", json.asJsonObject.remove("type").toString())
        }
    }
}

class DynamicModulesDeserializer : ModulesDeserializer<DynamicModules>()