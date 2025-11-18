package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.biliwebapi.bean.bangumi.BangumiDetail
import com.huanli233.biliwebapi.bean.bangumi.BangumiSections
import com.huanli233.bilizepam.data.repository.BangumiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BangumiViewModel @Inject constructor(
    private val repository: BangumiRepository
) : ViewModel() {
    
    private val _bangumiInfo = MutableStateFlow<BangumiDetail?>(null)
    val bangumiInfo: StateFlow<BangumiDetail?> = _bangumiInfo.asStateFlow()
    
    private val _sections = MutableStateFlow<BangumiSections?>(null)
    val sections: StateFlow<BangumiSections?> = _sections.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _selectedSection = MutableStateFlow(0)
    val selectedSection: StateFlow<Int> = _selectedSection.asStateFlow()
    
    private val _selectedEpisode = MutableStateFlow(0)
    val selectedEpisode: StateFlow<Int> = _selectedEpisode.asStateFlow()
    
    fun loadBangumi(mediaId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getBangumiInfo(mediaId).fold(
                onSuccess = { detail ->
                    _bangumiInfo.value = detail
                    loadSections(detail.media.seasonId)
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "加载失败"
                    _isLoading.value = false
                }
            )
        }
    }
    
    private fun loadSections(seasonId: Long) {
        viewModelScope.launch {
            repository.getBangumiSections(seasonId).fold(
                onSuccess = { sections ->
                    _sections.value = sections
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "加载分集失败"
                    _isLoading.value = false
                }
            )
        }
    }
    
    fun selectSection(index: Int) {
        _selectedSection.value = index
        _selectedEpisode.value = 0
    }
    
    fun selectEpisode(index: Int) {
        _selectedEpisode.value = index
    }
    
    fun getCurrentEpisode(): BangumiSections.Episode? {
        val sections = _sections.value ?: return null
        val allSections = buildList {
            sections.mainSection?.let { add(it) }
            sections.sections?.let { addAll(it) }
        }
        
        if (allSections.isEmpty()) return null
        val section = allSections.getOrNull(_selectedSection.value) ?: return null
        return section.episodes.getOrNull(_selectedEpisode.value)
    }
}
