package com.huanli233.biliwebapi.bean.dynamic

import android.os.Parcelable
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.bean.content.EmoteContent
import com.huanli233.biliwebapi.bean.content.RichTextNode
import com.huanli233.biliwebapi.bean.opus.DynamicOpus
import com.huanli233.biliwebapi.bean.opus.OpusBasicInfo
import com.huanli233.biliwebapi.bean.opus.OpusStatModule
import com.huanli233.biliwebapi.bean.topic.TopicId
import com.huanli233.biliwebapi.bean.user.UserInfo
import com.huanli233.biliwebapi.bean.video.ArgueInfo
import com.huanli233.biliwebapi.bean.video.Rights
import com.huanli233.biliwebapi.bean.video.Stat
import com.huanli233.biliwebapi.bean.video.SubtitleInfo
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.biliwebapi.util.LowerCaseUnderScore
import kotlinx.parcelize.Parcelize

@Parcelize
data class Dynamic(
    val basic: OpusBasicInfo,
    @SerializedName("id_str") val id: String,
    val type: String,
    val isVisible: Boolean,
    val modules: DynamicModules,
    @SerializedName("orig") val origin: Dynamic? = null,
) : Parcelable

@Parcelize
data class DynamicModules(
    @SerializedName("module_author") val authorModule: UserInfo,
    @SerializedName("module_dynamic") val contentModule: DynamicModule,
    @SerializedName("module_stat") val statsModule: OpusStatModule,
) : Parcelable

@Parcelize
data class DynamicModule(
    val desc: Desc? = null,
    val major: DynamicMajor? = null,
    val topic: TopicId? = null,
    val additional: Additional? = null,
) : Parcelable {
    @Parcelize
    data class Desc(
        @SerializedName("rich_text_nodes") val richTextNodes: List<RichTextNode> = emptyList(),
        val text: String = ""
    ) : Parcelable {
        val content: Content
            get() {
                val mentionTargets = mutableListOf<MentionTarget>()
                val emotes = mutableListOf<EmoteContent>()
                buildString {
                    richTextNodes.forEach { node ->
                        when (node.type) {
                            "RICH_TEXT_NODE_TYPE_EMOJI" -> {
                                append(node.text)
                                node.emoji?.let { emotes.add(it) }
                            }

                            "RICH_TEXT_NODE_TYPE_AT" -> {
                                val start = length
                                val textToAppend = node.text
                                append(textToAppend)
                                val end = length
                                mentionTargets.add(
                                    MentionTarget(
                                        node.rid ?: 0L,
                                         start,
                                         end
                                    )
                                )
                            }

                            "RICH_TEXT_NODE_TYPE_WEB" -> append(node.origText)

                            else -> append(node.text)
                        }
                    }
                }
                return Content(text, mentionTargets)
            }
    }

    @Parcelize
    data class Content(
        val text: String,
        val ats: List<MentionTarget> = emptyList(),
        val emotes: List<EmoteContent> = emptyList(),
    ) : Parcelable
}

typealias MentionTarget = Triple<Long, Int, Int>

@Parcelize
data class Additional(
    val type: String? = null
) : Parcelable

@Parcelize
@JsonAdapter(DynamicMajorAdapter::class)
data class DynamicMajor(
    val content: String,
    val type: String,
    val archive: DynamicArchive? = null,
    @LowerCaseUnderScore val ugcSeason: VideoInfo? = null,
    val pgc: VideoInfo? = null,
    val opus: DynamicOpus? = null
) : Parcelable

@Parcelize
data class DynamicArchive(
    val aid: String,
    val bvid: String,
    val cover: String,
    val title: String,
    val desc: String? = null,
    @SerializedName("duration_text") val durationText: String? = null,
    @SerializedName("jump_url") val jumpUrl: String? = null,
    @SerializedName("disable_preview") val disablePreview: Int = 0,
    val type: Int = 1,
    val badge: DynamicArchiveBadge? = null,
    val stat: DynamicArchiveStat? = null
) : Parcelable {
    fun toVideoInfo(author: UserInfo): VideoInfo {
        return VideoInfo(
            aid = aid.toLongOrNull() ?: 0L,
            bvid = bvid,
            cid = 0L,
            tid = 0,
            duration = parseDuration(durationText),
            goto = null,
            videos = 1,
            copyright = 1,
            pic = cover,
            title = title,
            pubDate = 0L,
            ctime = 0L,
            desc = desc,
            descV2 = null,
            state = 0,
            redirectUrl = jumpUrl,
            rights = Rights(
                bp = 0,
                elec = 0,
                download = 0,
                movie = 0,
                pay = 0,
                hd5 = 0,
                noReprint = 0,
                autoplay = 0,
                ugcPay = 0,
                isCooperation = 0,
                ugcPayPreview = 0,
                noBackground = 0,
                cleanMode = 0,
                isSteinGate = 0,
                is360 = 0,
                noShare = 0,
                arcPay = 0,
                freeWatch = 0
            ),
            owner = author,
            stat = stat?.toStat(aid.toLongOrNull() ?: 0L) ?: Stat(
                aid = aid.toLongOrNull() ?: 0L,
                view = 0,
                danmaku = 0,
                reply = 0,
                favorite = 0,
                coin = 0,
                share = 0,
                nowRank = 0,
                hisRank = 0,
                like = 0,
                dislike = 0,
                evaluation = "",
                vt = 0
            ),
            dynamic = "",
            pages = emptyList(),
            subtitle = SubtitleInfo(allowSubmit = false, list = emptyList()),
            staff = null,
            isUpowerExclusive = false,
            argueInfo = ArgueInfo("", "", 0),
            isViewSelf = false,
            isSeasonDisplay = false,
            ugcSeason = null,
            forward = 0L,
            seasonId = null,
            sectionId = null,
            durationText = durationText
        )
    }
    
    private fun parseDuration(durationText: String?): Long {
        if (durationText.isNullOrEmpty()) return 0L
        val parts = durationText.split(":")
        return when (parts.size) {
            3 -> parts[0].toLongOrNull()?.times(3600)?.plus(parts[1].toLongOrNull()?.times(60) ?: 0L)?.plus(parts[2].toLongOrNull() ?: 0L) ?: 0L
            2 -> parts[0].toLongOrNull()?.times(60)?.plus(parts[1].toLongOrNull() ?: 0L) ?: 0L
            else -> 0L
        }
    }
}

@Parcelize
data class DynamicArchiveBadge(
    @SerializedName("bg_color") val bgColor: String? = null,
    val color: String? = null,
    @SerializedName("icon_url") val iconUrl: String? = null,
    val text: String? = null
) : Parcelable

@Parcelize
data class DynamicArchiveStat(
    @JsonAdapter(ChineseNumberAdapter::class)
    val play: Int = 0,
    @JsonAdapter(ChineseNumberAdapter::class)
    val danmaku: Int = 0
) : Parcelable {
    fun toStat(aid: Long): Stat {
        return Stat(
            aid = aid,
            view = play,
            danmaku = danmaku,
            reply = 0,
            favorite = 0,
            coin = 0,
            share = 0,
            nowRank = 0,
            hisRank = 0,
            like = 0,
            dislike = 0,
            evaluation = "",
            vt = 0
        )
    }
}

data class DynamicFeedResponse(
    @LowerCaseUnderScore val hasMore: Boolean = false,
    val items: List<Dynamic>? = null,
    val offset: String? = null,
    @LowerCaseUnderScore val updateBaseline: String? = null,
    @LowerCaseUnderScore val updateNum: Int = 0
)
