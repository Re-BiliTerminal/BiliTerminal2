package com.huanli233.bilizepam.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.huanli233.bilizepam.api.apiResultNonNull
import com.huanli233.bilizepam.api.bilibiliApi
import com.huanli233.biliwebapi.api.interfaces.ISearchApi
import com.huanli233.biliwebapi.bean.search.SearchItem
import com.huanli233.biliwebapi.bean.search.SearchResult
import com.huanli233.biliwebapi.bean.search.SearchResultType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor() {
    
    private val gson = Gson()
    
    suspend fun searchAll(
        keyword: String,
        page: Int = 1
    ): Result<SearchResult> {
        return bilibiliApi.api(ISearchApi::class) {
            searchAll(keyword, page)
        }.apiResultNonNull()
    }
    
    suspend fun searchVideos(
        keyword: String,
        page: Int = 1
    ): Result<List<SearchItem>> {
        return bilibiliApi.api(ISearchApi::class) {
            searchByType(keyword, "video", page)
        }.apiResultNonNull().mapCatching { result ->
            parseSearchResult(result, "video")
        }
    }
    
    suspend fun searchUsers(
        keyword: String,
        page: Int = 1
    ): Result<List<SearchItem>> {
        return bilibiliApi.api(ISearchApi::class) {
            searchByType(keyword, "bili_user", page)
        }.apiResultNonNull().mapCatching { result ->
            parseSearchResult(result, "bili_user")
        }
    }
    
    suspend fun searchArticles(
        keyword: String,
        page: Int = 1
    ): Result<List<SearchItem>> {
        return bilibiliApi.api(ISearchApi::class) {
            searchByType(keyword, "article", page)
        }.apiResultNonNull().mapCatching { result ->
            parseSearchResult(result, "article")
        }
    }
    
    suspend fun searchLive(
        keyword: String,
        page: Int = 1
    ): Result<List<SearchItem>> {
        return bilibiliApi.api(ISearchApi::class) {
            searchByType(keyword, "live", page)
        }.apiResultNonNull().mapCatching { result ->
            parseSearchResult(result, "live")
        }
    }
    
    private fun parseSearchResult(searchResult: SearchResult, targetType: String): List<SearchItem> {
        val result = searchResult.result ?: return emptyList()
        
        return when (result) {
            is List<*> -> {
                val json = gson.toJson(result)
                val type = object : TypeToken<List<SearchItem>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            }
            else -> {
                val json = gson.toJson(result)
                val type = object : TypeToken<List<SearchResultType>>() {}.type
                val resultTypes: List<SearchResultType> = gson.fromJson(json, type) ?: emptyList()
                resultTypes.firstOrNull { it.resultType == targetType }?.data ?: emptyList()
            }
        }
    }
    
    suspend fun getSearchSuggestions(term: String): Result<List<String>> {
        return bilibiliApi.api(ISearchApi::class) {
            getSearchSuggestions(term)
        }.apiResultNonNull().mapCatching { suggestions ->
            suggestions.tag?.map { it.value } ?: emptyList()
        }
    }
}
