package com.huanli233.biliwebapi.bean.login

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.ILoginApi
import com.huanli233.biliwebapi.bean.ApiData
import com.huanli233.biliwebapi.bean.ApiResponse
import retrofit2.Call

class Sms {
    data class Token(
        @SerializedName("captcha_key") @Expose val captchaKey: String
    ): ApiData() {
        suspend fun login(country: Country, phoneNum: Long, code: Int): ApiResponse<LoginResult> {
            return api.getApi(ILoginApi::class.java).smsLogin(
                country.id, phoneNum, code, "main_web", captchaKey, "https://bilibili.com"
            )
        }
    }

    data class LoginResult(
        @SerializedName("is_new") @Expose val isNew: Boolean,
        @Expose val status: Int,
        @Expose val url: String
    )

    companion object {
        suspend fun sendCode(
            api: BiliWebApi,
            country: Country,
            phoneNum: Long,
            captcha: Captcha,
            captchaResult: CaptchaResult
        ): ApiResponse<Token> {
            return api.getApi(ILoginApi::class.java).sendSms(
                country.id,
                phoneNum,
                "main_web",
                captcha.token,
                captcha.geetest.challenge,
                captchaResult.validate,
                captchaResult.seccode
            )
        }
    }
}
