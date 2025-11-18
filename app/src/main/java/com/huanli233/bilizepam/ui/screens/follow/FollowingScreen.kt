package com.huanli233.bilizepam.ui.screens.follow

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.ScrollAwareTopBar
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
import com.huanli233.bilizepam.ui.viewmodel.FollowingViewModel
import com.huanli233.biliwebapi.bean.follow.FollowUser
import kotlinx.coroutines.launch

@Composable
fun FollowingScreen(
    mid: Long,
    onUserClick: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: FollowingViewModel = hiltViewModel()
) {
    val followingList = viewModel.followingFlow.collectAsLazyPagingItems()
    val scrollState = rememberScalingLazyListState()
    val scope = rememberCoroutineScope()
    
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberPullToRefreshState()
    
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    LaunchedEffect(mid) {
        viewModel.setMid(mid)
    }

    ScreenScaffold(
        scrollState = scrollState,
        topBar = {
            ScrollAwareTopBar(
                title = stringResource(R.string.following),
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
                    followingList.refresh()
                    isRefreshing = false
                }
            },
            isRefreshing = isRefreshing,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(
                    targetState = followingList.loadState.refresh,
                    animationSpec = tween(durationMillis = 300),
                    label = "loading_state"
                ) { state ->
                    when (state) {
                        is LoadState.Loading -> {
                            if (followingList.itemCount == 0 || isRefreshing) {
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
                                onRetry = { followingList.retry() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {}
                    }
                }

                if (followingList.loadState.refresh !is LoadState.Loading && followingList.itemCount == 0) {
                    LoadingView(
                        state = LoadingState.EMPTY,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (followingList.itemCount > 0) {
                    FollowingList(
                        followingList = followingList,
                        scrollState = scrollState,
                        paddingValues = paddingValues,
                        onUserClick = onUserClick
                    )
                }
            }
        }
    }
}

@Composable
private fun FollowingList(
    followingList: LazyPagingItems<FollowUser>,
    scrollState: androidx.wear.compose.foundation.lazy.ScalingLazyListState,
    paddingValues: PaddingValues,
    onUserClick: (Long) -> Unit
) {
    ScalingLazyColumn(
        state = scrollState,
        contentPadding = paddingValues,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(followingList.itemCount) { index ->
            followingList[index]?.let { user ->
                UserCard(
                    user = user,
                    onClick = { onUserClick(user.mid) }
                )
            }
        }
    }
}

@Composable
private fun UserCard(
    user: FollowUser,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.face)
                    .crossfade(200)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = user.uname,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (user.sign.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.sign,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
