package com.huanli233.biliwebapi.bean.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Nameplate(
    val nid: Int,
    val name: String,
    val image: String,
    @SerializedName("image_small") val imageSmall: String,
    val level: String,
    val condition: String
) : Parcelable