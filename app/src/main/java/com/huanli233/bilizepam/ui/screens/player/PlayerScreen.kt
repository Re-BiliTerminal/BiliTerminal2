package com.huanli233.bilizepam.ui.screens.player

import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.isRoundDevice
import androidx.wear.compose.material3.PaddingDefaults
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.bilizepam.data.setting.LocalData
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

@Composable
fun PlayerScreen(
    aid: Long,
    cid: Long = 0,
    onNavigateBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val settings by LocalData.settingsStateFlow.collectAsState()
    val playerSettings = settings?.playerSettings

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var buffering by remember { mutableStateOf(false) }
    var showPageSelector by remember { mutableStateOf(false) }
    var showQualitySelector by remember { mutableStateOf(false) }
    var showSpeedSelector by remember { mutableStateOf(false) }
    var danmakuParser by remember { mutableStateOf<BaseDanmakuParser?>(null) }
    var danmakuView by remember { mutableStateOf<DanmakuView?>(null) }
    var danmakuError by remember { mutableStateOf<String?>(null) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var isLongPressing by remember { mutableStateOf(false) }
    val videoAspectRatio = uiState.videoAspectRatio

    val scope = rememberCoroutineScope()

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

    LaunchedEffect(viewModel.ijkPlayer, uiState.historyProgress) {
        if (!hasAppliedHistoryProgress && uiState.historyProgress > 5000) {
            // 等待播放器准备就绪
            while (!viewModel.ijkPlayer.isPlaying && viewModel.ijkPlayer.duration <= 0) {
                delay(100)
            }
            if (viewModel.ijkPlayer.duration > 0) {
                viewModel.ijkPlayer.seekTo(uiState.historyProgress)
                hasAppliedHistoryProgress = true
                Log.d("PlayerScreen", "Seeked to history progress: ${uiState.historyProgress}ms")
            }
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            viewModel.startProgressReporting { viewModel.ijkPlayer.currentPosition }
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
                    Log.d("Danmaku", "Danmaku parser created successfully - parser: $danmakuParser")
                    // 不要在这里访问 danmakus，因为还没有设置 Context
                    Log.d("Danmaku", "Parser ready, will get danmaku count after Context is set")
                } else {
                    danmakuError = "Failed to load danmaku"
                    Log.e("Danmaku", "Parser is null!")
                }
            } catch (e: Exception) {
                val errorMsg = "Error loading danmaku: ${e.message}"
                Log.e("Danmaku", errorMsg, e)
                danmakuError = errorMsg
            }
        } else {
            Log.d("Danmaku", "Danmaku URL is empty - uiState.danmakuUrl: '${uiState.danmakuUrl}'")
        }
    }

    LaunchedEffect(viewModel.ijkPlayer) {
        while (true) {
            currentPosition = viewModel.ijkPlayer.currentPosition
            duration = viewModel.ijkPlayer.duration.coerceAtLeast(0L)
            
            // 同步播放状态 - 检测自动播放
            val actuallyPlaying = viewModel.ijkPlayer.isPlaying
            if (actuallyPlaying != isPlaying) {
                android.util.Log.d("PlayerScreen", "Syncing play state: $actuallyPlaying")
                isPlaying = actuallyPlaying
            }
            
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
            viewModel.reportFinalProgress(viewModel.ijkPlayer.currentPosition)
            // 播放器释放由 ViewModel 处理
        }
    }

    ScreenScaffold {
        Surface(
            modifier = Modifier.fillMaxSize()
                .padding(vertical = PaddingDefaults.verticalOptContentPadding()),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
            // 根据设置选择 TextureView 或 SurfaceView
            if (playerSettings?.useTextureView == true) {
                AndroidView(
                    factory = { ctx ->
                        Log.d("PlayerScreen", "Creating FrameLayout with TextureView + DanmakuView")
                        FrameLayout(ctx).apply {
                            val textureView = TextureView(ctx).apply {
                                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                                        android.util.Log.d("PlayerScreen", "TextureView surface available: ${width}x${height}")
                                        viewModel.ijkPlayer.setSurface(Surface(surface))

                                        if (viewModel.ijkPlayer.isPlayable && playerSettings?.autoPlay == true && !viewModel.ijkPlayer.isPlaying) {
                                            android.util.Log.d("PlayerScreen", "Auto-starting playback after TextureView ready")
                                            viewModel.ijkPlayer.start()
                                            isPlaying = true
                                        }
                                    }

                                    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                                        android.util.Log.d("PlayerScreen", "TextureView size changed: ${width}x${height}")
                                        viewModel.ijkPlayer.setSurface(Surface(surface))
                                    }

                                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                                        android.util.Log.d("PlayerScreen", "TextureView surface destroyed")
                                        return false
                                    }

                                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                                        // no-op
                                    }
                                }
                            }

                            val danmakuOverlay = DanmakuView(ctx).apply {
                                enableDanmakuDrawingCache(true)
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                danmakuView = this
                                setCallback(object : DrawHandler.Callback {
                                    override fun prepared() {
                                        start()
                                        seekTo(currentPosition)
                                        
                                        // 根据实际播放状态控制弹幕
                                        if (!viewModel.ijkPlayer.isPlaying) {
                                            pause()
                                        }
                                        
                                        // 显示弹幕
                                        if (uiState.isDanmakuVisible) {
                                            show()
                                        }
                                    }

                                    override fun updateTimer(timer: DanmakuTimer) {}
                                    override fun danmakuShown(danmaku: BaseDanmaku?) {}

                                    override fun drawingFinished() {}
                                })
                            }

                            addView(textureView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                            addView(danmakuOverlay, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                        }
                    },
                    update = { root ->
                        val danmakuOverlay = root.getChildAt(1) as DanmakuView
                        if (danmakuParser != null) {
                            try {
                                danmakuOverlay.prepare(danmakuParser, danmakuContext)
                            } catch (e: Exception) {
                                Log.e("Danmaku", "Error preparing danmaku view", e)
                                danmakuError = "Error preparing danmaku: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                        .aspectRatio(videoAspectRatio, matchHeightConstraintsFirst = false)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { showControls = !showControls },
                                onDoubleTap = {
                                    if (isPlaying) {
                                        viewModel.ijkPlayer.pause()
                                        isPlaying = false
                                    } else {
                                        viewModel.ijkPlayer.start()
                                        isPlaying = true
                                    }
                                },
                                onLongPress = {
                                    isLongPressing = true
                                    playbackSpeed = 2f
                                    viewModel.ijkPlayer.setSpeed(2f)
                                },
                                onPress = {
                                    val pressed = tryAwaitRelease()
                                    if (isLongPressing) {
                                        isLongPressing = false
                                        playbackSpeed = 1f
                                        viewModel.ijkPlayer.setSpeed(1f)
                                    }
                                }
                            )
                        }
                )
            } else {
                AndroidView(
                    factory = { ctx ->
                        android.util.Log.d("PlayerScreen", "Creating SurfaceView")
                        SurfaceView(ctx)
                    },
                    update = { surfaceView ->
                        surfaceView.holder.addCallback(object :
                            android.view.SurfaceHolder.Callback {
                            override fun surfaceCreated(holder: android.view.SurfaceHolder) {
                                android.util.Log.d("PlayerScreen", "SurfaceView created")
                                viewModel.ijkPlayer.setDisplay(holder)

                                // 检查播放器状态，如果已准备好且设置了自动播放，则开始播放
                                if (viewModel.ijkPlayer.isPlayable && playerSettings?.autoPlay == true && !viewModel.ijkPlayer.isPlaying) {
                                    android.util.Log.d(
                                        "PlayerScreen",
                                        "Auto-starting playback after SurfaceView ready"
                                    )
                                    viewModel.ijkPlayer.start()
                                    isPlaying = true
                                }
                            }

                            override fun surfaceChanged(
                                holder: android.view.SurfaceHolder,
                                format: Int,
                                width: Int,
                                height: Int
                            ) {
                                android.util.Log.d(
                                    "PlayerScreen",
                                    "SurfaceView changed: ${width}x${height}"
                                )
                                viewModel.ijkPlayer.setDisplay(holder)
                            }

                            override fun surfaceDestroyed(holder: android.view.SurfaceHolder) {
                                android.util.Log.d("PlayerScreen", "SurfaceView destroyed")
                            }
                        })
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                        .aspectRatio(videoAspectRatio, matchHeightConstraintsFirst = false)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    showControls = !showControls
                                },
                                onDoubleTap = {
                                    if (isPlaying) {
                                        viewModel.ijkPlayer.pause()
                                        isPlaying = false
                                    } else {
                                        viewModel.ijkPlayer.start()
                                        isPlaying = true
                                    }
                                },
                                onLongPress = {
                                    isLongPressing = true
                                    playbackSpeed = 2f
                                    viewModel.ijkPlayer.setSpeed(2f)
                                },
                                onPress = {
                                    val pressed = tryAwaitRelease()
                                    if (isLongPressing) {
                                        isLongPressing = false
                                        playbackSpeed = 1f
                                        viewModel.ijkPlayer.setSpeed(1f)
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
                    android.util.Log.d("Danmaku", "Visibility changed - isDanmakuVisible: ${uiState.isDanmakuVisible}, isDanmakuPrepared: $isDanmakuPrepared")
                    if (isDanmakuPrepared && danmakuView != null) {
                        if (uiState.isDanmakuVisible) {
                            danmakuView?.show()
                            android.util.Log.d("Danmaku", "DanmakuView.show() called")
                        } else {
                            danmakuView?.hide()
                            android.util.Log.d("Danmaku", "DanmakuView.hide() called")
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

                // SurfaceView 模式下的弹幕覆盖层
                val shouldShowDanmaku = danmakuParser != null && playerSettings?.useTextureView != true && danmakuError == null
                if (shouldShowDanmaku) {
                    AndroidView(
                        factory = { ctx ->
                            android.util.Log.d("Danmaku", "Creating DanmakuView - TextureView mode: ${playerSettings?.useTextureView}")
                            DanmakuView(ctx).apply {
                                enableDanmakuDrawingCache(true)
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                                bringToFront()
                                
                                // 添加调试信息
                                android.util.Log.d("Danmaku", "DanmakuView created - Width: $width, Height: $height")
                                android.util.Log.d("Danmaku", "DanmakuView visibility: $visibility")
                                android.util.Log.d("Danmaku", "DanmakuView elevation: $elevation")
                                
                                setCallback(object : DrawHandler.Callback {
                                    override fun prepared() {
                                        isDanmakuPrepared = true
                                        android.util.Log.d("Danmaku", "DanmakuView prepared - isShown: $isShown, visibility: $visibility")
                                        android.util.Log.d("Danmaku", "DanmakuView bounds: left=$left, top=$top, right=$right, bottom=$bottom")
                                        
                                        // 现在可以安全地获取弹幕数量了
                                        try {
                                            val danmakuCount = danmakuParser?.danmakus?.size() ?: 0
                                            android.util.Log.d("Danmaku", "Total danmaku count: $danmakuCount")
                                        } catch (e: Exception) {
                                            android.util.Log.e("Danmaku", "Error getting danmaku count: ${e.message}")
                                        }
                                        
                                        start()
                                        seekTo(currentPosition)
                                        if (!isPlaying) {
                                            pause()
                                        }
                                        android.util.Log.d("Danmaku", "DanmakuView started - isPlaying: $isPlaying")
                                    }

                                    override fun updateTimer(timer: DanmakuTimer) {
                                        // 添加定时器调试
                                        if (timer.currMillisecond % 5000 < 50) { // 每5秒打印一次
                                            android.util.Log.d("Danmaku", "Timer update: ${timer.currMillisecond}ms")
                                        }
                                    }
                                    
                                    override fun danmakuShown(danmaku: BaseDanmaku?) {
                                        android.util.Log.d("Danmaku", "Danmaku shown: ${danmaku?.text}")
                                    }
                                    
                                    override fun drawingFinished() {
                                        android.util.Log.v("Danmaku", "Drawing finished")
                                    }
                                })
                                danmakuView = this
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red.copy(alpha = 0.1f)), // 临时添加半透明红色背景用于调试
                        update = { view ->
                            android.util.Log.d("Danmaku", "DanmakuView update called - prepared: $isDanmakuPrepared")
                            android.util.Log.d("Danmaku", "DanmakuView size in update: ${view.width}x${view.height}")
                            android.util.Log.d("Danmaku", "DanmakuView visibility in update: ${view.visibility}")
                            
                            if (!isDanmakuPrepared && danmakuParser != null) {
                                try {
                                    view.prepare(danmakuParser, danmakuContext)
                                    android.util.Log.d("Danmaku", "DanmakuView prepare called successfully")
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
                android.util.Log.d(
                    "PlayerScreen",
                    "Play/Pause clicked, current isPlaying: $isPlaying"
                )
                android.util.Log.d(
                    "PlayerScreen",
                    "Player state - isPlayable: ${viewModel.ijkPlayer.isPlayable}, duration: ${viewModel.ijkPlayer.duration}"
                )

                if (isPlaying) {
                    val result = viewModel.ijkPlayer.pause()
                    isPlaying = false
                    android.util.Log.d("PlayerScreen", "Player paused, result: $result")
                } else {
                    val result = viewModel.ijkPlayer.start()
                    isPlaying = true
                    android.util.Log.d("PlayerScreen", "Player started, result: $result")
                }
            },
            onSeek = { position ->
                viewModel.ijkPlayer.seekTo(position)
                danmakuParser?.let {
                    danmakuView?.seekTo(position)
                }
            },
            onBackClick = onNavigateBack,
            onDanmakuToggle = { viewModel.toggleDanmaku() },
            isDanmakuVisible = uiState.isDanmakuVisible,
            onSpeedChange = { speed ->
                playbackSpeed = speed
                viewModel.ijkPlayer.setSpeed(speed)
            },
            onSpeedClick = { showSpeedSelector = true },
            onQualityClick = { showQualitySelector = true }
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

        if (showQualitySelector) {
            QualitySelectionDialog(
                currentQuality = playerSettings?.defaultQuality ?: 64,
                availableQualities = uiState.availableQualities,
                onDismiss = { showQualitySelector = false },
                onQualitySelected = { quality ->
                    viewModel.changeQuality(quality)
                    showQualitySelector = false
                }
            )
        }

        if (showSpeedSelector) {
            SpeedSelectionDialog(
                currentSpeed = playbackSpeed,
                onDismiss = { showSpeedSelector = false },
                onSpeedSelected = { speed ->
                    playbackSpeed = speed
                    viewModel.ijkPlayer.setSpeed(speed)
                    showSpeedSelector = false
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
    onSpeedChange: (Float) -> Unit,
    onSpeedClick: () -> Unit,
    onQualityClick: () -> Unit
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
                            IconButton(
                                onClick = onSpeedClick,
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
                            
                            IconButton(
                                onClick = onQualityClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text(
                                    text = "清",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
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

                                IconButton(
                                    onClick = onSpeedClick,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text(
                                        text = if (playbackSpeed == 1f) "倍速" else "${playbackSpeed}x",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
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
fun SpeedSelectionDialog(
    currentSpeed: Float,
    onDismiss: () -> Unit,
    onSpeedSelected: (Float) -> Unit
) {
    val speedOptions = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
    
    AdaptDialog(
        onDismissRequest = onDismiss,
        confirmButton = { close ->
            // 不需要确认按钮，点击选项即可
        },
        title = { Text("播放速度") },
        text = {
            Column {
                speedOptions.forEach { speed ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                onSpeedSelected(speed)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSpeed == speed,
                            onClick = { 
                                onSpeedSelected(speed)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${speed}x",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    )
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

@Composable
fun QualitySelectionDialog(
    currentQuality: Int,
    availableQualities: List<VideoQuality>,
    onDismiss: () -> Unit,
    onQualitySelected: (Int) -> Unit
) {
    AdaptDialog(
        onDismissRequest = onDismiss,
        confirmButton = { },
        title = { Text("选择清晰度") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                availableQualities.forEach { quality ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onQualitySelected(quality.qn) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = quality.qn == currentQuality,
                            onClick = { onQualitySelected(quality.qn) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = quality.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    )
}