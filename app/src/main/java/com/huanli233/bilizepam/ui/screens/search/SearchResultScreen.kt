package com.huanli233.bilizepam.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.material3.*
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
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
import com.huanli233.bilizepam.ui.viewmodel.ArticleRedirectState
import com.huanli233.bilizepam.utils.MsgUtil
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun SearchResultScreen(
    query: String,
    onNavigateBack: () -> Unit,
    onVideoClick: (Long, String) -> Unit = { _, _ -> },
    onUserClick: (Long) -> Unit = {},
    onOpusClick: (Long) -> Unit = {},
    viewModel: SearchResultViewModel = hiltViewModel(key = "search_$query")
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val currentType by viewModel.currentType.collectAsState()
    var selectedType by remember { mutableStateOf("video") }
    val lazyListState = rememberLazyListState()
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    LaunchedEffect(query, selectedType) {
        // 只在还没有搜索结果或类型改变时才搜索
        if (searchResults == null || currentType != selectedType) {
            viewModel.search(query, selectedType)
        }
    }
    
    val pagingItems = searchResults?.collectAsLazyPagingItems()

    ScreenScaffold(
        scrollState = lazyListState,
        topBar = scrollAwareTopBar(
            title = query,
            showBackIcon = true,
            onBackClick = onNavigateBack,
            scrollBehavior = scrollBehavior
        ),
        topBarScrollBehavior = scrollBehavior
    ) { paddingValues ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                SearchTypeChips(
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (pagingItems == null) {
                item {
                    LoadingView(
                        state = LoadingState.LOADING,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            } else {
                when (val refreshState = pagingItems.loadState.refresh) {
                    is LoadState.Loading -> {
                        item {
                            LoadingView(
                                state = LoadingState.LOADING,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }
                    is LoadState.Error -> {
                        item {
                            LoadingView(
                                state = LoadingState.ERROR,
                                errorMessage = refreshState.error.message ?: "Unknown error",
                                onRetry = { pagingItems.retry() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }
                    is LoadState.NotLoading -> {
                        if (pagingItems.itemCount == 0) {
                            item {
                                LoadingView(
                                    state = LoadingState.EMPTY,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                            }
                        } else {
                            items(pagingItems.itemCount) { index ->
                                pagingItems[index]?.let { item ->
                                    when (selectedType) {
                                        "video" -> SearchVideoCard(item, onVideoClick)
                                        "bili_user" -> UserResultItem(item, onUserClick)
                                        "article" -> SearchArticleCard(item, onOpusClick, viewModel)
                                        else -> SearchVideoCard(item, onVideoClick)
                                    }
                                }
                            }
                            
                            when (val appendState = pagingItems.loadState.append) {
                                is LoadState.Loading -> {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                        }
                                    }
                                }
                                is LoadState.Error -> {
                                    item {
                                        LoadingView(
                                            state = LoadingState.ERROR,
                                            errorMessage = appendState.error.message ?: "Unknown error",
                                            onRetry = { pagingItems.retry() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(100.dp)
                                        )
                                    }
                                }
                                is LoadState.NotLoading -> {}
                            }
                        }
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
        SearchTypeChip(
            label = stringResource(R.string.search_user),
            selected = selectedType == "bili_user",
            onClick = { onTypeSelected("bili_user") }
        )
        SearchTypeChip(
            label = stringResource(R.string.search_article),
            selected = selectedType == "article",
            onClick = { onTypeSelected("article") }
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
private fun SearchArticleCard(
    item: SearchItem,
    onOpusClick: (Long) -> Unit,
    viewModel: SearchResultViewModel
) {
    val loadingArticles by viewModel.loadingArticles.collectAsState()
    val cvid = item.id ?: 0L
    val isLoading = loadingArticles.contains(cvid)
    
    SearchArticleCardContent(
        title = item.title ?: "",
        cover = item.imageUrls?.firstOrNull() ?: "",
        author = item.author ?: "",
        view = item.view ?: 0,
        like = item.like ?: 0,
        isLoading = isLoading,
        onClick = {
            if (cvid > 0 && !isLoading) {
                viewModel.convertCvidToOpusId(
                    cvid = cvid,
                    onSuccess = { opusId ->
                        onOpusClick(opusId)
                    },
                    onError = { error ->
                        MsgUtil.showMsg(error)
                    }
                )
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserResultItem(
    item: SearchItem,
    onClick: (Long) -> Unit
) {
    Card(
        onClick = { item.mid?.let { onClick(it) } },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
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
                    .size(48.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
            ) {
                var isAvatarLoading by remember { mutableStateOf(true) }
                
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (item.upic?.startsWith("http") == true) item.upic else "http:${item.upic}")
                        .crossfade(200)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                    onSuccess = { isAvatarLoading = false },
                    onError = { isAvatarLoading = false }
                )
                
                if (isAvatarLoading) {
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
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.uname?.replace("<em class=\"keyword\">", "")
                        ?.replace("</em>", "") ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    item.level?.let { level ->
                        Text(
                            text = "Lv$level",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    
                    item.fans?.let { fans ->
                        Text(
                            text = "${formatPlayCount(fans)} 粉丝",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                item.description?.let { desc ->
                    if (desc.isNotBlank()) {
                        Text(
                            text = desc.replace("<em class=\"keyword\">", "")
                                .replace("</em>", ""),
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchArticleCardContent(
    title: String,
    cover: String,
    author: String,
    view: Long,
    like: Long,
    isLoading: Boolean,
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
        onClick = onClick,
        enabled = !isLoading
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (cover.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    var isImageLoading by remember { mutableStateOf(true) }
                    
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(if (cover.startsWith("http")) cover else "http:$cover")
                            .crossfade(200)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                        onSuccess = { isImageLoading = false },
                        onError = { isImageLoading = false }
                    )
                    
                    if (isImageLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .shimmer()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(vertical = 2.dp)
                    .animateContentSize(
                        animationSpec = tween(durationMillis = 300)
                    )
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
                            text = formatPlayCount(view),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatPlayCount(like),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                AnimatedVisibility(
                    visible = isLoading,
                    enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Loading...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
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
