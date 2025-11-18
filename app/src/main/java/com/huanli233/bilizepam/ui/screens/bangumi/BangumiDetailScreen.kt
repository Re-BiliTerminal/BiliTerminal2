package com.huanli233.bilizepam.ui.screens.bangumi

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.ScreenScaffold
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.huanli233.biliwebapi.bean.bangumi.BangumiSections
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.screens.comment.CommentScreen
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
import com.huanli233.bilizepam.ui.viewmodel.BangumiViewModel
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.WormIndicatorType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BangumiDetailScreen(
    mediaId: Long,
    viewModel: BangumiViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onPlayEpisode: (aid: Long, cid: Long) -> Unit = { _, _ -> }
) {
    val bangumiInfo by viewModel.bangumiInfo.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedSection by viewModel.selectedSection.collectAsState()
    val selectedEpisode by viewModel.selectedEpisode.collectAsState()
    
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()
    
    val bangumiScrollState = rememberScrollState()
    val commentScrollState = rememberScalingLazyListState()
    
    LaunchedEffect(mediaId) {
        viewModel.loadBangumi(mediaId)
    }
    
    ScreenScaffold(
        topBar = scrollAwareTopBar(
            title = bangumiInfo?.media?.title ?: "番剧详情",
            showBackIcon = true,
            onBackClick = onNavigateBack,
            scrollBehavior = scrollBehavior
        ),
        topBarScrollBehavior = scrollBehavior
    ) { paddingValues ->
        when {
            isLoading -> {
                LoadingView(
                    state = LoadingState.LOADING,
                    modifier = Modifier.fillMaxSize()
                )
            }
            error != null -> {
                LoadingView(
                    state = LoadingState.ERROR,
                    errorMessage = error,
                    onRetry = { viewModel.loadBangumi(mediaId) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            bangumiInfo != null && sections != null -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> BangumiContent(
                                bangumiInfo = bangumiInfo!!,
                                sections = sections!!,
                                selectedSection = selectedSection,
                                selectedEpisode = selectedEpisode,
                                onSectionSelected = { viewModel.selectSection(it) },
                                onEpisodeSelected = { viewModel.selectEpisode(it) },
                                onPlayClick = {
                                    val episode = viewModel.getCurrentEpisode()
                                    if (episode != null) {
                                        onPlayEpisode(episode.aid, episode.cid)
                                    }
                                },
                                scrollState = bangumiScrollState,
                                paddingValues = paddingValues
                            )
                            1 -> {
                                val currentEpisode = viewModel.getCurrentEpisode()
                                if (currentEpisode != null) {
                                    CommentScreen(
                                        aid = currentEpisode.aid,
                                        type = 1,
                                        onCommentDetailClick = {},
                                        onWriteReplyClick = { _, _, _, _ -> },
                                        paddingValues = paddingValues
                                    )
                                }
                            }
                        }
                    }
                    
                    DotsIndicator(
                        dotCount = 2,
                        type = WormIndicatorType(
                            dotsGraphic = DotGraphic(
                                size = 8.dp,
                                borderWidth = 0.dp,
                                borderColor = MaterialTheme.colorScheme.primary,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ),
                        pagerState = pagerState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BangumiContent(
    bangumiInfo: com.huanli233.biliwebapi.bean.bangumi.BangumiDetail,
    sections: BangumiSections,
    selectedSection: Int,
    selectedEpisode: Int,
    onSectionSelected: (Int) -> Unit,
    onEpisodeSelected: (Int) -> Unit,
    onPlayClick: () -> Unit,
    scrollState: androidx.compose.foundation.ScrollState,
    paddingValues: PaddingValues
) {
    val allSections = remember(sections) {
        buildList {
            sections.mainSection?.let { add(it) }
            sections.sections?.let { addAll(it) }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(paddingValues)
    ) {
        BangumiHeader(bangumiInfo.media)
        
        if (allSections.isNotEmpty()) {
            SectionSelector(
                sections = allSections,
                selectedSection = selectedSection,
                onSectionSelected = onSectionSelected
            )
            
            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("播放")
            }
            
            allSections[selectedSection].episodes.forEachIndexed { index, episode ->
                EpisodeItem(
                    episode = episode,
                    isSelected = index == selectedEpisode,
                    onClick = { onEpisodeSelected(index) }
                )
            }
        } else {
            Text(
                text = "敬请期待",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun BangumiHeader(info: com.huanli233.biliwebapi.bean.bangumi.BangumiInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(info.coverHorizontal.ifEmpty { info.cover })
                .crossfade(true)
                .build(),
            contentDescription = info.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = info.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        val newEp = info.newEp
        if (newEp != null && newEp.indexShow.isNotEmpty()) {
            Text(
                text = newEp.indexShow,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            val rating = info.rating
            if (rating != null && rating.score > 0) {
                Text(
                    text = "评分: ${rating.score}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (info.typeName.isNotEmpty()) {
                Text(
                    text = info.typeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionSelector(
    sections: List<BangumiSections.Section>,
    selectedSection: Int,
    onSectionSelected: (Int) -> Unit
) {
    if (sections.size > 1) {
        var showDialog by remember { mutableStateOf(false) }
        
        Card(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${sections[selectedSection].title} (点击切换)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("选择分集") },
                text = {
                    Column {
                        sections.forEachIndexed { index, section ->
                            Text(
                                text = section.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSectionSelected(index)
                                        showDialog = false
                                    }
                                    .padding(12.dp),
                                color = if (index == selectedSection) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("关闭")
                    }
                }
            )
        }
    }
}

@Composable
private fun EpisodeItem(
    episode: BangumiSections.Episode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = episode.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                if (episode.longTitle.isNotEmpty()) {
                    Text(
                        text = episode.longTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            if (episode.badge.isNotEmpty()) {
                Text(
                    text = episode.badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
