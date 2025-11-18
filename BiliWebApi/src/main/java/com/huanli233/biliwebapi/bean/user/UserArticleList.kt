package com.huanli233.biliwebapi.bean.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserArticleListResponse(
    val articles: List<UserArticle>
) : Parcelable

@Parcelize
data class UserArticle(
    val id: Long,
    val title: String,
    val summary: String?,
    @SerializedName("banner_url") val bannerUrl: String?,
    val stats: ArticleStats,
    val author: ArticleAuthor
) : Parcelable

@Parcelize
data class ArticleStats(
    val view: Int,
    val like: Int,
    val reply: Int
) : Parcelable

@Parcelize
data class ArticleAuthor(
    val name: String,
    val face: String
) : Parcelable
