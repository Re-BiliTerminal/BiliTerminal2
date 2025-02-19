package com.huanli233.biliwebapi.bean.login

import com.google.gson.annotations.Expose
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.ILoginApi
import com.huanli233.biliwebapi.bean.ApiResponse
import retrofit2.Call

data class CountryList(
    @Expose val common: List<Country>,
    @Expose val others: List<Country>
) {
    companion object {
        suspend fun getList(api: BiliWebApi): ApiResponse<CountryList> {
            return api.getApi(ILoginApi::class.java).getCountryList()
        }
    }
}
