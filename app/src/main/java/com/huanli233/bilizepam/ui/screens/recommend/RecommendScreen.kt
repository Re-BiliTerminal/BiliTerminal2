package com.huanli233.bilizepam.ui.screens.recommend

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.huanli233.biliwebapi.bean.video.VideoInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendScreen(
    viewModel: RecommendViewModel = hiltViewModel(),
    onVideoClick: (VideoInfo) -> Unit = {}
) {
    val videos = viewModel.videos.collectAsLazyPagingItems()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing || (videos.loadState.refresh is LoadState.Loading && videos.itemCount > 0),
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    videos.refresh()
                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            // Determine current state for crossfade animation
            val currentState = when {
                videos.loadState.refresh is LoadState.Loading && videos.itemCount == 0 -> "loading"
                videos.loadState.refresh is LoadState.Error -> "error"
                videos.loadState.refresh is LoadState.NotLoading && videos.itemCount == 0 -> "empty"
                else -> "content"
            }
            
            Crossfade(
                targetState = currentState,
                animationSpec = tween(durationMillis = 300),
                label = "ContentStateTransition"
            ) { state ->
                when (state) {
                    "loading" -> {
                        LoadingView(
                            state = LoadingState.LOADING,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    "error" -> {
                        val error = (videos.loadState.refresh as LoadState.Error).error
                        LoadingView(
                            state = LoadingState.ERROR,
                            errorMessage = error.message,
                            onRetry = { videos.retry() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    "empty" -> {
                        LoadingView(
                            state = LoadingState.EMPTY,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    "content" -> {
                        VideoList(
                            videos = videos,
                            onVideoClick = onVideoClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoList(
    videos: LazyPagingItems<VideoInfo>,
    onVideoClick: (VideoInfo) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = videos.itemCount,
            key = videos.itemKey { it.aid }
        ) { index ->
            val video = videos[index]
            if (video != null && video.bvid.isNotEmpty()) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)) +
                            slideInVertically(
                                initialOffsetY = { it / 4 },
                                animationSpec = tween(durationMillis = 300)
                            )
                ) {
                    VideoCard(
                        videoInfo = video,
                        onClick = { onVideoClick(video) }
                    )
                }
            }
        }
        
        // Loading footer
        item {
            when (videos.loadState.append) {
                is LoadState.Loading -> {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 200))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is LoadState.Error -> {
                    val error = (videos.loadState.append as LoadState.Error).error
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 200))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Text(
                                text = "加载失败: ${error.message}",
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
