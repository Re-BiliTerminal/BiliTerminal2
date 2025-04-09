package com.huanli233.biliwebapi.bean.topic

import android.os.Parcelable
import com.huanli233.biliwebapi.util.LowerCaseUnderScore
import kotlinx.parcelize.Parcelize

@Parcelize
data class TopicId(
    val id: Int,
    @LowerCaseUnderScore val jumpUrl: String,
    val name: String
) : Parcelable
