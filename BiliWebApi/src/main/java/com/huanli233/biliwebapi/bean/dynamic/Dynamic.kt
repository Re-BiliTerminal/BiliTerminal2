package com.huanli233.biliwebapi.bean.dynamic

import android.os.Parcelable
import com.google.gson.annotations.JsonAdapter
import com.huanli233.biliwebapi.bean.content.EmoteContent
import com.huanli233.biliwebapi.bean.content.RichTextNode
import com.huanli233.biliwebapi.bean.opus.DynamicOpus
import com.huanli233.biliwebapi.bean.opus.OpusBasicInfo
import com.huanli233.biliwebapi.bean.opus.OpusModulesDeserializer
import com.huanli233.biliwebapi.bean.opus.OpusStatModule
import com.huanli233.biliwebapi.bean.topic.TopicId
import com.huanli233.biliwebapi.bean.user.UserInfo
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.biliwebapi.util.LowerCaseUnderScore
import kotlinx.parcelize.Parcelize

@Parcelize
data class Dynamic(
    val basic: OpusBasicInfo,
    @LowerCaseUnderScore val dynamicId: String,
    val type: String,
    val isVisible: Boolean,
    val modules: DynamicModules,
    val origin: Dynamic? = null,
) : Parcelable

@Parcelize
@JsonAdapter(DynamicModulesDeserializer::class)
data class DynamicModules(
    @LowerCaseUnderScore val authorModule: UserInfo,
    @LowerCaseUnderScore val contentModule: DynamicModule,
    @LowerCaseUnderScore val statsModule: OpusStatModule,
) : Parcelable

@Parcelize
data class DynamicModule(
    val desc: Desc? = null,
    val major: DynamicMajor? = null,
    val topic: TopicId? = null,
) : Parcelable {
    @Parcelize
    data class Desc(
        @LowerCaseUnderScore val richTextNodes: List<RichTextNode>,
        val text: String
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
@JsonAdapter(DynamicMajorAdapter::class)
data class DynamicMajor(
    val content: String,
    val type: String,
    val archive: VideoInfo? = null,
    @LowerCaseUnderScore val ugcSeason: VideoInfo? = null,
    val pgc: VideoInfo? = null,
    val opus: DynamicOpus? = null
) : Parcelable
