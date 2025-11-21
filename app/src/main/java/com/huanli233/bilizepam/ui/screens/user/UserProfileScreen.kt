package com.huanli233.bilizepam.ui.screens.user

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.huanli233.bilizepam.R
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.PaddingDefaults
import androidx.wear.compose.material3.ScreenScaffold
import coil3.compose.AsyncImage
import com.huanli233.biliwebapi.bean.user.UserCardInfo
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.widget.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.type.WormIndicatorType
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import com.huanli233.bilizepam.ui.screens.dynamic.DynamicCard
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
import com.huanli233.bilizepam.ui.viewmodel.DynamicViewModel
import com.huanli233.bilizepam.ui.viewmodel.UserProfileViewModel
import com.huanli233.biliwebapi.bean.dynamic.Dynamic
import com.huanli233.bilizepam.ui.widget.PullToRefreshBox
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.huanli233.biliwebapi.bean.user.UserArticle
import com.huanli233.bilizepam.utils.ArticleRedirectUtil
import com.huanli233.bilizepam.utils.MsgUtil
import com.huanli233.bilizepam.utils.ObjectBuilder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import coil3.request.crossfade
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import com.huanli233.biliwebapi.bean.series.UserSeriesList
import com.huanli233.bilizepam.data.account.AccountManager
import com.huanli233.bilizepam.ui.dialog.AdaptDialog

@Composable
fun UserProfileScreen(
    mid: Long,
    onNavigateBack: () -> Unit,
    onDynamicClick: (String) -> Unit = {},
    onVideoClick: (VideoInfo) -> Unit = {},
    onOpusClick: (Long) -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onSeriesClick: (String, Long, Long, String) -> Unit = { _, _, _, _ -> },
    viewModel: UserProfileViewModel = hiltViewModel(key = "user_$mid")
) {
    val context = LocalContext.current
    val userInfo by viewModel.userInfo.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val followLoading by viewModel.followLoading.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    val dynamicViewModel = hiltViewModel<DynamicViewModel>(key = "dynamic_$mid")
    
    val currentUserMid = AccountManager.currentAccount.accountId

    LaunchedEffect(mid) {
        viewModel.loadUser(mid)
    }

    ScreenScaffold(
        topBar = scrollAwareTopBar(
            title = userInfo?.card?.name ?: "",
            showBackIcon = true,
            onBackClick = onNavigateBack,
            scrollBehavior = scrollBehavior
        ),
        topBarScrollBehavior = scrollBehavior
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (userInfo == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> ProfilePage(
                            userInfo = userInfo,
                            paddingValues = paddingValues,
                            isFollowing = isFollowing,
                            followLoading = followLoading,
                            onFollowClick = { viewModel.toggleFollow() },
                            currentUserMid = currentUserMid,
                            onLogoutClick = {
                                viewModel.logout(
                                    onSuccess = {
                                        MsgUtil.showMsg("账号已退出")
                                        onNavigateBack()
                                    },
                                    onError = { errorMsg ->
                                        MsgUtil.showMsg(errorMsg)
                                    }
                                )
                            }
                        )
                        1 -> DynamicsPage(paddingValues = paddingValues, mid = mid, onDynamicClick = onDynamicClick, onVideoClick = onVideoClick, onImageClick = onImageClick)
                        2 -> VideosPage(paddingValues = paddingValues, viewModel = viewModel, onVideoClick = onVideoClick, onSeriesClick = onSeriesClick)
                        3 -> ArticlesPage(paddingValues = paddingValues, viewModel = viewModel, onOpusClick = onOpusClick)
                        else -> PlaceholderListPage(paddingValues)
                    }
                }
            }

            DotsIndicator(
                dotCount = 4,
                pagerState = pagerState,
                type = WormIndicatorType(
                    dotsGraphic = DotGraphic(
                        16.dp,
                        borderWidth = 2.dp,
                        borderColor = MaterialTheme.colorScheme.primary,
                        color = Color.Transparent,
                    ),
                    wormDotGraphic = DotGraphic(
                        16.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = PaddingDefaults.verticalOptContentPadding())
            )
        }
    }
}

