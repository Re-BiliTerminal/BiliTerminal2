package com.huanli233.bilizepam.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import coil3.compose.AsyncImage
import com.huanli233.biliwebapi.bean.content.EmoteContent
import com.huanli233.bilizepam.utils.LinkTextUtil

@Composable
fun RichText(
    text: String,
    modifier: Modifier = Modifier,
    emotes: Map<String, EmoteContent>? = null,
    atList: List<Pair<String, Long>>? = null,
    style: TextStyle = LocalTextStyle.current,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    onUrlClick: (String) -> Unit = {},
    onBvidClick: (String) -> Unit = {},
    onAvidClick: (Long) -> Unit = {},
    onCvidClick: (Long) -> Unit = {},
    onUserClick: (Long) -> Unit = {}
) {
    val (annotatedString, inlineContent) = remember(text, emotes, atList, linkColor, onUserClick, onBvidClick, onAvidClick, onCvidClick, onUrlClick) {
        processRichText(text, emotes, atList, linkColor, onUserClick, onBvidClick, onAvidClick, onCvidClick, onUrlClick)
    }
    
    if (inlineContent.isEmpty()) {
        Text(
            text = annotatedString,
            modifier = modifier,
            style = style
        )
    } else {
        val mergedStyle = style.merge(
            TextStyle(color = MaterialTheme.colorScheme.onSurface)
        )
        BasicText(
            text = annotatedString,
            modifier = modifier,
            style = mergedStyle,
            inlineContent = inlineContent
        )
    }
}

private fun processRichText(
    text: String,
    emotes: Map<String, EmoteContent>?,
    atList: List<Pair<String, Long>>?,
    linkColor: Color,
    onUserClick: (Long) -> Unit,
    onBvidClick: (String) -> Unit,
    onAvidClick: (Long) -> Unit,
    onCvidClick: (Long) -> Unit,
    onUrlClick: (String) -> Unit
): Pair<AnnotatedString, Map<String, InlineTextContent>> {
    data class TextElement(
        val start: Int,
        val end: Int,
        val type: String,
        val value: String,
        val emoteContent: EmoteContent? = null
    )
    
    val elements = mutableListOf<TextElement>()
    
    LinkTextUtil.URL_PATTERN.findAll(text).forEach { match ->
        elements.add(TextElement(
            start = match.range.first,
            end = match.range.last + 1,
            type = LinkTextUtil.TAG_URL,
            value = match.value
        ))
    }
    
    LinkTextUtil.BV_PATTERN.findAll(text).forEach { match ->
        elements.add(TextElement(
            start = match.range.first,
            end = match.range.last + 1,
            type = LinkTextUtil.TAG_BVID,
            value = match.value
        ))
    }
    
    LinkTextUtil.AV_PATTERN.findAll(text).forEach { match ->
        elements.add(TextElement(
            start = match.range.first,
            end = match.range.last + 1,
            type = LinkTextUtil.TAG_AVID,
            value = match.value.removePrefix("av")
        ))
    }
    
    LinkTextUtil.CV_PATTERN.findAll(text).forEach { match ->
        elements.add(TextElement(
            start = match.range.first,
            end = match.range.last + 1,
            type = LinkTextUtil.TAG_CVID,
            value = match.value.removePrefix("cv")
        ))
    }
    
    atList?.forEach { (name, mid) ->
        val atText = "@$name"
        var index = text.indexOf(atText)
        while (index != -1) {
            elements.add(TextElement(
                start = index,
                end = index + atText.length,
                type = LinkTextUtil.TAG_USER,
                value = mid.toString()
            ))
            index = text.indexOf(atText, index + 1)
        }
    }
    
    emotes?.forEach { (emoteName, emoteContent) ->
        var index = text.indexOf(emoteName)
        while (index != -1) {
            elements.add(TextElement(
                start = index,
                end = index + emoteName.length,
                type = "emote",
                value = "emote_${emoteName.hashCode()}",
                emoteContent = emoteContent
            ))
            index = text.indexOf(emoteName, index + 1)
        }
    }
    
    elements.sortBy { it.start }
    
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        
        elements.forEach { element ->
            if (element.start >= lastIndex) {
                if (element.start > lastIndex) {
                    append(text.substring(lastIndex, element.start))
                }
                
                when (element.type) {
                    "emote" -> {
                        appendInlineContent(element.value, text.substring(element.start, element.end))
                    }
                    LinkTextUtil.TAG_URL -> {
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = element.type,
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = linkColor,
                                        textDecoration = TextDecoration.None
                                    )
                                ),
                                linkInteractionListener = { onUrlClick(element.value) }
                            )
                        ) {
                            append(text.substring(element.start, element.end))
                        }
                    }
                    LinkTextUtil.TAG_BVID -> {
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = element.type,
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = linkColor,
                                        textDecoration = TextDecoration.None
                                    )
                                ),
                                linkInteractionListener = { onBvidClick(element.value) }
                            )
                        ) {
                            append(text.substring(element.start, element.end))
                        }
                    }
                    LinkTextUtil.TAG_AVID -> {
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = element.type,
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = linkColor,
                                        textDecoration = TextDecoration.None
                                    )
                                ),
                                linkInteractionListener = { onAvidClick(element.value.toLongOrNull() ?: 0) }
                            )
                        ) {
                            append(text.substring(element.start, element.end))
                        }
                    }
                    LinkTextUtil.TAG_CVID -> {
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = element.type,
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = linkColor,
                                        textDecoration = TextDecoration.None
                                    )
                                ),
                                linkInteractionListener = { onCvidClick(element.value.toLongOrNull() ?: 0) }
                            )
                        ) {
                            append(text.substring(element.start, element.end))
                        }
                    }
                    LinkTextUtil.TAG_USER -> {
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = element.type,
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = linkColor,
                                        textDecoration = TextDecoration.None
                                    )
                                ),
                                linkInteractionListener = { onUserClick(element.value.toLongOrNull() ?: 0) }
                            )
                        ) {
                            append(text.substring(element.start, element.end))
                        }
                    }
                }
                
                lastIndex = element.end
            }
        }
        
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
    
    val inlineContent = mutableMapOf<String, InlineTextContent>()
    emotes?.forEach { (emoteName, emoteContent) ->
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
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    
    return Pair(annotatedString, inlineContent)
}
