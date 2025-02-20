package com.huanli233.biliwebapi.bean.video

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.IRecommendApi
import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.user.User
import com.huanli233.biliwebapi.bean.user.UserInfo

data class VideoInfo(
    @Expose @SerializedName("id") val aid: Long,
    @Expose val bvid: String,
    @Expose val cid: Long,
    @Expose val tid: Int,
    @Expose val duration: Long,
    @Expose val goto: String?,
    @Expose val videos: Int,
    @Expose val copyright: Int = 1,
    @Expose val pic: String,
    @Expose val title: String,
    @Expose @SerializedName("pubdate") val pubDate: Long,
    @Expose val ctime: Long,
    @Expose val desc: String,
    @Expose @SerializedName("desc_v2") val descV2: Any,
    @Expose val state: Int,
    @Expose @SerializedName("redirect_url") val redirectUrl: String,
    @Expose val rights: Rights,
    @Expose val owner: User,
    @Expose val stat: Stat,
    @Expose val dynamic: String,
    @Expose val pages: List<Page>,
    @Expose val subtitle: SubtitleInfo,
    @Expose val staff: List<UserInfo>,
    @Expose @SerializedName("is_upower_exclusive") val isUpowerExclusive: Boolean
) {
    companion object {
        suspend fun related(api: BiliWebApi, aid: Long, bvid: String = ""): ApiResponse<List<VideoInfo>> {
            return api.getApi(IRecommendApi::class.java).getRelated(aid, bvid)
        }
    }
}
