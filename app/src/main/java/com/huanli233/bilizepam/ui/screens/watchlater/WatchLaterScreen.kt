package com.huanli233.bilizepam.ui.screens.watchlater

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.ScrollAwareTopBar
import com.huanli233.bilizepam.ui.components.VideoCard
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
import com.huanli233.bilizepam.ui.viewmodel.WatchLaterUiState
import com.huanli233.bilizepam.ui.viewmodel.WatchLaterViewModel
import com.huanli233.bilizepam.utils.ObjectBuilder
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.biliwebapi.bean.watchlater.WatchLaterItem
import kotlinx.coroutines.launch

@Composable
fun WatchLaterScreen(
    onVideoClick: (VideoInfo) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: WatchLaterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScalingLazyListState()
    val scope = rememberCoroutineScope()
    
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberPullToRefreshState()
    
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    ScreenScaffold(
        scrollState = scrollState,
        topBar = {
            ScrollAwareTopBar(
                title = stringResource(R.string.watch_later),
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
                    viewModel.loadWatchLaterList()
                    isRefreshing = false
                }
            },
            isRefreshing = isRefreshing,
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is WatchLaterUiState.Loading -> {
                    LoadingView(
                        state = LoadingState.LOADING,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is WatchLaterUiState.Error -> {
                    LoadingView(
                        state = LoadingState.ERROR,
                        errorMessage = state.message,
                        onRetry = { viewModel.loadWatchLaterList() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is WatchLaterUiState.Success -> {
                    if (state.items.isEmpty()) {
                        LoadingView(
                            state = LoadingState.EMPTY,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        WatchLaterList(
                            items = state.items,
                            scrollState = scrollState,
                            paddingValues = paddingValues,
                            onVideoClick = onVideoClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchLaterList(
    items: List<WatchLaterItem>,
    scrollState: androidx.wear.compose.foundation.lazy.ScalingLazyListState,
    paddingValues: PaddingValues,
    onVideoClick: (VideoInfo) -> Unit
) {
    ScalingLazyColumn(
        state = scrollState,
        contentPadding = paddingValues,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.aid }) { item ->
            VideoCard(
                videoInfo = item.toVideoInfo(),
                onClick = { onVideoClick(item.toVideoInfo()) }
            )
        }
    }
}

private fun WatchLaterItem.toVideoInfo(): VideoInfo {
    return ObjectBuilder.build(
        "aid" to aid,
        "bvid" to bvid,
        "cid" to cid,
        "title" to title,
        "pic" to pic,
        "owner" to owner,
        "stat" to stat,
        "duration" to duration.toLong(),
        "ctime" to addAt
    )
}
