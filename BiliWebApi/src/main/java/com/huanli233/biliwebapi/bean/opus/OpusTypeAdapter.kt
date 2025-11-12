package com.huanli233.biliwebapi.bean.opus

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class OpusModulesDeserializer : ModulesDeserializer<OpusModules>()

open class ModulesDeserializer<T> : JsonDeserializer<T> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): T {
        val jsonObject = when {
            json.isJsonObject -> json.asJsonObject
            json.isJsonArray -> {
                val combined = JsonObject()
                json.asJsonArray.forEach { element ->
                    if (element.isJsonObject) {
                        element.asJsonObject.entrySet().forEach { (key, value) ->
                            combined.add(key, value)
                        }
                    }
                }
                combined
            }
            else -> throw JsonParseException("Unexpected JSON format")
        }
        
        val gson = Gson()
        return gson.fromJson(jsonObject, typeOfT)
    }
}