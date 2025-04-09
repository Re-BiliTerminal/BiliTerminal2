package com.huanli233.biliwebapi.bean.reply

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.bean.content.EmoteContent
import com.huanli233.biliwebapi.bean.content.JumpUrlContent
import com.huanli233.biliwebapi.bean.content.PictureContent
import com.huanli233.biliwebapi.bean.user.UserId
import com.huanli233.biliwebapi.bean.user.UserInfo
import com.huanli233.biliwebapi.util.LowerCaseUnderScore
import kotlinx.parcelize.Parcelize

@Parcelize
data class RepliesInfo(
    val cursor: ReplyCursor,
    val hots: List<Reply>? = null,
    val notice: List<RepliesNotice>? = null,
    val replies: List<Reply>? = null,
    val top: RepliesTopInfo,
    @SerializedName("top_replies") val topReplies: List<Reply>,
    val config: RepliesConfig,
    val upper: UserId,
    val control: RepliesControl,
    val note: Int
) : Parcelable

@Parcelize
data class ChildRepliesInfo(
    val config: RepliesConfig,
    val control: RepliesControl,
    val page: RepliesPage,
    val replies: List<Reply>,
    val root: Reply,
    val upper: UserId
) : Parcelable

@Parcelize
data class RepliesPage(
    val count: Int,
    val num: Int,
    val size: Int,
    val acount: Int? = null
) : Parcelable

@Parcelize
data class ReplyCursor(
    @SerializedName("all_count") val allCount: Int,
    @SerializedName("is_begin") val isBegin: Boolean,
    val prev: Int,
    val next: Int,
    @SerializedName("is_end") val isEnd: Boolean,
    val mode: Int,
    @SerializedName("support_mode") val supportMode: List<Int>,
    val name: String,
    @SerializedName("pagination_reply") val paginationReply: PaginationReply
) : Parcelable

@Parcelize
data class RepliesConfig(
    @SerializedName("showadmin") val showAdmin: Int,
    @SerializedName("showfloor") val showFloor: Int,
    @SerializedName("showtopic") val showTopic: Int,
    @SerializedName("show_up_flag") val showUpFlag: Boolean,
    @SerializedName("read_only") val readOnly: Boolean,
    @SerializedName("show_del_log") val showDeleteLog: Boolean
) : Parcelable

@Parcelize
data class RepliesTopInfo(
    val admin: Reply?,
    val upper: Reply?,
    val vote: Reply?
) : Parcelable

@Parcelize
data class RepliesNotice(
    val content: String,
    val id: Long,
    val link: String,
    val title: String
) : Parcelable

@Parcelize
data class RepliesControl(
    @SerializedName("input_disable") val inputDisable: Boolean,
    @SerializedName("root_input_text") val rootInputText: String,
    @SerializedName("child_input_text") val childInputText: String,
    @SerializedName("bg_text") val bgText: String,
    @SerializedName("web_selection") val webSelection: String,
    @SerializedName("answer_guide_text") val answerGuideText: String,
    @SerializedName("answer_guide_icon_url") val answerGuideIconUrl: String,
    @SerializedName("answer_guide_android_url") val answerGuideAndroidUrl: String,
    @SerializedName("answer_guide_ios_url") val answerGuideIosUrl: String
) : Parcelable

@Parcelize
data class Reply(
    @SerializedName("rpid") val replyId: Long,
    val oid: Long,
    val type: Int,
    val mid: Long,
    val root: Long,
    val parent: Long,
    val dialog: Long,
    val count: Int,
    val rcount: Int,
    val floor: Int? = 0,
    val state: Int,
    val fansgrade: Int,
    @SerializedName("ctime") val createTime: Long,
    @SerializedName("rpid_str") val replyIdStr: String,
    @SerializedName("root_str") val rootStr: String,
    @SerializedName("parent_str") val parentStr: String,
    val like: Int,
    @SerializedName("action") val actionState: Int,
    val replies: List<Reply>? = emptyList(),
    val member: UserInfo,
    val invisible: Boolean,
    @SerializedName("up_action") val upAction: UpAction,
    @SerializedName("reply_control") val replyTipControl: ReplyTipControl,
    val content: ReplyContent
) : Parcelable

@Parcelize
data class ReplyContent(
    val message: String,
    val plat: Int,
    val device: String,
    @LowerCaseUnderScore val atNameToMid: Map<String, Long>,
    val members: List<UserInfo>,
    @SerializedName("jump_url") val jumpUrl: Map<String, JumpUrlContent>? = null,
    val emote: Map<String, EmoteContent>? = null,
    @SerializedName("max_line") val maxLines: Int,
    val pictures: List<PictureContent>? = emptyList()
) : Parcelable

@Parcelize
data class UpAction(
    val like: Boolean,
    val reply: Boolean
) : Parcelable

@Parcelize
data class ReplyTipControl(
    @LowerCaseUnderScore val subReplyEntryText: String,
    @LowerCaseUnderScore val subReplyTitleText: String,
    @SerializedName("time_desc") val timeDesc: String? = null,
    @LowerCaseUnderScore val isUpTop: Boolean = false,
    val location: String = "",
    @LowerCaseUnderScore val isNoteV2: Boolean = false
) : Parcelable

@Parcelize
data class PaginationStr(
    val offset: String
) : Parcelable

@Parcelize
data class PaginationReply(
    @SerializedName("next_offset") val nextOffset: String,
    @SerializedName("prev_offset") val prevOffset: String
) : Parcelable