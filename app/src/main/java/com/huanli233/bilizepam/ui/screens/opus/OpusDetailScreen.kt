package com.huanli233.bilizepam.ui.screens.opus

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.screens.comment.CommentScreen
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
import com.huanli233.bilizepam.ui.viewmodel.OpusDetailUiState
import com.huanli233.bilizepam.ui.viewmodel.OpusDetailViewModel
import com.huanli233.bilizepam.utils.MsgUtil
import com.huanli233.bilizepam.utils.extensions.formatNumber
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.WormIndicatorType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OpusDetailScreen(
    opusId: String,
    onNavigateBack: () -> Unit = {},
    onUserClick: (Long) -> Unit = {},
    onVideoClick: (String) -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onCommentDetailClick: (Long, Long) -> Unit = { _, _ -> },
    onWriteReplyClick: (Long, Long, Long, String?) -> Unit = { _, _, _, _ -> },
    viewModel: OpusDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 2 })
    
    val opusDetailScrollState = rememberScrollState()
    val commentScrollState = rememberScalingLazyListState()
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    LaunchedEffect(opusId) {
        viewModel.loadOpus(opusId)
    }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is com.huanli233.bilizepam.ui.viewmodel.OpusDetailEvent.LikeSuccess -> {
                    val message = if (event.isLiked) 
                        context.getString(R.string.msg_like_success)
                    else 
                        context.getString(R.string.msg_cancel_success)
                    MsgUtil.showMsg(message)
                }
                is com.huanli233.bilizepam.ui.viewmodel.OpusDetailEvent.FavoriteSuccess -> {
                    val message = if (event.isFavorited) 
                        context.getString(R.string.msg_favorite_success)
                    else 
                        context.getString(R.string.msg_cancel_success)
                    MsgUtil.showMsg(message)
                }
                is com.huanli233.bilizepam.ui.viewmodel.OpusDetailEvent.NotLoggedIn -> {
                    MsgUtil.showMsg(context.getString(R.string.msg_not_logged_in))
                }
                is com.huanli233.bilizepam.ui.viewmodel.OpusDetailEvent.OperationFailed -> {
                    MsgUtil.showMsg(event.message ?: context.getString(R.string.msg_operation_failed))
                }
            }
        }
    }

    ScreenScaffold(
        scrollState = commentScrollState,
        modifier = Modifier.fillMaxSize(),
        topBar = scrollAwareTopBar(
            title = stringResource(R.string.opus_detail),
            showBackIcon = true,
            onBackClick = onNavigateBack,
            scrollBehavior = scrollBehavior
        ),
        topBarScrollBehavior = scrollBehavior
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is OpusDetailUiState.Loading -> {
                        LoadingView(
                            state = LoadingState.LOADING,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                    is OpusDetailUiState.Error -> {
                        LoadingView(
                            state = LoadingState.ERROR,
                            errorMessage = state.message,
                            onRetry = { viewModel.loadOpus(opusId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                    is OpusDetailUiState.Success -> {
                        Box(modifier = Modifier.weight(1f)) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                when (page) {
                                    0 -> OpusDetailContent(
                                        opus = state.opus,
                                        scrollState = opusDetailScrollState,
                                        padding = paddingValues,
                                        onUserClick = onUserClick,
                                        onVideoClick = onVideoClick,
                                        onImageClick = onImageClick,
                                        onLikeClick = { viewModel.like() },
                                        onFavoriteClick = { viewModel.favorite() },
                                        onShareClick = {
                                        }
                                    )
                                    1 -> CommentScreen(
                                        aid = state.opus.basic.commentIdStr.toLongOrNull() ?: 0L,
                                        type = 11,
                                        onCommentDetailClick = { replyId ->
                                            val oid = state.opus.basic.commentIdStr.toLongOrNull() ?: 0L
                                            onCommentDetailClick(replyId, oid)
                                        },
                                        onWriteReplyClick = onWriteReplyClick,
                                        onUserClick = onUserClick
                                    )
                                }
                            }
                        }
                        
                        DotsIndicator(
                            dotCount = 2,
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
                                .run {
                                    with (this@Box) {
                                        align(Alignment.BottomCenter)
                                    }
                                }
                                .padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
