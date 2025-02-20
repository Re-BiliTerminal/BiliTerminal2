package com.huanli233.biliwebapi.bean.video

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SubtitleInfo(
    @Expose
    @SerializedName("allow_submit")
    val allowSubmit: Boolean,

    @Expose
    val list: List<SubtitleItem>
)

data class SubtitleItem(
    @Expose
    val id: Double,

    @Expose
    val lan: String,

    @Expose
    @SerializedName("lan_doc")
    val lanDoc: String,

    @Expose
    @SerializedName("is_lock")
    val isLock: Boolean,

    @Expose
    @SerializedName("subtitle_url")
    val subtitleUrl: String,

    @Expose
    val type: Int,

    @Expose
    @SerializedName("id_str")
    val idStr: String,

    @Expose
    @SerializedName("ai_type")
    val aiType: Int,

    @Expose
    @SerializedName("ai_status")
    val aiStatus: Int,

    @Expose
    val author: Author
)

data class Author(
    @Expose
    val mid: Long,

    @Expose
    val name: String,

    @Expose
    val sex: String,

    @Expose
    val face: String,

    @Expose
    val sign: String,

    @Expose
    val rank: Int,

    @Expose
    val birthday: Long,

    @Expose
    @SerializedName("is_fake_account")
    val isFakeAccount: Int,

    @Expose
    @SerializedName("is_deleted")
    val isDeleted: Int,

    @Expose
    @SerializedName("in_reg_audit")
    val inRegAudit: Int,

    @Expose
    @SerializedName("is_senior_member")
    val isSeniorMember: Int,

    @Expose
    @SerializedName("name_render")
    val nameRender: String?
)
