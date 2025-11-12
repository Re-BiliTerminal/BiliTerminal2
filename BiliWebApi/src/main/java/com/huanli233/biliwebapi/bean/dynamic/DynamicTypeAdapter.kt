package com.huanli233.biliwebapi.bean.dynamic

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.huanli233.biliwebapi.bean.opus.DynamicOpus
import com.huanli233.biliwebapi.bean.opus.ModulesDeserializer
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

class DynamicModulesDeserializer : ModulesDeserializer<DynamicModules>()

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