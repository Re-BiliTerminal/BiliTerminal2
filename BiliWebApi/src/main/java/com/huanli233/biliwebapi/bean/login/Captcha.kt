package com.huanli233.biliwebapi.bean.login

import android.os.Parcelable
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.ILoginApi
import com.huanli233.biliwebapi.bean.ApiResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class Captcha(
    val type: String,
    val token: String,
    val geetest: Geetest
) : Parcelable {
    @Parcelize
    data class Geetest(
        val challenge: String,
        val gt: String
    ) : Parcelable

    companion object {
        suspend fun getCaptcha(api: BiliWebApi): ApiResponse<Captcha> {
            return api.getApi(ILoginApi::class.java).requestCaptcha()
        }
    }
}

@Parcelize
data class CaptchaResult(
    val validate: String,
    val seccode: String
) : Parcelable
