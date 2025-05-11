package com.huanli233.biliwebapi.bean.video

import android.os.Parcelable
import com.huanli233.biliwebapi.util.LowerCaseUnderScore
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoRelation(
    val attention: Boolean,
    val favorite: Boolean,
    @LowerCaseUnderScore val seasonFav: Boolean,
    val like: Boolean,
    val dislike: Boolean,
    val coin: Int
) : Parcelable