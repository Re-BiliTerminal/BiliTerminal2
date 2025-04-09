package com.huanli233.biliwebapi.bean.content

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.util.LowerCaseUnderScore
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmoteContent(
    val id: Long,
    @SerializedName("package_id") val packageId: Long,
    val state: Int,
    val type: Int,
    val text: String,
    val url: String,
    val meta: Meta,
    val mtime: Long,
    @SerializedName("jump_title") val jumpTitle: String
) : Parcelable {
    @Parcelize
    data class Meta(
        val size: Int,
        val alias: String? = null
    ) : Parcelable
}

@Parcelize
data class JumpUrlContent(
    val title: String,
    val state: Int,
    val prefixIcon: String,
    val appUrlSchema: String,
    val appName: String,
    val appPackageName: String,
    val clickReport: String
) : Parcelable

@Parcelize
data class PictureContent(
    @SerializedName("img_src") val src: String,
    @SerializedName("img_width") val width: Int,
    @SerializedName("img_height") val height: Int,
    @SerializedName("img_size") val size: Long
) : Parcelable

@Parcelize
data class RichTextNode(
    @LowerCaseUnderScore val origText: String,
    val text: String,
    val rid: Long? = null,
    val emoji: EmoteContent? = null,
    val type: String
) : Parcelable