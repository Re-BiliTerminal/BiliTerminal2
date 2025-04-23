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
    @LowerCaseUnderScore val moduleTitle: OpusTitleModule,
    @LowerCaseUnderScore val moduleContent: OpusContentModule,
    @LowerCaseUnderScore val moduleStat: OpusStatModule,
) : Parcelable

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
    val coin: OpusStat,
    val favourite: OpusStat,
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
        val align: Int,
        @SerializedName("para_type") val type: Int,
        val text: Text? = null,
        val list: ListContent? = null,
        val pic: Picture? = null,
        val line: Line? = null,
        @LowerCaseUnderScore val linkCard: LinkCard? = null
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
        val nodes: List<Node>
    ) : Parcelable {
        @Parcelize
        data class Node(
            val type: String,
            val word: Word, // Styles maybe
            val words: String
        ) : Parcelable

        @Parcelize
        data class Word(
            val color: String? = null,
            @LowerCaseUnderScore val fontSize: Int,
            val style: Style
        ) : Parcelable

        @Parcelize
        data class Style(
            val bold: Boolean = false,
            val strikethrough: Boolean = false,
            val italic: Boolean = false,
        ) : Parcelable
    }

    @Parcelize
    data class ListContent(
        @SerializedName("style") val type: Int,
        val items: List<Item>
    ) : Parcelable {
        @Parcelize
        data class Item(
            val level: Int,
            val order: Int,
            val nodes: List<Text.Node>
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
        data class GoodCard(
            @LowerCaseUnderScore val headIcon: String,
            @LowerCaseUnderScore val headText: String,
            @LowerCaseUnderScore val jumpUrl: String,
            /* val item: GoodItem, */
        ) : Parcelable
    }
}

@Parcelize
data class DynamicOpus(
    @LowerCaseUnderScore val jumpUrl: String,
    val pics: List<OpusPicture>,
    val summary: Summary,
    val title: String
) : Parcelable {
    @Parcelize
    data class Summary(
        @LowerCaseUnderScore val richTextNodes: List<RichTextNode>,
        val text: String
    ) : Parcelable
}

@Parcelize
data class OpusPicture(
    val height: Int,
    val width: Int,
    val url: String,
    val size: Float,
) : Parcelable