package com.huanli233.biliwebapi.bean.login

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.ILoginApi
import com.huanli233.biliwebapi.bean.ApiData
import com.huanli233.biliwebapi.bean.ApiResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class QrCode(
    val url: String,
    @SerializedName("qrcode_key") val qrcodeKey: String
): ApiData(), Parcelable {
    @Parcelize
    data class LoginResult(
        val code: Int,
        val message: String,
        val url: String,
        @SerializedName("refresh_token") val refreshToken: String,
        val timestamp: Long
    ) : Parcelable

    companion object {
        suspend fun generate(api: BiliWebApi): ApiResponse<QrCode> {
            return api.getApi(ILoginApi::class.java).requestQrCode()
        }
    }

    suspend fun poll(): ApiResponse<LoginResult> {
        return api.getApi(ILoginApi::class.java).qrCodeLogin(qrcodeKey)
    }
}
