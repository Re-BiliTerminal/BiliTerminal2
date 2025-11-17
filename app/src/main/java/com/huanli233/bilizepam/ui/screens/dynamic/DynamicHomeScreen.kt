package com.huanli233.bilizepam.ui.screens.dynamic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import com.huanli233.bilizepam.ui.widget.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.PaddingDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.verticalContentPadding
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
import com.huanli233.bilizepam.ui.viewmodel.DynamicViewModel
import com.huanli233.biliwebapi.bean.dynamic.Dynamic
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicHomeScreen(
    viewModel: DynamicViewModel = hiltViewModel(),
    onDynamicClick: (Dynamic) -> Unit = {},
    onUserClick: (Long) -> Unit = {},
    onVideoClick: (String) -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onMenuClick: () -> Unit = {}
) {
    val dynamics = viewModel.dynamicFlow.collectAsLazyPagingItems()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    var isRefreshing by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()
    
    LaunchedEffect(dynamics.loadState.refresh) {
        if (dynamics.loadState.refresh is LoadState.NotLoading && isRefreshing) {
            isRefreshing = false
        }
    }

    ScreenScaffold(
        scrollState = scrollState,
        topBar = scrollAwareTopBar(
            title = stringResource(R.string.dynamic),
            showBackIcon = false,
            showMenuIcon = true,
            onMenuClick = onMenuClick,
            scrollBehavior = scrollBehavior
        ),
        topBarScrollBehavior = scrollBehavior
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    dynamics.refresh()
                },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    isRefreshing || (dynamics.loadState.refresh is LoadState.Loading && dynamics.itemCount == 0) -> {
                        LoadingView(
                            state = LoadingState.LOADING,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    dynamics.loadState.refresh is LoadState.Error && dynamics.itemCount == 0 -> {
                        val error = (dynamics.loadState.refresh as LoadState.Error).error
                        LoadingView(
                            state = LoadingState.ERROR,
                            errorMessage = error.stackTraceToString(),
                            onRetry = { dynamics.retry() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    dynamics.itemCount == 0 -> {
                        LoadingView(
                            state = LoadingState.EMPTY,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        ScalingLazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = scrollState,
                            contentPadding = paddingValues
                        ) {
                            items(dynamics.itemCount) { index ->
                                dynamics[index]?.let { dynamic ->
                                    DynamicCard(
                                        dynamic = dynamic,
                                        onClick = { onDynamicClick(dynamic) },
                                        onUserClick = onUserClick,
                                        onVideoClick = onVideoClick,
                                        onImageClick = onImageClick,
                                        onLikeClick = { dynamicId, isLiked ->
                                            viewModel.likeDynamic(dynamicId, isLiked)
                                        },
                                        onDynamicClick = onDynamicClick
                                    )
                                }
                            }
                            
                            if (dynamics.loadState.append is LoadState.Loading) {
                                item {
                                    LoadingView(
                                        state = LoadingState.LOADING,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
