package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.cookie.CookieInfo
import com.huanli233.biliwebapi.httplib.Domains
import com.huanli233.biliwebapi.httplib.annotation.API
import com.huanli233.biliwebapi.httplib.annotation.Csrf
import com.huanli233.biliwebapi.httplib.annotation.Fields
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ICookieApi {

    @API(Domains.PASSPORT_URL)
    @GET("/x/passport-login/web/cookie/info")
    suspend fun cookieInfo(): ApiResponse<CookieInfo>

    @API(Domains.MAIN_URL)
    @GET("/correspond/1/{correspondPath}")
    suspend fun requestCorrespondPath(
        @Path("correspondPath") correspondPath: String
    ): String

    @API(Domains.PASSPORT_URL)
    @POST("/x/passport-login/web/cookie/refresh")
    @FormUrlEncoded @Csrf
    @Fields(keys = ["source"], values = ["main_web"])
    suspend fun refreshCookie(
        @Field("refresh_csrf") refreshCsrf: String,
        @Field("refresh_token") refreshToken: String
    )

}