package com.huanli233.biliwebapi.bean.login

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.ILoginApi
import com.huanli233.biliwebapi.bean.ApiResponse
import kotlinx.parcelize.Parcelize

@Parcelize
class Password : Parcelable {
    @Parcelize
    data class KeyAndHash(
        val hash: String,
        val key: String
    ) : Parcelable

    @Parcelize
    data class LoginResult(
        val message: String,
        val url: String,
        @SerializedName("refresh_token") val refreshToken: String,
        val timestamp: Long
    ) : Parcelable

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
