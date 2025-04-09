package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.loginInfo.CoinCount
import com.huanli233.biliwebapi.bean.loginInfo.NavInfo
import com.huanli233.biliwebapi.bean.loginInfo.NavStat
import com.huanli233.biliwebapi.httplib.Domains
import com.huanli233.biliwebapi.httplib.annotation.API
import retrofit2.http.GET

interface ILoginInfoApi {
    @GET("/x/web-interface/nav")
    suspend fun requestNavInfo(): ApiResponse<NavInfo>

    @GET("/x/web-interface/nav/stat")
    suspend fun requestNavStat(): ApiResponse<NavStat>

    @GET("/site/getCoin")
    @API(Domains.ACCOUNT_URL)
    suspend fun requestCoinCount(): ApiResponse<CoinCount>
}