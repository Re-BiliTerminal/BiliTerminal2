package com.huanli233.biliwebapi.bean.video

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.bean.user.UserInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubtitleContent(
    @SerializedName("font_size")
    val fontSize: Double,

    @SerializedName("font_color")
    val fontColor: String,

    @SerializedName("background_alpha")
    val backgroundAlpha: Double,
    
    @SerializedName("background_color")
    val backgroundColor: String,

    @SerializedName("Stroke")
    val stroke: String,
    
    val type: String,
    
    val lang: String,
    
    val version: String,
    
    val body: List<SubtitleBody>
) : Parcelable

@Parcelize
data class SubtitleBody(
    val from: Double,
    
    val to: Double,
    
    val sid: Int,
    
    val location: Int,
    
    val content: String,
    
    val music: Double
) : Parcelable

@Parcelize
data class SubtitleInfo(
    @SerializedName("allow_submit")
    val allowSubmit: Boolean,
    
    val list: List<SubtitleInfoItem>
) : Parcelable

@Parcelize
data class SubtitleInfoItem(
    val id: Long,

    val lan: String,

    @SerializedName("lan_doc")
    val lanDoc: String,

    @SerializedName("is_lock")
    val isLock: Boolean,

    @SerializedName("subtitle_url")
    val subtitleUrl: String,
    
    val type: Int,

    @SerializedName("id_str")
    val idStr: String,

    @SerializedName("ai_type")
    val aiType: Int,

    @SerializedName("ai_status")
    val aiStatus: Int,

    val author: UserInfo
) : Parcelable

