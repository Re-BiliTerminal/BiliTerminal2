package com.huanli233.biliwebapi.bean.favorite

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FavoriteBoxListResponse(
    val list: List<FavoriteBox>?
) : Parcelable

@Parcelize
data class FavoriteBox(
    @SerializedName("fav_box") val favBox: Long,
    val name: String,
    val count: Int,
    @SerializedName("max_count") val maxCount: Int,
    val videos: List<FavoriteVideo>?
) : Parcelable
