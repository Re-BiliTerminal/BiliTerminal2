package com.huanli233.biliwebapi.bean.bangumi

import com.google.gson.annotations.SerializedName

data class BangumiInfo(
    @SerializedName("media_id") val mediaId: Long = 0,
    @SerializedName("season_id") val seasonId: Long = 0,
    @SerializedName("type") val type: Int = 0,
    @SerializedName("type_name") val typeName: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("cover") val cover: String = "",
    @SerializedName("horizontal_picture") val coverHorizontal: String = "",
    @SerializedName("areas") val areas: List<Area> = emptyList(),
    @SerializedName("rating") val rating: Rating? = null,
    @SerializedName("new_ep") val newEp: NewEpisode? = null
) {
    data class Area(
        @SerializedName("name") val name: String = ""
    )
    
    data class Rating(
        @SerializedName("score") val score: Float = 0f,
        @SerializedName("count") val count: Int = 0
    )
    
    data class NewEpisode(
        @SerializedName("index_show") val indexShow: String = ""
    )
}

data class BangumiDetail(
    @SerializedName("media") val media: BangumiInfo,
    @SerializedName("review") val review: Review? = null
) {
    data class Review(
        @SerializedName("is_coin") val isCoin: Int = 0,
        @SerializedName("is_open") val isOpen: Int = 0
    )
}

data class BangumiSections(
    @SerializedName("main_section") val mainSection: Section? = null,
    @SerializedName("section") val sections: List<Section>? = null
) {
    data class Section(
        @SerializedName("id") val id: Long = 0,
        @SerializedName("title") val title: String = "",
        @SerializedName("type") val type: Int = 0,
        @SerializedName("episodes") val episodes: List<Episode> = emptyList()
    )
    
    data class Episode(
        @SerializedName("id") val id: Long = 0,
        @SerializedName("aid") val aid: Long = 0,
        @SerializedName("cid") val cid: Long = 0,
        @SerializedName("title") val title: String = "",
        @SerializedName("long_title") val longTitle: String = "",
        @SerializedName("cover") val cover: String = "",
        @SerializedName("badge") val badge: String = ""
    )
}
