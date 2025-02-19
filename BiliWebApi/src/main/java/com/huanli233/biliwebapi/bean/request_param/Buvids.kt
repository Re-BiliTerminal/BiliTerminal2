package com.huanli233.biliwebapi.bean.request_param

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.IRequestParamApi
import com.huanli233.biliwebapi.bean.ApiData

data class Buvids(
    @field:Expose @field:SerializedName("b_3")
    val buvid3: String,
    @field:Expose @field:SerializedName("b_4")
    val buvid4: String,
): ApiData() {
    companion object {
        suspend fun generate(api: BiliWebApi) =
            api.getApi(IRequestParamApi::class.java).requestBuvids()
    }
}