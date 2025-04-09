package com.huanli233.biliwebapi.bean.video

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UgcSeason(
    val id: Int,
    val title: String,
    val cover: String,
    val mid: Long,
    val intro: String,
    @SerializedName("sign_state") val signState: Int,
    val attribute: Int,
    val stat: Stat,
    val sections: List<Section>? = null,

) : Parcelable {
    @Parcelize
    data class Section(
        @SerializedName("season_id") val seasonId: Int,
        val id: Int,
        val title: String,
        val type: Int,
        val episodes: List<VideoInfo>
    ) : Parcelable
}