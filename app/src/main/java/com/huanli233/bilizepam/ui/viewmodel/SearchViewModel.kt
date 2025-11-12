package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.bilizepam.data.repository.SearchRepository
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.biliwebapi.bean.search.SearchItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    init {
        loadSearchHistory()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            loadSuggestions(query)
        } else {
            _suggestions.value = emptyList()
        }
    }

    private fun loadSuggestions(query: String) {
        viewModelScope.launch {
            searchRepository.getSearchSuggestions(query).fold(
                onSuccess = { suggestions ->
                    _suggestions.value = suggestions.take(5)
                },
                onFailure = {
                    _suggestions.value = emptyList()
                }
            )
        }
    }

    private fun loadSearchHistory() {
        try {
            val json = LocalData.settings.searchHistory
            if (json.isNotEmpty()) {
                val type = object : TypeToken<List<String>>() {}.type
                val history = Gson().fromJson<List<String>>(json, type)
                _searchHistory.value = history ?: emptyList()
            }
        } catch (e: Exception) {
            _searchHistory.value = emptyList()
        }
    }

    private suspend fun saveSearchHistory() {
        try {
            val json = Gson().toJson(_searchHistory.value)
            LocalData.edit {
                searchHistory = json
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun addToHistory(query: String) {
        if (query.isBlank()) return
        val currentHistory = _searchHistory.value.toMutableList()
        currentHistory.remove(query)
        currentHistory.add(0, query)
        _searchHistory.value = currentHistory.take(10)
        viewModelScope.launch {
            saveSearchHistory()
        }
    }

    fun clearHistory() {
        _searchHistory.value = emptyList()
        viewModelScope.launch {
            saveSearchHistory()
        }
    }

    fun removeHistoryItem(query: String) {
        _searchHistory.value = _searchHistory.value.filter { it != query }
        viewModelScope.launch {
            saveSearchHistory()
        }
    }
}
