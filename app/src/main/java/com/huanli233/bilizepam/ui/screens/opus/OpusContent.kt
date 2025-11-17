package com.huanli233.bilizepam.ui.screens.opus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.huanli233.biliwebapi.bean.opus.Opus
import com.huanli233.biliwebapi.bean.opus.OpusContentModule.*
import androidx.core.graphics.toColorInt

const val PARAGRAPH_TYPE_TEXT = 1
const val PARAGRAPH_TYPE_PICTURE = 2
const val PARAGRAPH_TYPE_LINE_DIVIDER = 3
const val PARAGRAPH_TYPE_QUOTE = 4
const val PARAGRAPH_TYPE_LIST = 5
const val PARAGRAPH_TYPE_LINK_CARD = 6
const val PARAGRAPH_TYPE_HEADING = 8

const val LIST_STYLE_ORDERED = 1
const val LIST_STYLE_UNORDERED = 2

@Composable
fun OpusContent(
    opus: Opus,
    onUserClick: (Long) -> Unit,
    onVideoClick: (String) -> Unit,
    onImageClick: (List<String>, Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opus.modules.moduleContent.paragraphs.forEach { paragraph ->
            ParagraphItem(
                paragraph = paragraph,
                onVideoClick = onVideoClick,
                onImageClick = onImageClick,
                onUserClick = onUserClick
            )
        }
    }
}

@Composable
private fun ParagraphItem(
    paragraph: Paragraph,
    onVideoClick: (String) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onUserClick: (Long) -> Unit = {}
) {
    when (paragraph.type) {
        PARAGRAPH_TYPE_TEXT -> {
            paragraph.text?.let { text ->
                RichText(
                    text = text,
                    align = paragraph.align,
                    onUserClick = onUserClick
                )
            }
        }

        PARAGRAPH_TYPE_HEADING -> {
            paragraph.heading?.let { heading ->
                HeadingText(
                    heading = heading,
                    align = paragraph.align
                )
            }
        }

        PARAGRAPH_TYPE_QUOTE -> {
            paragraph.blockquote?.let { blockquote ->
                QuoteBlock(blockquote = blockquote)
            }
        }

        PARAGRAPH_TYPE_LIST -> {
            paragraph.list?.let { list ->
                ListBlock(list = list)
            }
        }

        PARAGRAPH_TYPE_PICTURE -> {
            paragraph.pic?.let { pic ->
                PictureBlock(
                    pics = pic.pics,
                    onImageClick = onImageClick
                )
            }
        }

        PARAGRAPH_TYPE_LINE_DIVIDER -> {
            paragraph.line?.let { line ->
                LineDivider(line = line)
            }
        }

        PARAGRAPH_TYPE_LINK_CARD -> {
            paragraph.linkCard?.let { linkCard ->
                LinkCardBlock(
                    linkCard = linkCard,
                    onVideoClick = onVideoClick
                )
            }
        }
    }
}

