package com.huanli233.bilizepam.utils

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

object LinkTextUtil {
    
    const val TAG_URL = "url"
    const val TAG_BVID = "bvid"
    const val TAG_AVID = "avid"
    const val TAG_CVID = "cvid"
    const val TAG_USER = "user"
    
    val URL_PATTERN = Regex("(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]")
    val BV_PATTERN = Regex("BV[A-Za-z0-9]{10}")
    val AV_PATTERN = Regex("av\\d{1,10}")
    val CV_PATTERN = Regex("cv\\d{1,10}")
    
    data class LinkInfo(
        val text: String,
        val start: Int,
        val end: Int,
        val tag: String,
        val value: String
    )
    
    fun processText(
        text: String,
        atList: List<Pair<String, Long>>? = null,
        linkColor: Color = Color(0xFF66CCFF)
    ): AnnotatedString {
        val links = mutableListOf<LinkInfo>()
        
        URL_PATTERN.findAll(text).forEach { match ->
            links.add(LinkInfo(
                text = match.value,
                start = match.range.first,
                end = match.range.last + 1,
                tag = TAG_URL,
                value = match.value
            ))
        }
        
        BV_PATTERN.findAll(text).forEach { match ->
            links.add(LinkInfo(
                text = match.value,
                start = match.range.first,
                end = match.range.last + 1,
                tag = TAG_BVID,
                value = match.value
            ))
        }
        
        AV_PATTERN.findAll(text).forEach { match ->
            links.add(LinkInfo(
                text = match.value,
                start = match.range.first,
                end = match.range.last + 1,
                tag = TAG_AVID,
                value = match.value.removePrefix("av")
            ))
        }
        
        CV_PATTERN.findAll(text).forEach { match ->
            links.add(LinkInfo(
                text = match.value,
                start = match.range.first,
                end = match.range.last + 1,
                tag = TAG_CVID,
                value = match.value.removePrefix("cv")
            ))
        }
        
        atList?.forEach { (name, mid) ->
            val atText = "@$name"
            var index = text.indexOf(atText)
            while (index != -1) {
                links.add(LinkInfo(
                    text = atText,
                    start = index,
                    end = index + atText.length,
                    tag = TAG_USER,
                    value = mid.toString()
                ))
                index = text.indexOf(atText, index + 1)
            }
        }
        
        links.sortBy { it.start }
        
        return buildAnnotatedString {
            var lastIndex = 0
            
            links.forEach { link ->
                if (link.start >= lastIndex) {
                    if (link.start > lastIndex) {
                        append(text.substring(lastIndex, link.start))
                    }
                    
                    pushStringAnnotation(
                        tag = link.tag,
                        annotation = link.value
                    )
                    
                    withStyle(
                        SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.None
                        )
                    ) {
                        append(link.text)
                    }
                    
                    pop()
                    
                    lastIndex = link.end
                }
            }
            
            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }
}

@Composable
fun LinkText(
    text: String,
    modifier: Modifier = Modifier,
    atList: List<Pair<String, Long>>? = null,
    style: TextStyle = LocalTextStyle.current,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    onUrlClick: (String) -> Unit = {},
    onBvidClick: (String) -> Unit = {},
    onAvidClick: (Long) -> Unit = {},
    onCvidClick: (Long) -> Unit = {},
    onUserClick: (Long) -> Unit = {}
) {
    val annotatedString = LinkTextUtil.processText(text, atList, linkColor)
    
    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = style,
        onClick = { offset ->
            annotatedString.getStringAnnotations(offset, offset).firstOrNull()?.let { annotation ->
                when (annotation.tag) {
                    LinkTextUtil.TAG_URL -> onUrlClick(annotation.item)
                    LinkTextUtil.TAG_BVID -> onBvidClick(annotation.item)
                    LinkTextUtil.TAG_AVID -> onAvidClick(annotation.item.toLongOrNull() ?: 0)
                    LinkTextUtil.TAG_CVID -> onCvidClick(annotation.item.toLongOrNull() ?: 0)
                    LinkTextUtil.TAG_USER -> onUserClick(annotation.item.toLongOrNull() ?: 0)
                }
            }
        }
    )
}
