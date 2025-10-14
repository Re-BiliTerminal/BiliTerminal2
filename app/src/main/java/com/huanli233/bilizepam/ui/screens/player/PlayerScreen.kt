package com.huanli233.bilizepam.ui.screens.player

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.huanli233.bilizepam.ui.dialog.AdaptDialog
import kotlinx.coroutines.delay
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView

private const val BILIBILI_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.5414.75 Safari/537.36"
private const val BILIBILI_REFERER = "https://www.bilibili.com"

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    aid: Long,
    cid: Long = 0,
    onNavigateBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val exoPlayer = remember {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(BILIBILI_USER_AGENT)
            .setDefaultRequestProperties(
                mapOf(
                    "Referer" to BILIBILI_REFERER
                )
            )

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var buffering by remember { mutableStateOf(false) }
    var showPageSelector by remember { mutableStateOf(false) }
    var danmakuParser by remember { mutableStateOf<BaseDanmakuParser?>(null) }
    var danmakuView by remember { mutableStateOf<DanmakuView?>(null) }
    var danmakuError by remember { mutableStateOf<String?>(null) }

    val danmakuContext = remember {
        DanmakuContext.create().apply {
            setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
            setScaleTextSize(1.2f)
            setDanmakuTransparency(0.8f)
            setCacheStuffer(SpannedCacheStuffer(), null)
            setMaximumVisibleSizeInScreen(100)
            setDuplicateMergingEnabled(true)
        }
    }

    LaunchedEffect(aid) {
        viewModel.loadVideoInfo(aid)
    }

    LaunchedEffect(aid, cid) {
        if (cid > 0) {
            viewModel.loadVideo(aid, cid)
        }
    }

    LaunchedEffect(uiState.videoUrl) {
        if (uiState.videoUrl.isNotEmpty()) {
            exoPlayer.setMediaItem(MediaItem.fromUri(uiState.videoUrl))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    LaunchedEffect(uiState.danmakuUrl) {
        if (uiState.danmakuUrl.isNotEmpty()) {
            Log.d("Danmaku", "Loading danmaku from: ${uiState.danmakuUrl}")
            danmakuError = null
            try {
                val parser = viewModel.createDanmakuParser(uiState.danmakuUrl)
                if (parser != null) {
                    danmakuParser = parser
                    Log.d("Danmaku", "Danmaku parser created successfully")
                } else {
                    danmakuError = "Failed to load danmaku"
                }
            } catch (e: Exception) {
                val errorMsg = "Error loading danmaku: ${e.message}"
                Log.e("Danmaku", errorMsg, e)
                danmakuError = errorMsg
            }
        }
    }

    LaunchedEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                buffering = state == Player.STATE_BUFFERING
            }
        }
        exoPlayer.addListener(listener)

        while (true) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(0L)
            delay(250)
        }
    }

    LaunchedEffect(showControls) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            showControls = !showControls
                        },
                        onDoubleTap = {
                            if (isPlaying) exoPlayer.pause()
                            else exoPlayer.play()
                        }
                    )
                }
        )

        AnimatedVisibility(
            visible = buffering,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                )
            }
        }

        var isDanmakuPrepared by remember { mutableStateOf(false) }
        
        // 控制弹幕的播放/暂停
        LaunchedEffect(isPlaying, isDanmakuPrepared) {
            if (isDanmakuPrepared && danmakuView != null) {
                if (isPlaying) {
                    danmakuView?.resume()
                } else {
                    danmakuView?.pause()
                }
            }
        }
        
        // 控制弹幕的显示/隐藏
        LaunchedEffect(uiState.isDanmakuVisible, isDanmakuPrepared) {
            if (isDanmakuPrepared && danmakuView != null) {
                if (uiState.isDanmakuVisible) {
                    danmakuView?.show()
                } else {
                    danmakuView?.hide()
                }
            }
        }
        
        // Show loading indicator while loading danmaku
        if (uiState.isLoadingDanmaku) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.width(8.dp))
                Text("Loading danmaku...")
            }
        }
        
        // Show error message if danmaku loading failed
        danmakuError?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Failed to load danmaku: $error",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        // Only show danmaku view if we have a parser and no errors
        if (danmakuParser != null && danmakuError == null) {
            AndroidView(
                factory = { ctx ->
                    DanmakuView(ctx).apply {
                        enableDanmakuDrawingCache(true)
                        setCallback(object : DrawHandler.Callback {
                            override fun prepared() {
                                isDanmakuPrepared = true
                                start()
                                // 同步到当前播放位置
                                seekTo(currentPosition)
                                // 根据播放状态决定是否暂停
                                if (!isPlaying) {
                                    pause()
                                }
                            }
                            override fun updateTimer(timer: DanmakuTimer) {}
                            override fun danmakuShown(danmaku: BaseDanmaku?) {}
                            override fun drawingFinished() {}
                        })
                        danmakuView = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    if (!isDanmakuPrepared && danmakuParser != null) {
                        try {
                            view.prepare(danmakuParser, danmakuContext)
                        } catch (e: Exception) {
                            Log.e("Danmaku", "Error preparing danmaku view", e)
                            danmakuError = "Error preparing danmaku: ${e.message}"
                        }
                    }
                }
            )
        }

        PlayerControls(
            visible = showControls,
            isPlaying = isPlaying,
            currentPosition = currentPosition,
            duration = duration,
            title = uiState.title,
            onPlayPauseClick = {
                if (isPlaying) exoPlayer.pause()
                else exoPlayer.play()
            },
            onSeek = { position ->
                exoPlayer.seekTo(position)
                danmakuParser?.let {
                    danmakuView?.seekTo(position)
                }
            },
            onBackClick = onNavigateBack,
            onDanmakuToggle = { viewModel.toggleDanmaku() },
            isDanmakuVisible = uiState.isDanmakuVisible
        )
    }

    if (showPageSelector && uiState.pages.isNotEmpty()) {
        PageSelectorDialog(
            pages = uiState.pages,
            currentPage = uiState.currentPage,
            onDismiss = { showPageSelector = false },
            onPageSelected = { page ->
                viewModel.loadVideo(aid, uiState.pages[page].cid)
                showPageSelector = false
            }
        )
    }

    LaunchedEffect(uiState.pages) {
        if (uiState.pages.isNotEmpty() && cid == 0L) {
            if (uiState.pages.size == 1) {
                viewModel.loadVideo(aid, uiState.pages[0].cid)
            } else {
                showPageSelector = true
            }
        }
    }
}

