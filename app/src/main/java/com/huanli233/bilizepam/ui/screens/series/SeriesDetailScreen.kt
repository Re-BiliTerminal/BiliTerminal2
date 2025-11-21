package com.huanli233.bilizepam.ui.screens.series

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
import com.huanli233.bilizepam.ui.screens.recommend.VideoCard
import com.huanli233.bilizepam.ui.viewmodel.SeriesViewModel
import com.huanli233.bilizepam.ui.widget.PullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailScreen(
    type: String,
    mid: Long,
    id: Long,
    name: String,
    viewModel: SeriesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onVideoClick: (VideoInfo) -> Unit = {}
) {
    val seriesName by viewModel.seriesName.collectAsState()
    val videosFlow by viewModel.videos.collectAsState()
    
    val scrollState = rememberScalingLazyListState()
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()
    var isRefreshing by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    LaunchedEffect(type, mid, id, name) {
        viewModel.loadSeries(type, mid, id, name)
    }
    
    ScreenScaffold(
        scrollState = scrollState,
        topBar = scrollAwareTopBar(
            title = seriesName.ifEmpty { name },
            showBackIcon = true,
            onBackClick = onNavigateBack,
            scrollBehavior = scrollBehavior
        ),
        topBarScrollBehavior = scrollBehavior
    ) { paddingValues ->
        videosFlow?.let { flow ->
            val videos = flow.collectAsLazyPagingItems()
            
            LaunchedEffect(videos.loadState.refresh) {
                if (videos.loadState.refresh is LoadState.NotLoading && isRefreshing) {
                    isRefreshing = false
                }
            }
            
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
                                        if (videos.itemCount == 0 || isRefreshing) {
                                            LoadingView(
                                                state = LoadingState.LOADING,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .height(screenHeight * 0.7f)
                                            )
                                        }
                                    }
                                    is LoadState.Error -> {
                                        val error = state.error
                                        LoadingView(
                                            state = LoadingState.ERROR,
                                            errorMessage = error.message,
                                            onRetry = { videos.retry() },
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .height(screenHeight * 0.7f)
                                        )
                                    }
                                    is LoadState.NotLoading if videos.itemCount == 0 -> {
                                        LoadingView(
                                            state = LoadingState.EMPTY,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .height(screenHeight * 0.7f)
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }
                        
                        items(
                            count = videos.itemCount,
                            key = { index -> videos.peek(index)?.aid?.takeIf { it != 0L } ?: index }
                        ) { index ->
                            val video = videos[index]
                            if (video != null && video.bvid.isNotEmpty()) {
                                VideoCard(
                                    videoInfo = video,
                                    onClick = { onVideoClick(video) }
                                )
                            }
                        }
                        
                        item {
                            LoadingFooter(videos)
                        }
                    }
                }
            }
        } ?: run {
            LoadingView(
                state = LoadingState.LOADING,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun LoadingFooter(videos: LazyPagingItems<VideoInfo>) {
    when (videos.loadState.append) {
        is LoadState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is LoadState.Error -> {
            val error = (videos.loadState.append as LoadState.Error).error
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "加载失败: ${error.message}",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        else -> {}
    }
}
