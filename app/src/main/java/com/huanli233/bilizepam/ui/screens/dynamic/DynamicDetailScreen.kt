package com.huanli233.bilizepam.ui.screens.dynamic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.screens.comment.CommentScreen
import com.huanli233.bilizepam.ui.viewmodel.DynamicDetailUiState
import com.huanli233.bilizepam.ui.viewmodel.DynamicDetailViewModel
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.WormIndicatorType

@Composable
fun DynamicDetailScreen(
    dynamicId: String,
    navController: NavController,
    onNavigateBack: () -> Unit = {},
    onUserClick: (Long) -> Unit = {},
    onVideoClick: (String) -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onDynamicClick: (com.huanli233.biliwebapi.bean.dynamic.Dynamic) -> Unit = {},
    viewModel: DynamicDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val commentScrollState = rememberScalingLazyListState()

    LaunchedEffect(dynamicId) {
        viewModel.loadDynamic(dynamicId)
    }

    ScreenScaffold(
        topBar = scrollAwareTopBar(
            title = stringResource(R.string.dynamic_detail),
            showBackIcon = true,
            onBackClick = onNavigateBack
        )
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is DynamicDetailUiState.Loading -> {
                    LoadingContent()
                }
                is DynamicDetailUiState.Success -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when (page) {
                                0 -> Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollState)
                                        .padding(horizontal = 8.dp)
                                        .padding(paddingValues)
                                ) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    DynamicCard(
                                        dynamic = state.dynamic,
                                        onClick = {},
                                        onUserClick = onUserClick,
                                        onVideoClick = onVideoClick,
                                        onImageClick = onImageClick,
                                        onLikeClick = { dynamicId, isLiked ->
                                            viewModel.likeDynamic(dynamicId, isLiked)
                                        },
                                        onDynamicClick = onDynamicClick,
                                        showFullContent = true
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                1 -> CommentScreen(
                                    aid = state.dynamic.id.toLongOrNull() ?: 0L,
                                    type = 17,
                                    scrollState = commentScrollState,
                                    paddingValues = paddingValues,
                                    onCommentDetailClick = { replyId ->
                                        val oid = state.dynamic.id.toLongOrNull() ?: 0L
                                        navController.navigate("comment_detail/$replyId?oid=$oid&type=17")
                                    },
                                    onWriteReplyClick = { oid, rpid, parent, parentSender ->
                                        navController.navigate("write_reply/$oid/$rpid/$parent?parentSender=${parentSender ?: ""}")
                                    },
                                    onUserClick = onUserClick,
                                    onOpusClick = { opusId ->
                                        navController.navigate("opus_detail/$opusId")
                                    },
                                )
                            }
                        }

                        DotsIndicator(
                            dotCount = pagerState.pageCount,
                            dotSpacing = 8.dp,
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
                            pagerState = pagerState,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                        )
                    }
                }
                is DynamicDetailUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.loadDynamic(dynamicId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}
