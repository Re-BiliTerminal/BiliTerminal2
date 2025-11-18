package com.huanli233.biliwebapi.bean.opus

import android.os.Parcelable
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.bean.content.RichTextNode
import com.huanli233.biliwebapi.bean.user.UserInfo
import com.huanli233.biliwebapi.util.LowerCaseUnderScore
import kotlinx.parcelize.Parcelize

const val PARAGRAPH_TYPE_WORD = 1
const val PARAGRAPH_TYPE_PICTURE = 2
const val PARAGRAPH_TYPE_LINE_DIVIDER = 3
const val PARAGRAPH_TYPE_QUOTE = 4
const val PARAGRAPH_TYPE_LIST = 5
const val PARAGRAPH_TYPE_VIDEO = 6

@Parcelize
data class Opus(
    val basic: OpusBasicInfo,
    @LowerCaseUnderScore val idStr: String,
    val type: String,
    val visible: Boolean,
    val modules: OpusModules,
) : Parcelable

@Parcelize
@JsonAdapter(OpusModulesDeserializer::class)
data class OpusModules(
    @LowerCaseUnderScore val moduleAuthor: UserInfo,
    @LowerCaseUnderScore val moduleTop: OpusTopModule? = null,
    @LowerCaseUnderScore val moduleTitle: OpusTitleModule? = null,
    @LowerCaseUnderScore val moduleContent: OpusContentModule,
    @LowerCaseUnderScore val moduleStat: OpusStatModule,
) : Parcelable

@Parcelize
data class OpusTopModule(
    val display: OpusTopDisplay
) : Parcelable {
    @Parcelize
    data class OpusTopDisplay(
        val album: OpusTopAlbum,
        val type: Int
    ) : Parcelable
    
    @Parcelize
    data class OpusTopAlbum(
        val pics: List<OpusPicture>
    ) : Parcelable
}

@Parcelize
data class OpusTitleModule(
    val text: String
) : Parcelable

@Parcelize
data class OpusBasicInfo(
    @LowerCaseUnderScore val commentIdStr: String,
    @LowerCaseUnderScore val commentType: String,
    @LowerCaseUnderScore val jumpUrl: String,
    @LowerCaseUnderScore val ridStr: String,
    val title: String? = null,
    val editable: Boolean = false
) : Parcelable

@Parcelize
data class OpusStatModule(
    val comment: OpusStat,
    val coin: OpusStat?,
    val favorite: OpusStat?,
    val forward: OpusStat,
    val like: OpusStat,
) : Parcelable {
    @Parcelize
    data class OpusStat(
        val count: Int,
        val forbidden: Boolean,
        val status: Boolean = false,
        val hidden: Boolean = false
    ) : Parcelable
}

