package com.huanli233.biliwebapi.bean.search

import com.google.gson.annotations.SerializedName

data class SearchResult(
    @SerializedName("seid")
    val seid: String? = null,
    
    @SerializedName("page")
    val page: Int? = null,
    
    @SerializedName("pagesize")
    val pageSize: Int? = null,
    
    @SerializedName("numResults")
    val numResults: Int? = null,
    
    @SerializedName("numPages")
    val numPages: Int? = null,
    
    @SerializedName("result")
    val result: Any? = null
)

data class SearchResultType(
    @SerializedName("result_type")
    val resultType: String,
    
    @SerializedName("data")
    val data: List<SearchItem>? = null
)

data class SearchItem(
    @SerializedName("type")
    val type: String? = null,
    
    @SerializedName("aid")
    val aid: Long? = null,
    
    @SerializedName("bvid")
    val bvid: String? = null,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("author")
    val author: String? = null,
    
    @SerializedName("mid")
    val mid: Long? = null,
    
    @SerializedName("uname")
    val uname: String? = null,
    
    @SerializedName("upic")
    val upic: String? = null,
    
    @SerializedName("pic")
    val pic: String? = null,
    
    @SerializedName("play")
    val play: Long? = null,
    
    @SerializedName("video_review")
    val videoReview: Long? = null,
    
    @SerializedName("duration")
    val duration: String? = null,
    
    @SerializedName("pubdate")
    val pubdate: Long? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("fans")
    val fans: Long? = null,
    
    @SerializedName("level")
    val level: Int? = null,
    
    @SerializedName("room_id")
    val roomId: Long? = null,
    
    @SerializedName("cover")
    val cover: String? = null,
    
    @SerializedName("online")
    val online: Long? = null,
    
    @SerializedName("live_status")
    val liveStatus: Int? = null,
    
    @SerializedName("tag")
    val tag: String? = null
)

data class SearchSuggestionsResponse(
    val code: Int,
    val message: String? = null,
    @SerializedName("result")
    val result: SearchSuggestions? = null
)

data class SearchSuggestions(
    @SerializedName("tag")
    val tag: List<SearchSuggestionItem>? = null
)

data class SearchSuggestionItem(
    @SerializedName("value")
    val value: String,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("ref")
    val ref: Long? = null,
    
    @SerializedName("spid")
    val spid: Long? = null
)
