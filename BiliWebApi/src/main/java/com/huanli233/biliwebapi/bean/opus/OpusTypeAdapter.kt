package com.huanli233.biliwebapi.bean.opus

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class OpusModulesDeserializer : ModulesDeserializer<OpusModules>()

open class ModulesDeserializer<T> : JsonDeserializer<T> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): T {
        return when {
            json.isJsonObject -> {
                context.deserialize(json, typeOfT)
            }
            json.isJsonArray -> {
                val combined = JsonObject()
                json.asJsonArray.forEach { element ->
                    element.asJsonObject.entrySet().forEach { (key, value) ->
                        combined.add(key, value)
                    }
                }
                context.deserialize(combined, typeOfT)
            }
            else -> throw JsonParseException("Unexpected JSON format for OpusModules")
        }
    }
}