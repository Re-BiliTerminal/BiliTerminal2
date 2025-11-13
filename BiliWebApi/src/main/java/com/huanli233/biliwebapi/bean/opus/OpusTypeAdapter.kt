package com.huanli233.biliwebapi.bean.opus

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.huanli233.biliwebapi.bean.user.UserInfo
import java.lang.reflect.Type

class OpusModulesDeserializer : JsonDeserializer<OpusModules> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): OpusModules {
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
        
        val moduleAuthor = context.deserialize<UserInfo>(
            jsonObject.get("module_author"),
            UserInfo::class.java
        )
        
        val moduleTitle = jsonObject.getAsJsonObject("module_title")?.let {
            OpusTitleModule(text = it.get("text")?.asString ?: "")
        } ?: OpusTitleModule("")
        
        val moduleContent = context.deserialize<OpusContentModule>(
            jsonObject.get("module_content"),
            OpusContentModule::class.java
        )
        
        val moduleStat = context.deserialize<OpusStatModule>(
            jsonObject.get("module_stat"),
            OpusStatModule::class.java
        )
        
        return OpusModules(
            moduleAuthor = moduleAuthor,
            moduleTitle = moduleTitle,
            moduleContent = moduleContent,
            moduleStat = moduleStat
        )
    }
}