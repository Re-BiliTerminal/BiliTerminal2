package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.httplib.Domains
import com.huanli233.biliwebapi.httplib.annotation.API
import retrofit2.http.GET

interface IMainPage {

    @API(Domains.MAIN_URL)
    @GET("/")
    suspend fun getMainPage()

}