package com.huanli233.bilizepam.utils

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil3.compose.AsyncImage
import com.huanli233.biliwebapi.bean.content.EmoteContent

object EmoteUtil {
    
    /**
     * 将包含表情包的文本转换为AnnotatedString，并返回对应的InlineTextContent
     */
    fun processEmoteText(
        text: String,
        emotes: Map<String, EmoteContent>?
    ): Pair<AnnotatedString, Map<String, InlineTextContent>> {
        if (emotes.isNullOrEmpty()) {
            return Pair(AnnotatedString(text), emptyMap())
        }

        val annotatedString = buildAnnotatedString {
            var currentText = text
            var currentIndex = 0
            
            // 按照表情包在文本中出现的顺序处理
            val emotePositions = mutableListOf<Triple<Int, String, EmoteContent>>()
            
            emotes.forEach { (emoteName, emoteContent) ->
                var index = currentText.indexOf(emoteName)
                while (index != -1) {
                    emotePositions.add(Triple(index, emoteName, emoteContent))
                    index = currentText.indexOf(emoteName, index + 1)
                }
            }
            
            // 按位置排序
            emotePositions.sortBy { it.first }
            
            var lastIndex = 0
            emotePositions.forEach { (position, emoteName, _) ->
                // 添加表情包前的文本
                if (position > lastIndex) {
                    append(currentText.substring(lastIndex, position))
                }
                
                // 添加表情包占位符
                appendInlineContent("emote_${emoteName.hashCode()}", emoteName)
                
                lastIndex = position + emoteName.length
            }
            
            // 添加剩余文本
            if (lastIndex < currentText.length) {
                append(currentText.substring(lastIndex))
            }
        }

        val inlineContent = mutableMapOf<String, InlineTextContent>()
        emotes.forEach { (emoteName, emoteContent) ->
            inlineContent["emote_${emoteName.hashCode()}"] = InlineTextContent(
                placeholder = Placeholder(
                    width = 1.5.em,
                    height = 1.5.em,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                AsyncImage(
                    model = emoteContent.url,
                    contentDescription = emoteContent.text,
                    modifier = androidx.compose.ui.Modifier.fillMaxSize()
                )
            }
        }

        return Pair(annotatedString, inlineContent)
    }
    
    /**
     * 简化版本，直接返回处理后的文本（不显示表情包图片）
     */
    fun getPlainTextWithEmotes(text: String, emotes: Map<String, EmoteContent>?): String {
        android.util.Log.d("EmoteUtil", "getPlainTextWithEmotes - 原始文本: ${text.take(50)}")
        android.util.Log.d("EmoteUtil", "getPlainTextWithEmotes - 表情包数量: ${emotes?.size ?: 0}")
        
        if (emotes.isNullOrEmpty()) {
            android.util.Log.d("EmoteUtil", "getPlainTextWithEmotes - 无表情包，直接返回原文")
            return text
        }
        
        var result = text
        emotes.forEach { (emoteName, emoteContent) ->
            android.util.Log.d("EmoteUtil", "getPlainTextWithEmotes - 替换表情包: $emoteName -> ${emoteContent.text}")
            val oldResult = result
            // 将表情包名称替换为表情包的文本描述
            result = result.replace(emoteName, emoteContent.text)
            if (oldResult != result) {
                android.util.Log.d("EmoteUtil", "getPlainTextWithEmotes - 替换成功: ${oldResult.take(30)} -> ${result.take(30)}")
            }
        }
        android.util.Log.d("EmoteUtil", "getPlainTextWithEmotes - 最终结果: ${result.take(50)}")
        return result
    }
    
    /**
     * 检查文本是否包含表情包
     */
    fun hasEmotes(text: String, emotes: Map<String, EmoteContent>?): Boolean {
        if (emotes.isNullOrEmpty()) return false
        return emotes.keys.any { text.contains(it) }
    }
}
