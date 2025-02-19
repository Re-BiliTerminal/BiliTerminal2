package com.huanli233.biliwebapi.bean.request_param

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.IRequestParamApi
import com.huanli233.biliwebapi.bean.ApiData
import com.huanli233.biliwebapi.bean.ApiResponse


data class BiliTicket(
    @field:Expose val ticket: String,
    @field:Expose @field:SerializedName("created_at")
    val createTime: Long
): ApiData() {
    companion object {
        suspend fun generate(api: BiliWebApi, keyId: String?, hexSign: String?, ts: String?): ApiResponse<BiliTicket> {
            return api.getApi(IRequestParamApi::class.java)
                .genWebTicket(keyId, hexSign, ts)
        }
    }
}