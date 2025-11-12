package com.huanli233.bilizepam.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material3.*
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
import com.huanli233.bilizepam.ui.viewmodel.SearchResultState
import com.huanli233.bilizepam.ui.viewmodel.SearchResultViewModel
import com.huanli233.biliwebapi.bean.search.SearchItem
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.valentinilk.shimmer.shimmer
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.FilterChip
import coil3.request.crossfade

@Composable
fun SearchResultScreen(
    query: String,
    onNavigateBack: () -> Unit,
    onVideoClick: (Long, String) -> Unit = { _, _ -> },
    onUserClick: (Long) -> Unit = {},
    viewModel: SearchResultViewModel = hiltViewModel()
) {
    val searchState by viewModel.searchState.collectAsState()
    val currentType by viewModel.currentType.collectAsState()
    var selectedType by remember { mutableStateOf("video") }

    LaunchedEffect(query, selectedType) {
        viewModel.search(query, selectedType)
    }

    ScreenScaffold(
        topBar = scrollAwareTopBar(
            title = query,
            showBackIcon = true,
            onBackClick = onNavigateBack
        )
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchTypeChips(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            Box(modifier = Modifier.fillMaxSize()) {
            when (val state = searchState) {
                is SearchResultState.Loading -> {
                    LoadingView(
                        state = LoadingState.LOADING,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is SearchResultState.Success -> {
                    SearchResultList(
                        items = state.items,
                        type = selectedType,
                        onVideoClick = onVideoClick,
                        onUserClick = onUserClick
                    )
                }
                is SearchResultState.Error -> {
                    LoadingView(
                        state = LoadingState.ERROR,
                        errorMessage = state.message,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is SearchResultState.Empty -> {
                    LoadingView(
                        state = LoadingState.EMPTY,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun SearchTypeChips(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SearchTypeChip(
            label = stringResource(R.string.search_video),
            selected = selectedType == "video",
            onClick = { onTypeSelected("video") }
        )
    }
}

@Composable
private fun SearchTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp)
                )
            }
        } else null,
        modifier = Modifier.height(28.dp)
    )
}

@Composable
private fun SearchResultList(
    items: List<SearchItem>,
    type: String,
    onVideoClick: (Long, String) -> Unit,
    onUserClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(items) { item ->
            when (type) {
                "video" -> SearchVideoCard(item, onVideoClick)
                "user" -> UserResultItem(item, onUserClick)
                else -> SearchVideoCard(item, onVideoClick)
            }
        }
    }
}

@Composable
private fun SearchVideoCard(
    item: SearchItem,
    onClick: (Long, String) -> Unit
) {
    SearchVideoCardContent(
        title = item.title ?: "",
        cover = item.pic ?: "",
        author = item.author ?: "",
        play = item.play ?: 0,
        onClick = { 
            val aid = item.aid ?: 0L
            val bvid = item.bvid ?: ""
            onClick(aid, bvid)
        }
    )
}

@Composable
private fun UserResultItem(
    item: SearchItem,
    onClick: (Long) -> Unit
) {
    Card(
        onClick = { item.mid?.let { onClick(it) } },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.uname ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            item.fans?.let {
                Text(
                    text = "${it}粉丝",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchVideoCardContent(
    title: String,
    cover: String,
    author: String,
    play: Long,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        onClick = onClick
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                var isLoading by remember { mutableStateOf(true) }
                
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (cover.startsWith("http")) cover else "http:$cover")
                        .crossfade(200)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                    onSuccess = { isLoading = false },
                    onError = { isLoading = false }
                )
                
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .shimmer()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = title.replace("<em class=\"keyword\">", "").replace("</em>", ""),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = author,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatPlayCount(play),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatPlayCount(count: Long): String {
    return when {
        count >= 100000000 -> String.format("%.1f亿", count / 100000000.0)
        count >= 10000 -> String.format("%.1f万", count / 10000.0)
        else -> count.toString()
    }
}