@Composable
private fun DynamicsPage(
    paddingValues: PaddingValues,
    mid: Long,
    viewModel: DynamicViewModel = hiltViewModel(key = "user_dynamic_$mid"),
    onDynamicClick: (String) -> Unit = {},
    onVideoClick: (VideoInfo) -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> }
) {
    LaunchedEffect(mid) {
        viewModel.setHostUid(mid)
    }
    
    val dynamics = viewModel.dynamicFlow.collectAsLazyPagingItems()
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(dynamics.loadState.refresh) {
        if (dynamics.loadState.refresh is LoadState.NotLoading && isRefreshing) {
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            dynamics.refresh()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            state = scrollState,
            contentPadding = PaddingValues()
        ) {
            item {
                Text(
                    text = "动态",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            
            item {
                when {
                    isRefreshing || (dynamics.loadState.refresh is LoadState.Loading && dynamics.itemCount == 0) -> {
                        LoadingView(state = LoadingState.LOADING, modifier = Modifier.fillMaxSize())
                    }
                    dynamics.loadState.refresh is LoadState.Error && dynamics.itemCount == 0 -> {
                        val error = (dynamics.loadState.refresh as LoadState.Error).error
                        LoadingView(
                            state = LoadingState.ERROR,
                            errorMessage = error.message,
                            onRetry = { dynamics.retry() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    dynamics.itemCount == 0 -> {
                        LoadingView(state = LoadingState.EMPTY, modifier = Modifier.fillMaxSize())
                    }
                }
            }

            items(dynamics.itemCount) { index ->
                dynamics[index]?.let { item ->
                    DynamicCard(
                        dynamic = item,
                        onDynamicClick = { onDynamicClick(it.id) },
                        onClick = { onDynamicClick(item.id) },
                        onVideoClick = { bvid ->
                            onVideoClick(ObjectBuilder.build("bvid" to bvid))
                        },
                        onImageClick = onImageClick
                    )
                }
            }

            item {
                when (val state = dynamics.loadState.append) {
                    is LoadState.Loading -> {
                        LoadingView(state = LoadingState.LOADING)
                    }
                    is LoadState.Error -> {
                        LoadingView(
                            state = LoadingState.ERROR,
                            errorMessage = state.error.message,
                            onRetry = { dynamics.retry() }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun ProfilePage(
    userInfo: UserCardInfo?,
    paddingValues: PaddingValues,
    isFollowing: Boolean,
    followLoading: Boolean,
    onFollowClick: () -> Unit,
    currentUserMid: Long,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = userInfo?.card?.face,
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userInfo?.card?.name ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!userInfo?.card?.sign.isNullOrEmpty()) {
                    Text(
                        text = userInfo?.card?.sign ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (userInfo?.card?.mid?.toLongOrNull() != currentUserMid && currentUserMid != 0L) {
            FilledTonalButton(
                onClick = onFollowClick,
                enabled = !followLoading,
                modifier = Modifier.fillMaxWidth().animateContentSize()
            ) {
                if (followLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isFollowing) "已关注" else "关注")
                }
            }
        }

        StatsGrid(
            fans = userInfo?.follower ?: 0,
            following = userInfo?.card?.attention ?: 0,
            level = userInfo?.card?.levelInfo?.currentLevel ?: 0,
            archives = userInfo?.archiveCount ?: 0
        )
        
        if (userInfo?.card?.mid?.toLongOrNull() == currentUserMid && currentUserMid != 0L) {
            var showLogoutDialog by remember { mutableStateOf(false) }
            
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("退出登录")
            }
            
            if (showLogoutDialog) {
                AdaptDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("退出登录") },
                    text = { Text("确定要退出当前账号吗？") },
                    confirmButton = { close ->
                        TextButton(
                            onClick = {
                                close()
                                onLogoutClick()
                            }
                        ) {
                            Text("确定", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("取消")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun StatsGrid(
    fans: Int,
    following: Int,
    level: Int,
    archives: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatTonalCard(title = "粉丝", value = formatCount(fans), modifier = Modifier.weight(1f))
            StatTonalCard(title = "关注", value = formatCount(following), modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatTonalCard(title = "等级", value = "Lv$level", modifier = Modifier.weight(1f))
            StatTonalCard(title = "稿件", value = formatCount(archives), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTonalCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = modifier) {
        Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun VideosPage(
    paddingValues: PaddingValues,
    viewModel: UserProfileViewModel,
    onVideoClick: (VideoInfo) -> Unit = {},
    onSeriesClick: (String, Long, Long, String) -> Unit = { _, _, _, _ -> }
) {
    val videos = viewModel.videosFlow.collectAsLazyPagingItems()
    val seriesList by viewModel.seriesList.collectAsState()
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    var isRefreshing by remember { mutableStateOf(false) }
    var isSeriesExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(videos.loadState.refresh) {
        if (videos.loadState.refresh is LoadState.NotLoading && isRefreshing) {
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            videos.refresh()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            state = scrollState,
            contentPadding = PaddingValues()
        ) {
            if (seriesList.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SeriesSectionHeader(
                            count = seriesList.size,
                            isExpanded = isSeriesExpanded,
                            onToggle = { isSeriesExpanded = !isSeriesExpanded }
                        )
                        
                        AnimatedVisibility(
                            visible = isSeriesExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column {
                                seriesList.forEach { series ->
                                    SeriesCard(
                                        series = series,
                                        onClick = { 
                                            onSeriesClick(series.type, series.meta.mid, series.id, series.meta.name)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Text(
                    text = "视频",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            
            item {
                when {
                    isRefreshing || (videos.loadState.refresh is LoadState.Loading && videos.itemCount == 0) -> {
                        LoadingView(state = LoadingState.LOADING, modifier = Modifier.fillMaxSize())
                    }
                    videos.loadState.refresh is LoadState.Error && videos.itemCount == 0 -> {
                        val error = (videos.loadState.refresh as LoadState.Error).error
                        LoadingView(
                            state = LoadingState.ERROR,
                            errorMessage = error.message,
                            onRetry = { videos.retry() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    videos.itemCount == 0 -> {
                        LoadingView(state = LoadingState.EMPTY, modifier = Modifier.fillMaxSize())
                    }
                }
            }

            items(videos.itemCount) { index ->
                videos[index]?.let { video ->
                    com.huanli233.bilizepam.ui.screens.recommend.VideoCard(
                        videoInfo = video,
                        onClick = onVideoClick
                    )
                }
            }

            item {
                when (val state = videos.loadState.append) {
                    is LoadState.Loading -> {
                        LoadingView(state = LoadingState.LOADING)
                    }
                    is LoadState.Error -> {
                        LoadingView(
                            state = LoadingState.ERROR,
                            errorMessage = state.error.message,
                            onRetry = { videos.retry() }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun ArticlesPage(
    paddingValues: PaddingValues,
    viewModel: UserProfileViewModel,
    onOpusClick: (Long) -> Unit = {}
) {
    val articles = viewModel.articlesFlow.collectAsLazyPagingItems()
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    var isRefreshing by remember { mutableStateOf(false) }
    var loadingArticles by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(articles.loadState.refresh) {
        if (articles.loadState.refresh is LoadState.NotLoading && isRefreshing) {
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            articles.refresh()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            state = scrollState,
            contentPadding = PaddingValues()
        ) {
            item {
                Text(
                    text = "专栏",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            
            item {
                when {
                    isRefreshing || (articles.loadState.refresh is LoadState.Loading && articles.itemCount == 0) -> {
                        LoadingView(state = LoadingState.LOADING, modifier = Modifier.fillMaxSize())
                    }
                    articles.loadState.refresh is LoadState.Error && articles.itemCount == 0 -> {
                        val error = (articles.loadState.refresh as LoadState.Error).error
                        LoadingView(
                            state = LoadingState.ERROR,
                            errorMessage = error.message,
                            onRetry = { articles.retry() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    articles.itemCount == 0 -> {
                        LoadingView(state = LoadingState.EMPTY, modifier = Modifier.fillMaxSize())
                    }
                }
            }

            items(articles.itemCount) { index ->
                articles[index]?.let { article ->
                    ArticleCard(
                        article = article,
                        isLoading = loadingArticles.contains(article.id),
                        onClick = {
                            if (!loadingArticles.contains(article.id)) {
                                loadingArticles = loadingArticles + article.id
                                coroutineScope.launch {
                                    val result = ArticleRedirectUtil.convertCvidToOpusId(article.id)
                                    loadingArticles = loadingArticles - article.id
                                    result.fold(
                                        onSuccess = { opusId ->
                                            onOpusClick(opusId)
                                        },
                                        onFailure = { error ->
                                            MsgUtil.showMsg(error.message ?: "转换失败")
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }

            item {
                when (val state = articles.loadState.append) {
                    is LoadState.Loading -> {
                        LoadingView(state = LoadingState.LOADING)
                    }
                    is LoadState.Error -> {
                        LoadingView(
                            state = LoadingState.ERROR,
                            errorMessage = state.error.message,
                            onRetry = { articles.retry() }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(
    article: UserArticle,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (!article.bannerUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(article.bannerUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.author.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${formatCount(article.stats.view)}阅读",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isLoading) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SeriesSectionHeader(
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "合集/系列",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            painter = painterResource(
                if (isExpanded) R.drawable.icon_arrow_up else R.drawable.icon_arrow_down
            ),
            contentDescription = if (isExpanded) "收起" else "展开",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SeriesCard(
    series: UserSeriesList.SeriesItem,
    onClick: (UserSeriesList.SeriesItem) -> Unit
) {
    Card(
        onClick = { onClick(series) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (series.meta.cover.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(series.meta.cover)
                        .crossfade(true)
                        .build(),
                    contentDescription = series.meta.name,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = series.meta.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (series.meta.description.isNotEmpty()) {
                    Text(
                        text = series.meta.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (series.type == "season") "合集" else "系列",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${series.meta.total}个视频",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderListPage(paddingValues: PaddingValues) {
    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 10000 -> "${count / 10000}万"
        else -> count.toString()
    }
}
