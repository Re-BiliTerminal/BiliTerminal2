package com.huanli233.bilizepam.ui.screens.recommend

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.verticalContentPadding
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.ui.components.WearTopBar
import com.huanli233.biliwebapi.bean.video.VideoInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendScreen(
    viewModel: RecommendViewModel = hiltViewModel(),
    onVideoClick: (VideoInfo) -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val videos = viewModel.videos.collectAsLazyPagingItems()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    var isRefreshing by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    ScreenScaffold(
        scrollState = scrollState,
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    videos.refresh()
                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = scrollState,
                contentPadding = PaddingValues(vertical = verticalContentPadding())
            ) {
            item {
                WearTopBar(
                    title = stringResource(R.string.recommend),
                    showBackIcon = false,
                    showMenuIcon = true,
                    modifier = Modifier.clickable { onMenuClick() }
                )
            }

            item {
                Crossfade(
                    targetState = videos.loadState.refresh,
                    animationSpec = tween(durationMillis = 300),
                    label = "ContentStateTransition",
                    modifier = Modifier
                        .fillMaxWidth()
                ) { state ->
                    when (state) {
                        is LoadState.Loading if videos.itemCount == 0 -> {
                            LoadingView(
                                state = LoadingState.LOADING,
                                modifier = Modifier.fillMaxSize()
                                    .height(screenHeight * 0.7f)
                            )
                        }
                        is LoadState.Error -> {
                            val error = state.error
                            LoadingView(
                                state = LoadingState.ERROR,
                                errorMessage = error.message,
                                onRetry = { videos.retry() },
                                modifier = Modifier.fillMaxSize()
                                    .height(screenHeight * 0.7f)
                            )
                        }
                        is LoadState.NotLoading if videos.itemCount == 0 -> {
                            LoadingView(
                                state = LoadingState.EMPTY,
                                modifier = Modifier.fillMaxSize()
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
