package com.huanli233.biliwebapi.bean.login

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.ILoginApi
import com.huanli233.biliwebapi.bean.ApiData
import com.huanli233.biliwebapi.bean.ApiResponse
import kotlinx.parcelize.Parcelize

@Parcelize
class Sms : Parcelable {
    data class Token(
        @SerializedName("captcha_key") val captchaKey: String
    ): ApiData() {
        suspend fun login(country: Country, phoneNum: Long, code: Int): ApiResponse<LoginResult> {
            return api.getApi(ILoginApi::class.java).smsLogin(
                country.id, phoneNum, code, "main_web", captchaKey, "https://bilibili.com"
            )
        }
    }

    data class LoginResult(
        @SerializedName("is_new") val isNew: Boolean,
        val status: Int,
        val url: String
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

@Parcelize
data class Country(
    val id: Int,
    val cname: String,
    @SerializedName("country_id") val countryId: String
) : Parcelable

@Parcelize
data class CountryList(
    val common: List<Country>,
    val others: List<Country>
) : Parcelable {
    companion object {
        suspend fun getList(api: BiliWebApi): ApiResponse<CountryList> {
            return api.getApi(ILoginApi::class.java).getCountryList()
        }
    }
}
