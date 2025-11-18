package com.huanli233.bilizepam.ui.screens.precious

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.bilizepam.ui.components.VideoCard
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
import com.huanli233.bilizepam.ui.viewmodel.PreciousViewModel
import com.huanli233.bilizepam.ui.widget.PullToRefreshBox
import com.huanli233.biliwebapi.bean.video.VideoInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreciousScreen(
    viewModel: PreciousViewModel = hiltViewModel(),
    onVideoClick: (VideoInfo) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val videos = viewModel.videos.collectAsLazyPagingItems()
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    var isRefreshing by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()
    
    LaunchedEffect(videos.loadState.refresh) {
        if (videos.loadState.refresh is LoadState.NotLoading && isRefreshing) {
            isRefreshing = false
        }
    }

    ScreenScaffold(
        scrollState = scrollState,
        topBar = scrollAwareTopBar(
            title = "⭐ 入站必刷",
            showBackIcon = true,
            onBackClick = onNavigateBack,
            scrollBehavior = scrollBehavior
        ),
        topBarScrollBehavior = scrollBehavior
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    videos.refresh()
                },
                modifier = Modifier.fillMaxSize()
            ) {
                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = scrollState,
                    contentPadding = paddingValues
                ) {
                    item {
                        Crossfade(
                            targetState = if (isRefreshing) LoadState.Loading else videos.loadState.refresh,
                            animationSpec = tween(durationMillis = 300),
                            label = "ContentStateTransition",
                            modifier = Modifier.fillMaxWidth()
                        ) { state ->
                            when (state) {
                                is LoadState.Loading -> {
                                    LoadingView(
                                        state = LoadingState.LOADING,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(screenHeight * 0.6f)
                                    )
                                }
                                is LoadState.Error -> {
                                    LoadingView(
                                        state = LoadingState.ERROR,
                                        errorMessage = state.error.message ?: "Unknown error",
                                        onRetry = { videos.retry() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(screenHeight * 0.6f)
                                    )
                                }
                                else -> {}
                            }
                        }
                    }

                    items(videos.itemCount) { index ->
                        videos[index]?.let { video ->
                            VideoCard(
                                videoInfo = video,
                                onClick = { onVideoClick(video) }
                            )
                        }
                    }

                    item {
                        when (val appendState = videos.loadState.append) {
                            is LoadState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                            is LoadState.Error -> {
                                LoadingView(
                                    state = LoadingState.ERROR,
                                    errorMessage = appendState.error.message ?: "Unknown error",
                                    onRetry = { videos.retry() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}
