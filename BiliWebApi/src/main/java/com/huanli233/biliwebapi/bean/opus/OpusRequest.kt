package com.huanli233.biliwebapi.bean.opus

import com.google.gson.annotations.SerializedName

data class OpusLikeRequest(
    @SerializedName("dyn_id_str")
    val dynIdStr: String,
    @SerializedName("up")
    val up: Int,
    @SerializedName("spmid")
    val spmid: String = "333.1369.0.0",
    @SerializedName("from_spmid")
    val fromSpmid: String = "333.1387.0.0"
)

data class OpusFavoriteRequest(
    @SerializedName("meta")
    val meta: FavoriteMeta,
    @SerializedName("entity")
    val entity: FavoriteEntity,
    @SerializedName("action")
    val action: Int
)

data class FavoriteMeta(
    @SerializedName("spmid")
    val spmid: String = "333.1369.0.0",
    @SerializedName("from_spmid")
    val fromSpmid: String = "333.1387.0.0",
    @SerializedName("from")
    val from: String = "unknown"
)

data class FavoriteEntity(
    @SerializedName("object_id_str")
    val objectIdStr: String,
    @SerializedName("type")
    val type: FavoriteType
)

data class FavoriteType(
    @SerializedName("biz")
    val biz: Int = 2
)
