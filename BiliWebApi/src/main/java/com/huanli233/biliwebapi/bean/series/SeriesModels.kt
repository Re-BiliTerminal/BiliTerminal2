package com.huanli233.biliwebapi.bean.series

import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.bean.video.VideoInfo

data class SeriesInfo(
    @SerializedName("archives") val archives: List<VideoInfo> = emptyList(),
    @SerializedName("page") val page: PageInfo? = null,
    @SerializedName("meta") val meta: Meta? = null
) {
    data class PageInfo(
        @SerializedName("num") val num: Int = 0,
        @SerializedName("size") val size: Int = 0,
        @SerializedName("total") val total: Int = 0,
        @SerializedName("page_num") val pageNum: Int = 0,
        @SerializedName("page_size") val pageSize: Int = 0
    )
    
    data class Meta(
        @SerializedName("name") val name: String = "",
        @SerializedName("description") val description: String = "",
        @SerializedName("total") val total: Int = 0,
        @SerializedName("cover") val cover: String = ""
    )
}

data class UserSeriesList(
    @SerializedName("items_lists") val itemsLists: ItemsLists? = null
) {
    data class ItemsLists(
        @SerializedName("seasons_list") val seasonsList: List<SeriesItem>? = null,
        @SerializedName("series_list") val seriesList: List<SeriesItem>? = null
    )
    
    data class SeriesItem(
        @SerializedName("meta") val meta: SeriesMeta
    ) {
        data class SeriesMeta(
            @SerializedName("season_id") val seasonId: Long? = null,
            @SerializedName("series_id") val seriesId: Long? = null,
            @SerializedName("name") val name: String = "",
            @SerializedName("cover") val cover: String = "",
            @SerializedName("description") val description: String = "",
            @SerializedName("mid") val mid: Long = 0,
            @SerializedName("total") val total: Int = 0
        )
        
        val type: String
            get() = if (meta.seasonId != null) "season" else "series"
        
        val id: Long
            get() = meta.seasonId ?: meta.seriesId ?: 0L
    }
}
