package com.huanli233.biliwebapi.bean.article

import android.os.Parcelable
import com.huanli233.biliwebapi.util.LowerCaseUnderScore
import kotlinx.parcelize.Parcelize

@Deprecated("Move to opus api")
@Parcelize
data class ArticleInfo(
    val id: Long,
    @LowerCaseUnderScore val dynIdStr: String
) : Parcelable
