package com.huanli233.biliwebapi.bean.dynamic

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.huanli233.biliwebapi.bean.opus.DynamicOpus
import com.huanli233.biliwebapi.bean.opus.OpusStatModule
import com.huanli233.biliwebapi.bean.user.UserInfo
import com.huanli233.biliwebapi.bean.video.Stat
import com.huanli233.biliwebapi.bean.video.VideoInfo
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
    ): DynamicMajor {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type")?.asString ?: ""
        
        return DynamicMajor(
            content = type,
            type = type,
            archive = jsonObject.get("archive")?.let { 
                context.deserialize(it, DynamicArchive::class.java) 
            },
            ugcSeason = jsonObject.get("ugc_season")?.let { 
                context.deserialize(it, VideoInfo::class.java) 
            },
            pgc = jsonObject.get("pgc")?.let { 
                context.deserialize(it, VideoInfo::class.java) 
            },
            opus = jsonObject.get("opus")?.let { 
                context.deserialize(it, DynamicOpus::class.java) 
            }
        )
    }
}

class DynamicModulesDeserializer : JsonDeserializer<DynamicModules> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): DynamicModules {
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
        
        val authorModule = context.deserialize<UserInfo>(
            jsonObject.get("module_author"),
            UserInfo::class.java
        )
        
        val contentModule = context.deserialize<DynamicModule>(
            jsonObject.get("module_dynamic"),
            DynamicModule::class.java
        )
        
        val statsModule = context.deserialize<OpusStatModule>(
            jsonObject.get("module_stat"),
            OpusStatModule::class.java
        )
        
        return DynamicModules(
            authorModule = authorModule,
            contentModule = contentModule,
            statsModule = statsModule
        )
    }
}

class ChineseNumberAdapter : TypeAdapter<Int>() {
    override fun write(out: JsonWriter, value: Int?) {
        out.value(value)
    }

    override fun read(`in`: JsonReader): Int {
        return when (`in`.peek()) {
            JsonToken.NUMBER -> `in`.nextInt()
            JsonToken.STRING -> {
                val str = `in`.nextString()
                parseChineseNumber(str)
            }
            else -> {
                `in`.skipValue()
                0
            }
        }
    }

    private fun parseChineseNumber(str: String): Int {
        return try {
            when {
                str.endsWith("万") -> {
                    val num = str.removeSuffix("万").toDoubleOrNull() ?: 0.0
                    (num * 10000).toInt()
                }
                str.endsWith("千") -> {
                    val num = str.removeSuffix("千").toDoubleOrNull() ?: 0.0
                    (num * 1000).toInt()
                }
                str.endsWith("亿") -> {
                    val num = str.removeSuffix("亿").toDoubleOrNull() ?: 0.0
                    (num * 100000000).toInt()
                }
                else -> str.toIntOrNull() ?: 0
            }
        } catch (e: Exception) {
            0
        }
    }
}