package com.huanli233.bilizepam.ui.screens.player

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.viewinterop.AndroidView
import androidx.wear.compose.foundation.isRoundDevice
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.wear.compose.material3.PaddingDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.verticalContentPadding
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
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var isLongPressing by remember { mutableStateOf(false) }

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

    var hasAppliedHistoryProgress by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.videoUrl) {
        if (uiState.videoUrl.isNotEmpty()) {
            exoPlayer.setMediaItem(MediaItem.fromUri(uiState.videoUrl))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            hasAppliedHistoryProgress = false
        }
    }
    
    LaunchedEffect(exoPlayer, uiState.historyProgress) {
        if (!hasAppliedHistoryProgress && uiState.historyProgress > 5000) {
            while (exoPlayer.playbackState == Player.STATE_IDLE || exoPlayer.playbackState == Player.STATE_BUFFERING) {
                delay(100)
            }
            if (exoPlayer.playbackState == Player.STATE_READY) {
                exoPlayer.seekTo(uiState.historyProgress)
                hasAppliedHistoryProgress = true
                Log.d("PlayerScreen", "Seeked to history progress: ${uiState.historyProgress}ms")
            }
        }
    }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            viewModel.startProgressReporting { exoPlayer.currentPosition }
        } else {
            viewModel.stopProgressReporting()
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
            viewModel.reportFinalProgress(exoPlayer.currentPosition)
            exoPlayer.release()
        }
    }

    ScreenScaffold {
        Surface(
            modifier = Modifier.fillMaxSize()
                .padding(vertical = PaddingDefaults.verticalOptContentPadding()),
            color = MaterialTheme.colorScheme.surface
        ) {
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
                            },
                            onLongPress = {
                                isLongPressing = true
                                playbackSpeed = 2f
                                exoPlayer.setPlaybackSpeed(2f)
                            },
                            onPress = {
                                val pressed = tryAwaitRelease()
                                if (isLongPressing) {
                                    isLongPressing = false
                                    playbackSpeed = 1f
                                    exoPlayer.setPlaybackSpeed(1f)
                                }
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

            LaunchedEffect(isPlaying, isDanmakuPrepared) {
                if (isDanmakuPrepared && danmakuView != null) {
                    if (isPlaying) {
                        danmakuView?.resume()
                    } else {
                        danmakuView?.pause()
                    }
                }
            }

            LaunchedEffect(uiState.isDanmakuVisible, isDanmakuPrepared) {
                if (isDanmakuPrepared && danmakuView != null) {
                    if (uiState.isDanmakuVisible) {
                        danmakuView?.show()
                    } else {
                        danmakuView?.hide()
                    }
                }
            }

            LaunchedEffect(playbackSpeed, isDanmakuPrepared) {
                if (isDanmakuPrepared && danmakuView != null) {
                    danmakuView?.setSpeed(playbackSpeed)
                }
            }

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

            if (danmakuParser != null && danmakuError == null) {
                AndroidView(
                    factory = { ctx ->
                        DanmakuView(ctx).apply {
                            enableDanmakuDrawingCache(true)
                            setCallback(object : DrawHandler.Callback {
                                override fun prepared() {
                                    isDanmakuPrepared = true
                                    start()
                                    seekTo(currentPosition)
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
        }
    }

    PlayerControls(
        visible = showControls,
        isPlaying = isPlaying,
        currentPosition = currentPosition,
        duration = duration,
        title = uiState.title,
        playbackSpeed = playbackSpeed,
        isLongPressing = isLongPressing,
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
        isDanmakuVisible = uiState.isDanmakuVisible,
        onSpeedChange = { speed ->
            playbackSpeed = speed
            exoPlayer.setPlaybackSpeed(speed)
        }
    )

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
    playbackSpeed: Float,
    isLongPressing: Boolean,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onBackClick: () -> Unit,
    onDanmakuToggle: () -> Unit,
    isDanmakuVisible: Boolean,
    onSpeedChange: (Float) -> Unit
) {
    val isRound = isRoundDevice()
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isRound) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = PaddingDefaults.verticalOptContentPadding())
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Color.Black.copy(alpha = 0.6f)
                        )
                        .clickable { onBackClick() }
                        .padding(horizontal = 16.dp, vertical = 3.dp)
                        .padding(top = PaddingDefaults.verticalOptContentPadding()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
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

            if (isLongPressing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${playbackSpeed}x",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Color.Black.copy(alpha = 0.6f)
                    )
                    .padding(
                        horizontal = if (isRound) 12.dp else 16.dp,
                        vertical = if (isRound) 12.dp else 12.dp
                    )
            ) {
                var sliderPosition by remember { mutableFloatStateOf(0f) }
                var isSeeking by remember { mutableStateOf(false) }

                LaunchedEffect(currentPosition) {
                    if (!isSeeking) {
                        sliderPosition = currentPosition.toFloat()
                    }
                }

                if (isRound) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onPlayPauseClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                            var showSpeedMenu by remember { mutableStateOf(false) }
                            
                            Box {
                                IconButton(
                                    onClick = { showSpeedMenu = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text(
                                        text = if (playbackSpeed == 1f) "速" else "${playbackSpeed}x",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                androidx.compose.material3.DropdownMenu(
                                    expanded = showSpeedMenu,
                                    onDismissRequest = { showSpeedMenu = false }
                                ) {
                                    listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text("${speed}x") },
                                            onClick = {
                                                onSpeedChange(speed)
                                                showSpeedMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            IconButton(
                                onClick = onDanmakuToggle,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDanmakuVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = sliderPosition,
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
                } else {
                    Slider(
                        value = sliderPosition,
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

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(
                                    onClick = onPlayPauseClick,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                var showSpeedMenu by remember { mutableStateOf(false) }
                                
                                Box {
                                    IconButton(
                                        onClick = { showSpeedMenu = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text(
                                            text = if (playbackSpeed == 1f) "倍速" else "${playbackSpeed}x",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontSize = 10.sp
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showSpeedMenu,
                                        onDismissRequest = { showSpeedMenu = false }
                                    ) {
                                        listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                                            DropdownMenuItem(
                                                text = { Text("${speed}x") },
                                                onClick = {
                                                    onSpeedChange(speed)
                                                    showSpeedMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                                
                                IconButton(
                                    onClick = onDanmakuToggle,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDanmakuVisible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
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