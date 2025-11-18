package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.huanli233.biliwebapi.bean.series.UserSeriesList
import com.huanli233.biliwebapi.bean.user.UserArticle
import com.huanli233.biliwebapi.bean.user.UserCardInfo
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.bilizepam.data.paging.UserArticlePagingSource
import com.huanli233.bilizepam.data.paging.UserVideoPagingSource
import com.huanli233.bilizepam.data.repository.SeriesRepository
import com.huanli233.bilizepam.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val seriesRepository: SeriesRepository
) : ViewModel() {
    private val _userInfo = MutableStateFlow<UserCardInfo?>(null)
    val userInfo: StateFlow<UserCardInfo?> = _userInfo.asStateFlow()
    
    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()
    
    private val _followLoading = MutableStateFlow(false)
    val followLoading: StateFlow<Boolean> = _followLoading.asStateFlow()

    private val _mid = MutableStateFlow(0L)
    
    private val _seriesList = MutableStateFlow<List<UserSeriesList.SeriesItem>>(emptyList())
    val seriesList: StateFlow<List<UserSeriesList.SeriesItem>> = _seriesList.asStateFlow()
    
    private val _isLoadingSeries = MutableStateFlow(false)
    val isLoadingSeries: StateFlow<Boolean> = _isLoadingSeries.asStateFlow()

    val videosFlow: Flow<PagingData<VideoInfo>> = _mid.asStateFlow().let { midFlow ->
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                UserVideoPagingSource(midFlow.value, userRepository)
            }
        ).flow.cachedIn(viewModelScope)
    }

    val articlesFlow: Flow<PagingData<UserArticle>> = _mid.asStateFlow().let { midFlow ->
        Pager(
            config = PagingConfig(pageSize = 30, enablePlaceholders = false),
            pagingSourceFactory = {
                UserArticlePagingSource(midFlow.value, userRepository)
            }
        ).flow.cachedIn(viewModelScope)
    }

    fun loadUser(mid: Long) {
        _mid.value = mid
        viewModelScope.launch {
            userRepository.getUserCard(mid).onSuccess { info ->
                _userInfo.value = info
                _isFollowing.value = info.following
            }
        }
        loadSeriesList(mid)
    }
    
    fun toggleFollow() {
        val currentMid = _mid.value
        if (currentMid == 0L || _followLoading.value) return
        
        viewModelScope.launch {
            _followLoading.value = true
            val newFollowState = !_isFollowing.value
            
            userRepository.followUser(currentMid, newFollowState).fold(
                onSuccess = {
                    _isFollowing.value = newFollowState
                    _userInfo.value = _userInfo.value?.copy(following = newFollowState)
                },
                onFailure = { error ->
                    android.util.Log.e("UserProfileViewModel", "关注操作失败", error)
                }
            )
            _followLoading.value = false
        }
    }
    
    fun logout(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            userRepository.logout().fold(
                onSuccess = {
                    onSuccess()
                },
                onFailure = { error ->
                    android.util.Log.e("UserProfileViewModel", "退出登录失败", error)
                    onError(error.message ?: "退出登录失败")
                }
            )
        }
    }
    
    private fun loadSeriesList(mid: Long) {
        viewModelScope.launch {
            _isLoadingSeries.value = true
            seriesRepository.getUserSeriesList(mid, 1, 20).fold(
                onSuccess = { userSeriesList ->
                    val allSeries = mutableListOf<UserSeriesList.SeriesItem>()
                    userSeriesList.itemsLists?.seasonsList?.let { allSeries.addAll(it) }
                    userSeriesList.itemsLists?.seriesList?.let { allSeries.addAll(it) }
                    _seriesList.value = allSeries
                    _isLoadingSeries.value = false
                },
                onFailure = {
                    _seriesList.value = emptyList()
                    _isLoadingSeries.value = false
                }
            )
        }
    }
}
