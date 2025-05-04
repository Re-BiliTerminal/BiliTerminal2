@file:Suppress("DEPRECATION")

package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.article.ArticleInfo
import retrofit2.http.GET
import retrofit2.http.Query

interface IArticleApi {

    @GET("/x/article/view")
    @Deprecated("Move to opus api")
    suspend fun getArticle(@Query("cvid") cvid: Long): ApiResponse<ArticleInfo>

}