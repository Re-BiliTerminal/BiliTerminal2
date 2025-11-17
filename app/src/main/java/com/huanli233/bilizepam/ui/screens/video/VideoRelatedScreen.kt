package com.huanli233.bilizepam.ui.screens.video

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import com.huanli233.bilizepam.data.repository.RecommendRepository
import com.huanli233.bilizepam.ui.components.VideoCard
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
import com.huanli233.biliwebapi.bean.video.VideoInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoRelatedViewModel @Inject constructor(
    private val recommendRepository: RecommendRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<VideoRelatedUiState>(VideoRelatedUiState.Loading)
    val uiState: StateFlow<VideoRelatedUiState> = _uiState.asStateFlow()
    
    fun loadRelatedVideos(aid: Long, bvid: String = "") {
        viewModelScope.launch {
            _uiState.value = VideoRelatedUiState.Loading
            
            recommendRepository.getRelatedVideos(aid, bvid).fold(
                onSuccess = { videos ->
                    _uiState.value = if (videos.isEmpty()) {
                        VideoRelatedUiState.Empty
                    } else {
                        VideoRelatedUiState.Success(videos)
                    }
                },
                onFailure = { error ->
                    _uiState.value = VideoRelatedUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
    
    fun retry(aid: Long, bvid: String = "") {
        loadRelatedVideos(aid, bvid)
    }
}

sealed class VideoRelatedUiState {
    object Loading : VideoRelatedUiState()
    object Empty : VideoRelatedUiState()
    data class Success(val videos: List<VideoInfo>) : VideoRelatedUiState()
    data class Error(val message: String) : VideoRelatedUiState()
}

@Composable
fun VideoRelatedScreen(
    aid: Long,
    bvid: String = "",
    scrollState: androidx.wear.compose.foundation.lazy.ScalingLazyListState? = null,
    onVideoClick: (VideoInfo) -> Unit,
    paddingValues: PaddingValues = PaddingValues(),
    viewModel: VideoRelatedViewModel = hiltViewModel(key = "video_related_$aid")
) {
    val uiState by viewModel.uiState.collectAsState()
    val actualScrollState = scrollState ?: rememberScalingLazyListState()
    
    LaunchedEffect(aid) {
        if (uiState is VideoRelatedUiState.Loading) {
            viewModel.loadRelatedVideos(aid, bvid)
        }
    }
    
    when (val state = uiState) {
        is VideoRelatedUiState.Loading -> {
            LoadingView(
                state = LoadingState.LOADING,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        is VideoRelatedUiState.Error -> {
            LoadingView(
                state = LoadingState.ERROR,
                errorMessage = state.message,
                onRetry = { viewModel.retry(aid, bvid) },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        is VideoRelatedUiState.Empty -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无相关推荐",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        is VideoRelatedUiState.Success -> {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = actualScrollState,
                contentPadding = paddingValues
            ) {
                items(state.videos.size) { index ->
                    val video = state.videos[index]
                    VideoCard(
                        videoInfo = video,
                        onClick = { onVideoClick(video) }
                    )
                }
            }
        }
    }
}
