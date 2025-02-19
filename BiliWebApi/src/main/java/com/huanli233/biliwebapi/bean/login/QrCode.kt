package com.huanli233.biliwebapi.bean.login

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.ILoginApi
import com.huanli233.biliwebapi.bean.ApiData
import com.huanli233.biliwebapi.bean.ApiResponse
import retrofit2.Call

data class QrCode(
    @Expose val url: String,
    @SerializedName("qrcode_key") @Expose val qrcodeKey: String
): ApiData() {
    data class LoginResult(
        @Expose val code: Int,
        @Expose val message: String,
        @Expose val url: String,
        @SerializedName("refresh_token") @Expose val refreshToken: String,
        @Expose val timestamp: Long
    )

    companion object {
        suspend fun generate(api: BiliWebApi): ApiResponse<QrCode> {
            return api.getApi(ILoginApi::class.java).requestQrCode()
        }
    }

    suspend fun poll(): ApiResponse<LoginResult> {
        return api.getApi(ILoginApi::class.java).qrCodeLogin(qrcodeKey)
    }
}
