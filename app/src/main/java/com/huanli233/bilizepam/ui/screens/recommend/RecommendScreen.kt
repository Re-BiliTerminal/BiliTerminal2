package com.huanli233.bilizepam.ui.screens.recommend

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import com.huanli233.bilizepam.ui.widget.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.PaddingDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.verticalContentPadding
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.biliwebapi.bean.video.VideoInfo
import kotlinx.coroutines.launch
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendScreen(
    viewModel: RecommendViewModel = hiltViewModel(),
    onVideoClick: (VideoInfo) -> Unit = {},
    onMenuClick: () -> Unit = {},
    onPopularClick: () -> Unit = {},
    onPreciousClick: () -> Unit = {}
) {
    val videos = viewModel.videos.collectAsLazyPagingItems()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    var isRefreshing by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    // Create ScrollBehavior for TopBar
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()
    
    LaunchedEffect(videos.loadState.refresh) {
        if (videos.loadState.refresh is LoadState.NotLoading && isRefreshing) {
            isRefreshing = false
        }
    }

    ScreenScaffold(
        scrollState = scrollState,
        topBar = scrollAwareTopBar(
            title = stringResource(R.string.recommend),
            showBackIcon = false,
            showMenuIcon = true,
            onMenuClick = onMenuClick,
            scrollBehavior = scrollBehavior
        ),
        topBarScrollBehavior = scrollBehavior // Pass the same ScrollBehavior to ScreenScaffold
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
                        QuickAccessButtons(
                            onPopularClick = onPopularClick,
                            onPreciousClick = onPreciousClick
                        )
                    }

                item {
                    Crossfade(
                        targetState = if (isRefreshing) LoadState.Loading else videos.loadState.refresh,
                        animationSpec = tween(durationMillis = 300),
                        label = "ContentStateTransition",
                        modifier = Modifier
                            .fillMaxWidth()
                    ) { state ->
                        when (state) {
                            is LoadState.Loading -> {
                                if (videos.itemCount == 0 || isRefreshing) {
                                    LoadingView(
                                        state = LoadingState.LOADING,
                                        modifier = Modifier.fillMaxSize()
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
}

@Composable
private fun QuickAccessButtons(
    onPopularClick: () -> Unit,
    onPreciousClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Card(
            onClick = onPopularClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔥 热门",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Card(
            onClick = onPreciousClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⭐ 必刷",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
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

