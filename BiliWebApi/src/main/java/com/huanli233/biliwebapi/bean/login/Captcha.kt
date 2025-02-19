package com.huanli233.biliwebapi.bean.login

import com.google.gson.annotations.Expose
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.ILoginApi
import com.huanli233.biliwebapi.bean.ApiResponse

data class Captcha(
    @Expose val type: String,
    @Expose val token: String,
    @Expose val geetest: Geetest
) {
    data class Geetest(
        @Expose val challenge: String,
        @Expose val gt: String
    )

    companion object {
        suspend fun getCaptcha(api: BiliWebApi): ApiResponse<Captcha> {
            return api.getApi(ILoginApi::class.java).requestCaptcha()
        }
    }
}
