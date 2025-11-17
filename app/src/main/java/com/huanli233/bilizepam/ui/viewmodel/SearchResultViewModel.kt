package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.huanli233.bilizepam.data.paging.SearchPagingSource
import com.huanli233.bilizepam.data.repository.SearchRepository
import com.huanli233.biliwebapi.bean.search.SearchItem
import com.huanli233.biliwebapi.httplib.BilibiliApiInterceptor
import com.huanli233.bilizepam.api.bilibiliApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

sealed class SearchResultState {
    object Loading : SearchResultState()
    data class Success(val items: List<SearchItem>) : SearchResultState()
    data class Error(val message: String) : SearchResultState()
    object Empty : SearchResultState()
}

sealed class ArticleRedirectState {
    object Idle : ArticleRedirectState()
    object Loading : ArticleRedirectState()
    data class Success(val opusId: Long) : ArticleRedirectState()
    data class Error(val message: String) : ArticleRedirectState()
}

@HiltViewModel
class SearchResultViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _currentQuery = MutableStateFlow("")
    val currentQuery: StateFlow<String> = _currentQuery.asStateFlow()

    private val _currentType = MutableStateFlow("video")
    val currentType: StateFlow<String> = _currentType.asStateFlow()
    
    private val _searchResults = MutableStateFlow<Flow<PagingData<SearchItem>>?>(null)
    val searchResults: StateFlow<Flow<PagingData<SearchItem>>?> = _searchResults.asStateFlow()

    fun search(query: String, type: String = "video") {
        _currentQuery.value = query
        _currentType.value = type
        
        _searchResults.value = Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = {
                SearchPagingSource(
                    searchRepository = searchRepository,
                    keyword = query,
                    searchType = type
                )
            }
        ).flow.cachedIn(viewModelScope)
    }
    
    private val _loadingArticles = MutableStateFlow<Set<Long>>(emptySet())
    val loadingArticles: StateFlow<Set<Long>> = _loadingArticles.asStateFlow()
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(BilibiliApiInterceptor(bilibiliApi))
        .followRedirects(false)
        .build()
    
    fun convertCvidToOpusId(cvid: Long, onSuccess: (Long) -> Unit, onError: (String) -> Unit) {
        _loadingArticles.value += cvid
        
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val url = "https://www.bilibili.com/read/cv$cvid/"
                    val request = Request.Builder()
                        .url(url)
                        .build()

                    okHttpClient.newCall(request).execute().use { response ->
                        val location = response.header("Location")
                            ?: throw Exception("No Location header in redirect response")

                        val opusIdRegex = """https://www.bilibili.com/opus/(\d+)""".toRegex()
                        val matchResult = opusIdRegex.find(location)

                        if (matchResult != null) {
                            matchResult.groupValues[1].toLong()
                        } else {
                            throw Exception("Failed to extract opus ID from Location: $location")
                        }
                    }
                }

                _loadingArticles.value -= cvid
                onSuccess(result)
            } catch (e: Exception) {
                _loadingArticles.value -= cvid
                onError(e.message ?: e.toString())
            }
        }
    }
}
