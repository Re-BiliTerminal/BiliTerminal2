package com.huanli233.bilizepam.ui.screens.history

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.biliwebapi.bean.history.HistoryItem
import com.huanli233.biliwebapi.bean.user.UserInfo
import com.huanli233.biliwebapi.bean.video.Page
import com.huanli233.biliwebapi.bean.video.Stat
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.VideoCard
import com.huanli233.bilizepam.ui.components.ScrollAwareTopBar
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
import com.huanli233.bilizepam.ui.viewmodel.HistoryViewModel
import com.huanli233.bilizepam.utils.ObjectBuilder
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    onVideoClick: (VideoInfo) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val historyItems = viewModel.historyFlow.collectAsLazyPagingItems()
    val scrollState = rememberScalingLazyListState()
    val scope = rememberCoroutineScope()
    
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberPullToRefreshState()
    
    // Create ScrollBehavior manually
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    ScreenScaffold(
        scrollState = scrollState,
        topBar = {
            ScrollAwareTopBar(
                title = stringResource(R.string.history),
                scrollBehavior = scrollBehavior,
                showBackIcon = true,
                showMenuIcon = false,
                onBackClick = onNavigateBack,
                onMenuClick = null
            )
        },
        topBarScrollBehavior = scrollBehavior
    ) { paddingValues ->
        PullToRefreshBox(
            state = swipeRefreshState,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    historyItems.refresh()
                    isRefreshing = false
                }
            },
            isRefreshing = isRefreshing,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(
                    targetState = historyItems.loadState.refresh,
                    animationSpec = tween(durationMillis = 300),
                    label = "loading_state"
                ) { state ->
                    when (state) {
                        is LoadState.Loading -> {
                            if (historyItems.itemCount == 0 || isRefreshing) {
                                LoadingView(
                                    state = LoadingState.LOADING,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        is LoadState.Error -> {
                            val error = state.error
                            LoadingView(
                                state = LoadingState.ERROR,
                                errorMessage = error.message,
                                onRetry = { historyItems.retry() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        is LoadState.NotLoading -> {
                            if (historyItems.itemCount == 0) {
                                LoadingView(
                                    state = LoadingState.EMPTY,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                if (historyItems.itemCount > 0) {
                    HistoryList(
                        historyItems = historyItems,
                        scrollState = scrollState,
                        paddingValues = paddingValues,
                        onVideoClick = onVideoClick
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryList(
    historyItems: LazyPagingItems<HistoryItem>,
    scrollState: ScalingLazyListState,
    paddingValues: PaddingValues,
    onVideoClick: (VideoInfo) -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    ScalingLazyColumn(
        state = scrollState,
        contentPadding = paddingValues,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(historyItems.itemCount) { index ->
            historyItems[index]?.let { item ->
                HistoryCard(
                    item = item,
                    onClick = {
                        val videoInfo = item.toVideoInfo()
                        onVideoClick(videoInfo)
                    }
                )
            }
        }

        when (val appendState = historyItems.loadState.append) {
            is LoadState.Loading -> {
                item {
                    LoadingView(
                        state = LoadingState.LOADING,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeight * 0.3f)
                    )
                }
            }
            is LoadState.Error -> {
                item {
                    LoadingView(
                        state = LoadingState.ERROR,
                        errorMessage = appendState.error.message,
                        onRetry = { historyItems.retry() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeight * 0.3f)
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun HistoryCard(
    item: HistoryItem,
    onClick: () -> Unit
) {
    VideoCard(
        videoInfo = item.toVideoInfo(),
        onClick = {
            onClick()
        }
    )
}

private fun HistoryItem.toVideoInfo(): VideoInfo {
    val progressText = when {
        progress == 0 -> "还没看过"
        progress >= duration -> "已看完"
        else -> "看到${formatTime(progress)}"
    }
    
    return ObjectBuilder.build(
        "aid" to history.oid,
        "bvid" to history.bvid,
        "cid" to history.cid,
        "title" to title,
        "pic" to cover,
        "owner" to ObjectBuilder.build<UserInfo>(
            "name" to authorName
        ),
        "stat" to ObjectBuilder.build<Stat>(
            "aid" to history.oid,
            "view" to (viewCount ?: 0)
        ),
        "dynamic" to progressText,
        "pages" to listOf(
            ObjectBuilder.build<Page>(
                "cid" to history.cid,
                "page" to 1,
                "duration" to duration
            )
        ),
        "duration" to duration,
        "ctime" to viewAt
    )
}

private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    
    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, secs)
        else -> String.format("%d:%02d", minutes, secs)
    }
}
