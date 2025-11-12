package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.search.SearchResult
import com.huanli233.biliwebapi.bean.search.SearchSuggestions
import com.huanli233.biliwebapi.httplib.annotation.WbiSign
import retrofit2.http.GET
import retrofit2.http.Query

interface ISearchApi {
    
    @WbiSign
    @GET("/x/web-interface/wbi/search/all/v2")
    suspend fun searchAll(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1
    ): ApiResponse<SearchResult>
    
    @WbiSign
    @GET("/x/web-interface/wbi/search/type")
    suspend fun searchByType(
        @Query("keyword") keyword: String,
        @Query("search_type") searchType: String,
        @Query("page") page: Int = 1
    ): ApiResponse<SearchResult>
    
    @GET("/x/web-interface/search/suggest")
    suspend fun getSearchSuggestions(
        @Query("term") term: String
    ): ApiResponse<SearchSuggestions>
}
