package com.huanli233.biliwebapi.bean.video

import android.os.Parcelable
import com.huanli233.biliwebapi.util.LowerCaseUnderScore
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tag(
    @LowerCaseUnderScore val tagId: Long,
    @LowerCaseUnderScore val tagName: String,
    val cover: String,
    @LowerCaseUnderScore val headCover: String,
    val content: String,
    @LowerCaseUnderScore val shortContent: String,
    val type: Int,
    val state: Int,
    val ctime: Long,
    val count: TagCount,
    @LowerCaseUnderScore val isAtten: Int,
    val liked: Int,
    val hated: Int
) : Parcelable

@Parcelize
data class TagCount(
    val view: Int,
    val use: Int,
    val atten: Int
) : Parcelable