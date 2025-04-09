package com.huanli233.biliwebapi.bean.requestParam

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.IRequestParamApi
import com.huanli233.biliwebapi.bean.ApiData
import kotlinx.parcelize.Parcelize

@Parcelize
data class Buvids(
    @SerializedName("b_3")
    val buvid3: String,
    @SerializedName("b_4")
    val buvid4: String,
): ApiData(), Parcelable {
    companion object {
        suspend fun generate(api: BiliWebApi) =
            api.getApi(IRequestParamApi::class.java).requestBuvids()
    }
}