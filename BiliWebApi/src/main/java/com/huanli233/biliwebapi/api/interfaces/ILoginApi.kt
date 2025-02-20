package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.login.Captcha
import com.huanli233.biliwebapi.bean.login.CookieActivePayload
import com.huanli233.biliwebapi.bean.login.CountryList
import com.huanli233.biliwebapi.bean.login.Password
import com.huanli233.biliwebapi.bean.login.Password.KeyAndHash
import com.huanli233.biliwebapi.bean.login.QrCode
import com.huanli233.biliwebapi.bean.login.Sms
import com.huanli233.biliwebapi.httplib.Domains
import com.huanli233.biliwebapi.httplib.annotation.API
import com.huanli233.biliwebapi.httplib.annotation.Queries
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@API(Domains.PASSPORT_URL)
interface ILoginApi {
    @GET("/x/passport-login/captcha")
    @Queries(keys = ["source"], values = ["main_web"])
    suspend fun requestCaptcha(): ApiResponse<Captcha>

    @GET("/x/passport-login/web/qrcode/generate")
    suspend fun requestQrCode(): ApiResponse<QrCode>

    @GET("/x/passport-login/web/qrcode/poll")
    suspend fun qrCodeLogin(@Query("qrcode_key") qrcodeKey: String): ApiResponse<QrCode.LoginResult>

    @GET("/web/generic/country/list")
    suspend fun getCountryList(): ApiResponse<CountryList>

    @POST("/x/passport-login/web/sms/send")
    @FormUrlEncoded
    suspend fun sendSms(
        @Field("cid") cid: Int, @Field("tel") phoneNum: Long,
        @Field("source") loginSource: String, @Field("token") token: String,
        @Field("challenge") challenge: String, @Field("validate") validate: String,
        @Field("seccode") seccode: String
    ): ApiResponse<Sms.Token>

    @POST("/x/passport-login/web/login/sms")
    @FormUrlEncoded
    suspend fun smsLogin(
        @Field("cid") cid: Int, @Field("tel") phoneNum: Long,
        @Field("code") code: Int, @Field("source") loginSource: String,
        @Field("captcha_key") token: String, @Field("go_url") goUrl: String
    ): ApiResponse<Sms.LoginResult>

    @GET("/x/passport-login/web/key")
    suspend fun getKeyAndHash(): ApiResponse<KeyAndHash>

    @POST("/x/passport-login/web/login")
    @FormUrlEncoded
    suspend fun passwordLogin(
        @Field("username") username: String?,
        @Field("password") password: String?,
        @Field("keep") keep: Int,
        @Field("token") token: String?,
        @Field("challenge") challenge: String?,
        @Field("validate") validate: String?,
        @Field("seccode") seccode: String?,
        @Field("source") loginSource: String?,
        @Field("go_url") goUrl: String?
    ): ApiResponse<Password.LoginResult>

    @GET("/login/exit/v2")
    suspend fun exitLogin(): ApiResponse<Void>

    @API(Domains.BASE_API_URL)
    @POST("/x/internal/gaia-gateway/ExClimbWuzhi")
    suspend fun activeCookie(@Body payload: CookieActivePayload): ApiResponse<Void>
}