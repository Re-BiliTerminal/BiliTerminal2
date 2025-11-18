package com.huanli233.bilizepam.ui.screens.favorite

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
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
import com.huanli233.bilizepam.ui.viewmodel.FavoriteUiState
import com.huanli233.bilizepam.ui.viewmodel.FavoriteViewModel
import com.huanli233.biliwebapi.bean.favorite.FavoriteBox
import kotlinx.coroutines.launch

@Composable
fun FavoriteScreen(
    onFolderClick: (Long, String) -> Unit,
    onOpusFavoriteClick: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: FavoriteViewModel = hiltViewModel()
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
                title = stringResource(R.string.favorite),
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
                    viewModel.loadFavoriteFolders()
                    isRefreshing = false
                }
            },
            isRefreshing = isRefreshing,
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is FavoriteUiState.Loading -> {
                    LoadingView(
                        state = LoadingState.LOADING,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is FavoriteUiState.Error -> {
                    LoadingView(
                        state = LoadingState.ERROR,
                        errorMessage = state.message,
                        onRetry = { viewModel.loadFavoriteFolders() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is FavoriteUiState.Success -> {
                    if (state.folders.isEmpty()) {
                        LoadingView(
                            state = LoadingState.EMPTY,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        FavoriteFolderList(
                            folders = state.folders,
                            scrollState = scrollState,
                            paddingValues = paddingValues,
                            onFolderClick = onFolderClick,
                            onOpusFavoriteClick = onOpusFavoriteClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteFolderList(
    folders: List<FavoriteBox>,
    scrollState: androidx.wear.compose.foundation.lazy.ScalingLazyListState,
    paddingValues: PaddingValues,
    onFolderClick: (Long, String) -> Unit,
    onOpusFavoriteClick: () -> Unit
) {
    ScalingLazyColumn(
        state = scrollState,
        contentPadding = paddingValues,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(folders, key = { it.favBox }) { folder ->
            FavoriteFolderCard(
                folder = folder,
                onClick = { onFolderClick(folder.favBox, folder.name) }
            )
        }
        
        item {
            OpusFavoriteCard(onClick = onOpusFavoriteClick)
        }
    }
}

@Composable
private fun FavoriteFolderCard(
    folder: FavoriteBox,
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val coverUrl = folder.videos?.firstOrNull()?.pic ?: ""
            if (coverUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(coverUrl)
                        .crossfade(200)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${folder.count}/${folder.maxCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OpusFavoriteCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Article,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = stringResource(R.string.opus_favorite),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