@Parcelize
data class OpusContentModule(
    val paragraphs: List<Paragraph>
) : Parcelable {
    @Parcelize
    data class Paragraph(
        val align: Int = 0,
        @SerializedName("para_type") val type: Int,
        val format: ParagraphFormat? = null,
        val text: Text? = null,
        val heading: Heading? = null,
        val blockquote: Blockquote? = null,
        val list: ListContent? = null,
        val pic: Picture? = null,
        val line: Line? = null,
        @LowerCaseUnderScore val linkCard: LinkCard? = null
    ) : Parcelable
    
    @Parcelize
    data class ParagraphFormat(
        val align: Int = 0,
        val indent: Int? = null
    ) : Parcelable
    
    @Parcelize
    data class Heading(
        val level: Int,
        val nodes: List<TextNode>
    ) : Parcelable
    
    @Parcelize
    data class Blockquote(
        val children: List<BlockquoteChild>
    ) : Parcelable
    
    @Parcelize
    data class BlockquoteChild(
        val format: ParagraphFormat? = null,
        @SerializedName("para_type") val type: Int,
        val text: Text? = null
    ) : Parcelable

    @Parcelize
    data class Line(
        val pic: OpusPicture
    ) : Parcelable

    @Parcelize
    data class Picture(
        val pics: List<OpusPicture>
    ) : Parcelable

    @Parcelize
    data class Text(
        val nodes: List<TextNode>
    ) : Parcelable
    
    @Parcelize
    data class TextNode(
        val type: String,
        val word: WordNode? = null,
        val rich: RichNode? = null,
        val emoji: EmojiNode? = null
    ) : Parcelable
    
    @Parcelize
    data class WordNode(
        val words: String,
        val color: String? = null,
        @LowerCaseUnderScore val fontSize: Int = 17,
        @LowerCaseUnderScore val fontLevel: String? = null,
        val style: WordStyle? = null,
        @LowerCaseUnderScore val bgStyle: BgStyle? = null
    ) : Parcelable
    
    @Parcelize
    data class WordStyle(
        val bold: Boolean = false,
        val strikethrough: Boolean = false,
        val italic: Boolean = false,
        val underline: Boolean = false
    ) : Parcelable
    
    @Parcelize
    data class BgStyle(
        val color: ColorStyle? = null
    ) : Parcelable
    
    @Parcelize
    data class ColorStyle(
        val day: String? = null,
        val night: String? = null
    ) : Parcelable
    
    @Parcelize
    data class RichNode(
        val text: String,
        @LowerCaseUnderScore val origText: String,
        @LowerCaseUnderScore val jumpUrl: String? = null,
        val type: String,
        val style: WordStyle? = null,
        val rid: String? = null
    ) : Parcelable
    
    @Parcelize
    data class EmojiNode(
        val text: String,
        @LowerCaseUnderScore val iconUrl: String,
        val size: Int,
        val type: Int
    ) : Parcelable

    @Parcelize
    data class ListContent(
        @SerializedName("style") val style: Int,
        val children: List<ListItem> = emptyList(),
        val items: List<ListItem> = emptyList()
    ) : Parcelable {
        @Parcelize
        data class ListItem(
            val level: Int,
            val order: Int,
            @LowerCaseUnderScore val orderStyle: OrderStyle? = null,
            val children: List<ListItemChild>
        ) : Parcelable
        
        @Parcelize
        data class ListItemChild(
            @SerializedName("para_type") val type: Int,
            val text: Text? = null
        ) : Parcelable
        
        @Parcelize
        data class OrderStyle(
            val prefix: String? = null,
            val suffix: String? = null
        ) : Parcelable
    }

    @Parcelize
    data class LinkCard(
        val card: Card,
    ) : Parcelable
    @Parcelize
    data class Card(
        val oid: String,
        val type: String,
        val ugc: VideoCard? = null,
        val live: LiveCard? = null,
        val opus: OpusCard? = null,
        val goods: GoodCard? = null
    ) : Parcelable {
        @Parcelize
        data class VideoCard(
            val title: String,
            val cover: String,
            @LowerCaseUnderScore val descSecond: String,
            val duration: String,
            @LowerCaseUnderScore val idStr: String,
            @LowerCaseUnderScore val jumpUrl: String,
        ) : Parcelable

        @Parcelize
        data class LiveCard(
            val title: String,
            val cover: String,
            @LowerCaseUnderScore val descFirst: String,
            @LowerCaseUnderScore val descSecond: String,
            @LowerCaseUnderScore val id: Long,
            @LowerCaseUnderScore val liveState: Int,
            @LowerCaseUnderScore val jumpUrl: String,
        ) : Parcelable

        @Parcelize
        data class OpusCard(
            val title: String,
            val author: OpusAuthor,
            val stat: OpusStat,
            @LowerCaseUnderScore val jumpUrl: String
        ) : Parcelable
        
        @Parcelize
        data class OpusAuthor(
            val mid: Long,
            val name: String
        ) : Parcelable
        
        @Parcelize
        data class OpusStat(
            val view: Int
        ) : Parcelable

        @Parcelize
        data class GoodCard(
            @LowerCaseUnderScore val headIcon: String? = null,
            @LowerCaseUnderScore val headText: String? = null,
            @LowerCaseUnderScore val jumpUrl: String,
            val items: List<GoodItem>? = null
        ) : Parcelable
        
        @Parcelize
        data class GoodItem(
            val id: Long,
            val name: String,
            val brief: String,
            val cover: String,
            val price: String,
            @LowerCaseUnderScore val jumpUrl: String,
            @LowerCaseUnderScore val jumpDesc: String
        ) : Parcelable
    }
}

@Parcelize
data class DynamicOpus(
    @LowerCaseUnderScore val jumpUrl: String,
    val pics: List<OpusPicture> = emptyList(),
    val summary: Summary? = null,
    val title: String? = null,
    @SerializedName("fold_action") val foldAction: List<String>? = null
) : Parcelable {
    @Parcelize
    data class Summary(
        @SerializedName("rich_text_nodes") val richTextNodes: List<RichTextNode> = emptyList(),
        val text: String = ""
    ) : Parcelable
}

@Parcelize
data class OpusPicture(
    val height: Int,
    val width: Int,
    val url: String,
    val size: Float,
) : Parcelable