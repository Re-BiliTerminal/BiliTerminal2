package com.huanli233.biliwebapi.bean.requestParam

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.IRequestParamApi
import com.huanli233.biliwebapi.bean.ApiData
import com.huanli233.biliwebapi.bean.ApiResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class BiliTicket(
    val ticket: String,
    @SerializedName("created_at")
    val createTime: Long
): ApiData(), Parcelable {
    companion object {
        suspend fun generate(api: BiliWebApi, keyId: String?, hexSign: String?, ts: String?): ApiResponse<BiliTicket> {
            return api.getApi(IRequestParamApi::class.java)
                .genWebTicket(keyId, hexSign, ts)
        }
    }
}