@Composable
fun PlayerControls(
    visible: Boolean,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    title: String,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onBackClick: () -> Unit,
    onDanmakuToggle: () -> Unit,
    isDanmakuVisible: Boolean
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Color.Black.copy(alpha = 0.6f)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Color.Black.copy(alpha = 0.6f)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                var sliderPosition by remember(currentPosition) {
                    mutableFloatStateOf(currentPosition.toFloat())
                }
                var isSeeking by remember { mutableStateOf(false) }

                Slider(
                    value = if (isSeeking) sliderPosition else currentPosition.toFloat(),
                    onValueChange = {
                        isSeeking = true
                        sliderPosition = it
                    },
                    onValueChangeFinished = {
                        onSeek(sliderPosition.toLong())
                        isSeeking = false
                    },
                    valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )

                    Row {
                        IconButton(onClick = onDanmakuToggle) {
                            Icon(
                                imageVector = if (isDanmakuVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }

                    Text(
                        text = formatTime(duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000).toInt()
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

@Composable
fun PageSelectorDialog(
    pages: List<VideoPage>,
    currentPage: Int,
    onDismiss: () -> Unit,
    onPageSelected: (Int) -> Unit
) {
    AdaptDialog(
        onDismissRequest = onDismiss,
        confirmButton = { },
        title = { Text("选择分P") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(pages) { index, page ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (index == currentPage) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        onClick = { onPageSelected(index) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "P${page.page} ${page.part}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (index == currentPage) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}