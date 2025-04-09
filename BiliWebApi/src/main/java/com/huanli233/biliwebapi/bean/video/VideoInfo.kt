package com.huanli233.biliwebapi.bean.video

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.IRecommendApi
import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.content.ContentElement
import com.huanli233.biliwebapi.bean.user.UserInfo
import com.huanli233.biliwebapi.util.LowerCaseUnderScore
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoInfo(
    @SerializedName("id") val aid: Long,
    val bvid: String,
    val cid: Long,
    val tid: Int,
    val duration: Long,
    val goto: String?,
    val videos: Int,
    val copyright: Int = 1,
    @SerializedName("pic", alternate = ["cover"]) val pic: String,
    val title: String,
    @SerializedName("pubdate") val pubDate: Long,
    val ctime: Long,
    val desc: String,
    @SerializedName("desc_v2") val descV2: List<ContentElement>,
    val state: Int,
    @SerializedName("redirect_url") val redirectUrl: String? = null,
    val rights: Rights,
    val owner: UserInfo,
    val stat: Stat,
    val dynamic: String,
    val pages: List<Page>,
    val subtitle: SubtitleInfo,
    val staff: List<UserInfo>,
    @SerializedName("is_upower_exclusive") val isUpowerExclusive: Boolean,
    @SerializedName("argue_info") val argueInfo: ArgueInfo,
    @SerializedName("is_view_self") val isViewSelf: Boolean,
    @SerializedName("is_season_display") val isSeasonDisplay: Boolean,
    @SerializedName("ugc_season") val ugcSeason: UgcSeason? = null,
    val forward: Long,
    @SerializedName("season_id") val seasonId: Int? = null,
    @SerializedName("section_id") val sectionId: Int? = null,
    @LowerCaseUnderScore val durationText: String? = null
) : Parcelable {
    val coinLimit: Int
        get() = if (copyright == 1) 2 else 1
    companion object {
        suspend fun related(api: BiliWebApi, aid: Long, bvid: String = ""): ApiResponse<List<VideoInfo>> {
            return api.getApi(IRecommendApi::class.java).getRelated(aid, bvid)
        }
    }
}

@Parcelize
data class ArgueInfo(
    @SerializedName("argue_msg") val argueMsg: String,
    @SerializedName("argue_link") val argueLink: String,
    @SerializedName("argue_type") val argueType: Int,
) : Parcelable