@Composable
private fun RichText(
    text: Text,
    align: Int = 0,
    onUserClick: (Long) -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val bodyMediumFontSize = MaterialTheme.typography.bodyMedium.fontSize
    val (annotatedString, inlineContent) = remember(text) {
        val emojis = mutableMapOf<String, EmojiNode>()
        
        val annotated = buildAnnotatedString {
            text.nodes.forEachIndexed { index, node ->
                when (node.type) {
                    "TEXT_NODE_TYPE_WORD" -> {
                        node.word?.let { word ->
                            val style = SpanStyle(
                                color = parseColor(word.color),
                                fontSize = bodyMediumFontSize * (word.fontSize / 17f),
                                fontWeight = if (word.style?.bold == true) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (word.style?.italic == true) FontStyle.Italic else FontStyle.Normal,
                                textDecoration = when {
                                    word.style?.strikethrough == true -> TextDecoration.LineThrough
                                    word.style?.underline == true -> TextDecoration.Underline
                                    else -> null
                                }
                            )
                            withStyle(style) {
                                append(word.words)
                            }
                        }
                    }

                    "TEXT_NODE_TYPE_RICH" -> {
                        node.rich?.let { rich ->
                            val linkStyle = SpanStyle(
                                color = primaryColor,
                                textDecoration = TextDecoration.Underline
                            )
                            
                            when (rich.type) {
                                "RICH_TEXT_NODE_TYPE_AT" -> {
                                    rich.rid?.toLongOrNull()?.let { mid ->
                                        val link = LinkAnnotation.Clickable(
                                            tag = "user_$mid",
                                            styles = TextLinkStyles(style = linkStyle),
                                            linkInteractionListener = {
                                                onUserClick(mid)
                                            }
                                        )
                                        withLink(link) {
                                            append(rich.text)
                                        }
                                    } ?: run {
                                        withStyle(linkStyle) {
                                            append(rich.text)
                                        }
                                    }
                                }
                                else -> {
                                    withStyle(linkStyle) {
                                        append(rich.text)
                                    }
                                }
                            }
                        }
                    }
                    
                    "TEXT_NODE_TYPE_EMOJI" -> {
                        node.emoji?.let { emoji ->
                            val emojiId = "emoji_${emoji.iconUrl.hashCode()}"
                            emojis[emojiId] = emoji
                            appendInlineContent(emojiId, emoji.text)
                        }
                    }
                }
            }
        }
        
        val inline = emojis.mapValues { (id, emoji) ->
            InlineTextContent(
                placeholder = Placeholder(
                    width = when (emoji.size) {
                        1 -> 1.2.em
                        2 -> 1.5.em
                        else -> 1.5.em
                    },
                    height = when (emoji.size) {
                        1 -> 1.2.em
                        2 -> 1.5.em
                        else -> 1.5.em
                    },
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                AsyncImage(
                    model = emoji.iconUrl,
                    contentDescription = emoji.text,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        Pair(annotated, inline)
    }
    
    androidx.compose.material3.Text(
        text = annotatedString,
        inlineContent = inlineContent,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = when (align) {
            1 -> TextAlign.Center
            2 -> TextAlign.End
            else -> TextAlign.Start
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun HeadingText(
    heading: Heading,
    align: Int = 0
) {
    val annotatedString = buildAnnotatedString {
        heading.nodes.forEach { node ->
            node.word?.let { word ->
                val style = SpanStyle(
                    color = parseColor(word.color),
                    fontSize = word.fontSize.sp,
                    fontWeight = FontWeight.Bold
                )
                withStyle(style) {
                    append(word.words)
                }
            }
        }
    }

    Text(
        text = annotatedString,
        style = when (heading.level) {
            1 -> MaterialTheme.typography.headlineMedium
            2 -> MaterialTheme.typography.headlineSmall
            else -> MaterialTheme.typography.titleLarge
        },
        fontWeight = FontWeight.Bold,
        textAlign = when (align) {
            1 -> TextAlign.Center
            2 -> TextAlign.End
            else -> TextAlign.Start
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun QuoteBlock(blockquote: Blockquote) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            blockquote.children.forEach { child ->
                child.text?.let { text ->
                    RichText(text = text)
                }
            }
        }
    }
}

@Composable
private fun ListBlock(list: ListContent) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val items = list.children.ifEmpty { list.items }
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val bullet = when (list.style) {
                    LIST_STYLE_ORDERED -> "${item.order}."
                    LIST_STYLE_UNORDERED -> "•"
                    else -> "•"
                }

                Text(
                    text = bullet,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(24.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    item.children.forEach { child ->
                        child.text?.let { text ->
                            RichText(text = text)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PictureBlock(
    pics: List<com.huanli233.biliwebapi.bean.opus.OpusPicture>,
    onImageClick: (List<String>, Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        pics.forEachIndexed { index, pic ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(pic.url)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onImageClick(pics.map { it.url }, index)
                    },
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun LineDivider(line: Line) {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    )
}

@Composable
private fun LinkCardBlock(
    linkCard: LinkCard,
    onVideoClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when (linkCard.card.type) {
                    "LINK_CARD_TYPE_UGC" -> {
                        linkCard.card.ugc?.jumpUrl?.let { url ->
                            val bvid = url.substringAfter("/video/").substringBefore("/")
                            if (bvid.isNotEmpty()) {
                                onVideoClick(bvid)
                            }
                        }
                    }
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (linkCard.card.type) {
                "LINK_CARD_TYPE_UGC" -> linkCard.card.ugc?.let { ugc ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(ugc.cover)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = ugc.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2
                        )
                        Text(
                            text = ugc.descSecond,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                "LINK_CARD_TYPE_LIVE" -> linkCard.card.live?.let { live ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(live.cover)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = live.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2
                        )
                        Text(
                            text = live.descSecond,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                "LINK_CARD_TYPE_OPUS" -> linkCard.card.opus?.let { opus ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = opus.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2
                        )
                        Text(
                            text = "${opus.author.name} · ${opus.stat.view}阅读",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun parseColor(colorString: String?): Color {
    if (colorString.isNullOrEmpty() || colorString == "null") {
        return Color.Unspecified
    }

    return try {
        val hex = colorString.removePrefix("#")
        when (hex.length) {
            6 -> Color("#$hex".toColorInt())
            8 -> Color("#$hex".toColorInt())
            else -> Color.Unspecified
        }
    } catch (e: Exception) {
        Color.Unspecified
    }
}
