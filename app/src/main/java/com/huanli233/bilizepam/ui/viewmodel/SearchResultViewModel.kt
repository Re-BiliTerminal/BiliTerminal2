package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.bilizepam.data.repository.SearchRepository
import com.huanli233.biliwebapi.bean.search.SearchItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchResultState {
    object Loading : SearchResultState()
    data class Success(val items: List<SearchItem>) : SearchResultState()
    data class Error(val message: String) : SearchResultState()
    object Empty : SearchResultState()
}

@HiltViewModel
class SearchResultViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchResultState>(SearchResultState.Loading)
    val searchState: StateFlow<SearchResultState> = _searchState.asStateFlow()

    private val _currentQuery = MutableStateFlow("")
    val currentQuery: StateFlow<String> = _currentQuery.asStateFlow()

    private val _currentType = MutableStateFlow("video")
    val currentType: StateFlow<String> = _currentType.asStateFlow()

    fun search(query: String, type: String = "video") {
        _currentQuery.value = query
        _currentType.value = type
        _searchState.value = SearchResultState.Loading

        viewModelScope.launch {
            val result = when (type) {
                "video" -> searchRepository.searchVideos(query)
                "user" -> searchRepository.searchUsers(query)
                "article" -> searchRepository.searchArticles(query)
                "live" -> searchRepository.searchLive(query)
                else -> searchRepository.searchVideos(query)
            }

            result.fold(
                onSuccess = { items ->
                    _searchState.value = if (items.isEmpty()) {
                        SearchResultState.Empty
                    } else {
                        SearchResultState.Success(items)
                    }
                },
                onFailure = { error ->
                    _searchState.value = SearchResultState.Error(
                        error.message ?: "Unknown error"
                    )
                }
            )
        }
    }

    fun retry() {
        search(_currentQuery.value, _currentType.value)
    }
}
