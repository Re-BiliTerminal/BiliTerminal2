package com.huanli233.biliwebapi.bean.login

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.ILoginApi
import com.huanli233.biliwebapi.bean.ApiResponse
import retrofit2.Call

class Password {
    data class KeyAndHash(
        @Expose val hash: String,
        @Expose val key: String
    )

    data class LoginResult(
        @Expose val message: String,
        @Expose val url: String,
        @SerializedName("refresh_token") @Expose val refreshToken: String,
        @Expose val timestamp: Long
    )

    companion object {
        suspend fun getKeyAndHash(api: BiliWebApi): ApiResponse<KeyAndHash> {
            return api.getApi(ILoginApi::class.java).getKeyAndHash()
        }

        suspend fun login(
            api: BiliWebApi,
            username: String,
            encryptedPassword: String,
            captcha: Captcha,
            captchaResult: CaptchaResult
        ): ApiResponse<LoginResult> {
            return api.getApi(ILoginApi::class.java).passwordLogin(
                username,
                encryptedPassword,
                0,
                captcha.token,
                captcha.geetest.challenge,
                captchaResult.validate,
                captchaResult.seccode,
                "main_web",
                "https://www.bilibili.com"
            )
        }
    }
}
