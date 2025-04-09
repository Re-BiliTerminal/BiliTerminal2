package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.common.SimpleAction
import com.huanli233.biliwebapi.httplib.annotation.Csrf
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ICommonApi {

    @POST("/x/community/cosmo/interface/simple_action")
    @Csrf(forceQuery = true)
    suspend fun simpleAction(@Body simpleAction: SimpleAction): ApiResponse<Unit>

}