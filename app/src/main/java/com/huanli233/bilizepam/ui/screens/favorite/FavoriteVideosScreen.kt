package com.huanli233.bilizepam.ui.screens.favorite

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.bilizepam.ui.components.ScrollAwareTopBar
import com.huanli233.bilizepam.ui.components.VideoCard
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
import com.huanli233.bilizepam.ui.viewmodel.FavoriteVideosViewModel
import com.huanli233.bilizepam.utils.ObjectBuilder
import com.huanli233.biliwebapi.bean.favorite.FavoriteVideo
import com.huanli233.biliwebapi.bean.video.VideoInfo
import kotlinx.coroutines.launch

@Composable
fun FavoriteVideosScreen(
    mid: Long,
    fid: Long,
    folderName: String,
    onVideoClick: (VideoInfo) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: FavoriteVideosViewModel = hiltViewModel()
) {
    val videos = viewModel.videosFlow.collectAsLazyPagingItems()
    val scrollState = rememberScalingLazyListState()
    val scope = rememberCoroutineScope()
    
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberPullToRefreshState()
    
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    LaunchedEffect(mid, fid) {
        viewModel.setFolder(mid, fid)
    }

    ScreenScaffold(
        scrollState = scrollState,
        topBar = {
            ScrollAwareTopBar(
                title = folderName,
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
                    videos.refresh()
                    isRefreshing = false
                }
            },
            isRefreshing = isRefreshing,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(
                    targetState = videos.loadState.refresh,
                    animationSpec = tween(durationMillis = 300),
                    label = "loading_state"
                ) { state ->
                    when (state) {
                        is LoadState.Loading -> {
                            if (videos.itemCount == 0 || isRefreshing) {
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
                                onRetry = { videos.retry() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {}
                    }
                }

                if (videos.loadState.refresh !is LoadState.Loading && videos.itemCount == 0) {
                    LoadingView(
                        state = LoadingState.EMPTY,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (videos.itemCount > 0) {
                    FavoriteVideosList(
                        videos = videos,
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
private fun FavoriteVideosList(
    videos: LazyPagingItems<FavoriteVideo>,
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
        items(videos.itemCount) { index ->
            videos[index]?.let { video ->
                VideoCard(
                    videoInfo = video.toVideoInfo(),
                    onClick = { onVideoClick(video.toVideoInfo()) }
                )
            }
        }
    }
}

private fun FavoriteVideo.toVideoInfo(): VideoInfo {
    return ObjectBuilder.build<VideoInfo>(
        "aid" to aid,
        "bvid" to bvid,
        "cid" to cid,
        "title" to title,
        "pic" to pic,
        "owner" to owner,
        "stat" to stat,
        "duration" to duration.toLong()
    )
}
