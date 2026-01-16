package com.huanli233.bilizepam.ui.screens.player

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Build
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Download
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
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.isRoundDevice
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.onSizeChanged
import androidx.wear.compose.foundation.hierarchicalFocusGroup
import androidx.wear.compose.foundation.requestFocusOnHierarchyActive
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.PaddingDefaults
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.dialog.AdaptDialog
import com.huanli233.bilizepam.utils.MsgUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

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
    var isVideoReady by remember { mutableStateOf(false) }
    var showPageSelector by remember { mutableStateOf(false) }
    var showQualitySelector by remember { mutableStateOf(false) }
    var showSpeedSelector by remember { mutableStateOf(false) }
    var danmakuParser by remember { mutableStateOf<BaseDanmakuParser?>(null) }
    var danmakuView by remember { mutableStateOf<DanmakuView?>(null) }
    var danmakuError by remember { mutableStateOf<String?>(null) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    
    val videoStateCache = remember { mutableMapOf<String, Pair<Boolean, Float>>() }
    var isLongPressing by remember { mutableStateOf(false) }
    val videoAspectRatio = uiState.videoAspectRatio

    val viewConfig = LocalViewConfiguration.current
    val touchSlopPx = viewConfig.touchSlop
    val doubleTapTimeoutMillis = viewConfig.doubleTapTimeoutMillis
    val longPressTimeoutMillis = viewConfig.longPressTimeoutMillis

    val enableOneFingerZoom = playerSettings?.enableOneFingerZoom == true

    var videoScale by remember { mutableFloatStateOf(1f) }
    var videoOffset by remember { mutableStateOf(Offset.Zero) }
    var videoContainerSizePx by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    var pendingDownload by remember { mutableStateOf(false) }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val shouldRun = pendingDownload
        pendingDownload = false
        if (granted && shouldRun) {
            viewModel.enqueueDownload(aid = uiState.aid, cid = uiState.cid, title = uiState.title)
        } else if (!granted) {
            MsgUtil.showMsg(context.getString(R.string.msg_storage_permission_denied))
        }
    }

    fun canWriteToPublicDownloads(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    val scope = rememberCoroutineScope()

    val danmakuContext = remember(playerSettings) {
        val defaultFontSize = 16f
        val fontSize = playerSettings?.danmakuFontSize ?: defaultFontSize
        val scaleFactor = fontSize / defaultFontSize
        
        DanmakuContext.create().apply {
            setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
            setScaleTextSize(scaleFactor)
            setDanmakuTransparency(0.8f)
            setCacheStuffer(SpannedCacheStuffer(), null)
            setMaximumVisibleSizeInScreen(playerSettings?.danmakuMaxCount ?: 50)
            setDuplicateMergingEnabled(true)
            
            setR2LDanmakuVisibility(playerSettings?.danmakuScrollEnabled ?: true)
            setFTDanmakuVisibility(playerSettings?.danmakuTopEnabled ?: true)
            setFBDanmakuVisibility(playerSettings?.danmakuBottomEnabled ?: true)
            setSpecialDanmakuVisibility(playerSettings?.danmakuAdvancedEnabled ?: true)
        }
    }

    LaunchedEffect(aid) {
        viewModel.loadVideoInfo(aid)
    }

    LaunchedEffect(aid, cid) {
        if (cid > 0) {
            viewModel.loadVideo(aid, cid)
            
            val videoKey = "${aid}_${cid}"
            val playerSettings = settings?.playerSettings
            
            if (playerSettings?.rememberDanmakuEnabled == true || playerSettings?.rememberSpeed == true) {
                val cachedState = videoStateCache[videoKey]
                if (cachedState != null) {
                    if (playerSettings.rememberDanmakuEnabled) {
                        viewModel.setDanmakuVisible(cachedState.first)
                    }
                    if (playerSettings.rememberSpeed) {
                        playbackSpeed = cachedState.second
                        viewModel.ijkPlayer.setSpeed(playbackSpeed)
                    }
                } else {
                    val defaultDanmaku = playerSettings?.defaultDanmakuEnabled ?: true
                    val defaultSpeed = playerSettings?.defaultSpeed ?: 1.0f
                    
                    if (playerSettings.rememberDanmakuEnabled) {
                        viewModel.setDanmakuVisible(defaultDanmaku)
                    } else {
                        viewModel.setDanmakuVisible(defaultDanmaku)
                    }
                    
                    if (playerSettings.rememberSpeed) {
                        playbackSpeed = defaultSpeed
                        viewModel.ijkPlayer.setSpeed(playbackSpeed)
                    } else {
                        playbackSpeed = defaultSpeed
                        viewModel.ijkPlayer.setSpeed(playbackSpeed)
                    }
                    
                    videoStateCache[videoKey] = Pair(defaultDanmaku, defaultSpeed)
                }
            } else {
                viewModel.setDanmakuVisible(playerSettings?.defaultDanmakuEnabled ?: true)
                playbackSpeed = playerSettings?.defaultSpeed ?: 1.0f
                viewModel.ijkPlayer.setSpeed(playbackSpeed)
            }
        }
    }

    var hasAppliedHistoryProgress by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.ijkPlayer, uiState.historyProgress) {
        if (!hasAppliedHistoryProgress && uiState.historyProgress > 5000) {
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

    // 监听视频准备状态
    LaunchedEffect(uiState.videoUrl) {
        if (uiState.videoUrl.isNotEmpty()) {
            isVideoReady = true
        } else {
            isVideoReady = false
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

            val actuallyPlaying = viewModel.ijkPlayer.isPlaying
            if (actuallyPlaying != isPlaying) {
                android.util.Log.d("PlayerScreen", "Syncing play state: $actuallyPlaying")
                isPlaying = actuallyPlaying
            }

            delay(250)
        }
    }

    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.reportFinalProgress(viewModel.ijkPlayer.currentPosition)
        }
    }

    ScreenScaffold {
        Surface(
            modifier = Modifier.fillMaxSize()
                .padding(vertical = PaddingDefaults.verticalOptContentPadding())
                .hierarchicalFocusGroup(true),
            color = MaterialTheme.colorScheme.surface
        ) {
            val focusRequester = remember { FocusRequester() }
            val scrollableState = rememberScrollableState { delta ->
                // 表冠滚动时调整进度，delta为负值表示向下滚动（快进），正值表示向上滚动（快退）
                val seekDelta = (-delta * 500).toLong() // 调整灵敏度，负号反转方向
                val newPosition = (currentPosition + seekDelta).coerceIn(0L, duration)
                if (newPosition != currentPosition) {
                    viewModel.ijkPlayer.seekTo(newPosition)
                }
                delta
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .requestFocusOnHierarchyActive()
                    .rotaryScrollable(
                        behavior = RotaryScrollableDefaults.behavior(
                            scrollableState = scrollableState,
                            flingBehavior = null // 禁用惯性滚动，视频进度控制不需要
                        ),
                        focusRequester = focusRequester
                    )
            ) {
                fun clampVideoOffset(offset: Offset, scale: Float): Offset {
                    if (scale <= 1f) return Offset.Zero
                    val w = videoContainerSizePx.width.toFloat()
                    val h = videoContainerSizePx.height.toFloat()
                    if (w <= 0f || h <= 0f) return Offset.Zero

                    val maxX = (w * (scale - 1f)) / 2f
                    val maxY = (h * (scale - 1f)) / 2f
                    return Offset(
                        x = offset.x.coerceIn(-maxX, maxX),
                        y = offset.y.coerceIn(-maxY, maxY)
                    )
                }

                val videoMeasureModifier = Modifier.onSizeChanged { size ->
                    videoContainerSizePx = size
                    videoOffset = clampVideoOffset(videoOffset, videoScale)
                }

                val videoGestureModifier = Modifier.pointerInput(
                    enableOneFingerZoom,
                    videoContainerSizePx,
                    touchSlopPx,
                    doubleTapTimeoutMillis,
                    longPressTimeoutMillis
                ) {
                    awaitEachGesture {
                        fun togglePlayPause() {
                            if (isPlaying) {
                                viewModel.ijkPlayer.pause()
                                isPlaying = false
                            } else {
                                viewModel.ijkPlayer.start()
                                isPlaying = true
                            }
                        }

                        fun applyTwoFingerTransform(c1: PointerInputChange, c2: PointerInputChange) {
                            val prevP1 = c1.previousPosition
                            val prevP2 = c2.previousPosition
                            val curP1 = c1.position
                            val curP2 = c2.position

                            val prevCentroid = (prevP1 + prevP2) / 2f
                            val curCentroid = (curP1 + curP2) / 2f
                            val pan = curCentroid - prevCentroid

                            val prevDiff = prevP1 - prevP2
                            val curDiff = curP1 - curP2
                            val prevDist = sqrt(prevDiff.x * prevDiff.x + prevDiff.y * prevDiff.y)
                            val curDist = sqrt(curDiff.x * curDiff.x + curDiff.y * curDiff.y)
                            val zoom = if (prevDist > 0f) curDist / prevDist else 1f

                            val newScale = (videoScale * zoom).coerceIn(1f, 3f)
                            val newOffset = if (newScale <= 1f) {
                                Offset.Zero
                            } else {
                                clampVideoOffset(videoOffset + pan, newScale)
                            }

                            videoScale = newScale
                            videoOffset = newOffset
                        }

                        val firstDown = awaitFirstDown(requireUnconsumed = false)
                        val firstPointerId = firstDown.id
                        val firstDownPosition = firstDown.position

                        var didLongPress = false
                        var didPan = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val pressed = event.changes.filter { it.pressed }
                            if (pressed.size >= 2) {
                                val p1 = pressed[0]
                                val p2 = pressed[1]
                                while (true) {
                                    val e = awaitPointerEvent()
                                    val p = e.changes.filter { it.pressed }
                                    if (p.size < 2) break
                                    val c1 = p[0]
                                    val c2 = p[1]
                                    applyTwoFingerTransform(c1, c2)
                                    c1.consume()
                                    c2.consume()
                                }
                                return@awaitEachGesture
                            }

                            val change = event.changes.firstOrNull { it.id == firstPointerId } ?: break
                            if (!change.pressed) break

                            val totalDelta = change.position - firstDownPosition
                            val movedDistance = sqrt(totalDelta.x * totalDelta.x + totalDelta.y * totalDelta.y)

                            if (!didLongPress && !didPan && movedDistance < touchSlopPx) {
                                val elapsed = change.uptimeMillis - firstDown.uptimeMillis
                                if (elapsed >= longPressTimeoutMillis) {
                                    didLongPress = true
                                    isLongPressing = true
                                    playbackSpeed = 2f
                                    viewModel.ijkPlayer.setSpeed(2f)
                                }
                            }

                            if (didLongPress) {
                                change.consume()
                                continue
                            }

                            if (!didPan && videoScale > 1f && movedDistance >= touchSlopPx) {
                                didPan = true
                            }

                            if (didPan) {
                                val delta = change.position - change.previousPosition
                                val newOffset = clampVideoOffset(videoOffset + delta, videoScale)
                                if (newOffset != videoOffset) {
                                    videoOffset = newOffset
                                }
                                change.consume()
                                continue
                            }
                        }

                        if (didLongPress) {
                            isLongPressing = false
                            playbackSpeed = 1f
                            viewModel.ijkPlayer.setSpeed(1f)
                            return@awaitEachGesture
                        }

                        if (didPan) {
                            return@awaitEachGesture
                        }

                        val secondDown = withTimeoutOrNull(doubleTapTimeoutMillis.toLong()) {
                            awaitFirstDown(requireUnconsumed = false)
                        }

                        if (secondDown == null) {
                            showControls = !showControls
                            return@awaitEachGesture
                        }

                        if (!enableOneFingerZoom) {
                            togglePlayPause()
                            return@awaitEachGesture
                        }

                        val secondPointerId = secondDown.id
                        val secondDownPosition = secondDown.position
                        val zoomStartSlopPx = touchSlopPx * 0.5f
                        var hasDragged = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val pressed = event.changes.filter { it.pressed }
                            if (pressed.size >= 2) {
                                val p1 = pressed[0]
                                val p2 = pressed[1]
                                while (true) {
                                    val e = awaitPointerEvent()
                                    val p = e.changes.filter { it.pressed }
                                    if (p.size < 2) break
                                    val c1 = p[0]
                                    val c2 = p[1]
                                    applyTwoFingerTransform(c1, c2)
                                    c1.consume()
                                    c2.consume()
                                }
                                return@awaitEachGesture
                            }

                            val change = event.changes.firstOrNull { it.id == secondPointerId } ?: break
                            if (!change.pressed) break

                            val delta = change.position - change.previousPosition
                            if (!hasDragged) {
                                val totalDelta = change.position - secondDownPosition
                                val totalDistance = sqrt(totalDelta.x * totalDelta.x + totalDelta.y * totalDelta.y)
                                if (totalDistance < zoomStartSlopPx) {
                                    change.consume()
                                    continue
                                }
                            }

                            hasDragged = true
                            val sensitivity = 0.005f
                            val scaleFactor = (1f - delta.y * sensitivity)
                            val newScale = (videoScale * scaleFactor).coerceIn(1f, 3f)
                            videoScale = newScale
                            if (newScale <= 1f) {
                                videoOffset = Offset.Zero
                            } else {
                                videoOffset = clampVideoOffset(videoOffset, newScale)
                            }
                            change.consume()
                        }

                        if (!hasDragged) {
                            togglePlayPause()
                        }
                    }
                }

                val videoContainerModifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
                    .aspectRatio(videoAspectRatio, matchHeightConstraintsFirst = false)
                    .then(videoMeasureModifier)
                    .graphicsLayer {
                        scaleX = videoScale
                        scaleY = videoScale
                        translationX = videoOffset.x
                        translationY = videoOffset.y
                    }

                if (playerSettings?.useTextureView == true) {
                    Box(modifier = videoContainerModifier) {
                        AndroidView(
                            factory = { ctx ->
                                Log.d(
                                    "PlayerScreen",
                                    "Creating FrameLayout with TextureView + DanmakuView + LoadingOverlay"
                                )
                                FrameLayout(ctx).apply {
                                    val textureView = TextureView(ctx).apply {
                                        surfaceTextureListener =
                                            object : TextureView.SurfaceTextureListener {
                                                override fun onSurfaceTextureAvailable(
                                                    surface: SurfaceTexture,
                                                    width: Int,
                                                    height: Int
                                                ) {
                                                    android.util.Log.d(
                                                        "PlayerScreen",
                                                        "TextureView surface available: ${width}x${height}"
                                                    )
                                                    viewModel.ijkPlayer.setSurface(Surface(surface))
                                                }

                                                override fun onSurfaceTextureSizeChanged(
                                                    surface: SurfaceTexture,
                                                    width: Int,
                                                    height: Int
                                                ) {
                                                    android.util.Log.d(
                                                        "PlayerScreen",
                                                        "TextureView size changed: ${width}x${height}"
                                                    )
                                                    viewModel.ijkPlayer.setSurface(Surface(surface))
                                                }

                                                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                                                    android.util.Log.d(
                                                        "PlayerScreen",
                                                        "TextureView surface destroyed"
                                                    )
                                                    return false
                                                }

                                                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
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

                                                if (!viewModel.ijkPlayer.isPlaying) {
                                                    pause()
                                                }

                                                if (uiState.isDanmakuVisible) {
                                                    show()
                                                }
                                            }

                                            override fun updateTimer(timer: DanmakuTimer) {}
                                            override fun danmakuShown(danmaku: BaseDanmaku?) {}
                                            override fun drawingFinished() {}
                                        })
                                    }

                                    // 创建LoadingOverlay - 使用ComposeView确保正确的层级
                                    val loadingOverlay =
                                        androidx.compose.ui.platform.ComposeView(ctx).apply {
                                            setContent {
                                                val currentUiState by viewModel.uiState.collectAsState()
                                                val shouldShowVideoLoading =
                                                    currentUiState.isLoading || (!isVideoReady && currentUiState.videoUrl.isEmpty())

                                                if (shouldShowVideoLoading) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(Color.Black.copy(alpha = 0.9f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.Center
                                                        ) {
                                                            CircularProgressIndicator(
                                                                color = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(48.dp),
                                                                strokeWidth = 4.dp
                                                            )
                                                            Spacer(modifier = Modifier.height(16.dp))
                                                            Text(
                                                                text = if (currentUiState.isLoading) "Loading video..." else "Preparing player...",
                                                                color = Color.White,
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Text(
                                                                text = "TextureView mode",
                                                                color = Color.Gray,
                                                                style = MaterialTheme.typography.bodySmall
                                                            )
                                                        }
                                                    }
                                                }

                                                // 错误状态显示
                                                currentUiState.error?.let { error ->
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(Color.Black.copy(alpha = 0.9f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.Center
                                                        ) {
                                                            Text(
                                                                text = "Failed to load video",
                                                                color = Color.White,
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Text(
                                                                text = error,
                                                                color = Color.Red,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                textAlign = TextAlign.Center
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    // 添加视图到FrameLayout，确保正确的层级顺序
                                    addView(
                                        textureView,
                                        FrameLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                    )
                                    addView(
                                        danmakuOverlay,
                                        FrameLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                    )
                                    addView(
                                        loadingOverlay,
                                        FrameLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                    )
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
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .then(videoGestureModifier)
                        )
                    }
                } else {
                    Box(modifier = videoContainerModifier) {
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

                                        // 自动播放逻辑已在PlayerViewModel的onPrepared中处理
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
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .then(videoGestureModifier)
                        )
                    }
                }
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
                    android.util.Log.d(
                        "Danmaku",
                        "Visibility changed - isDanmakuVisible: ${uiState.isDanmakuVisible}, isDanmakuPrepared: $isDanmakuPrepared"
                    )
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

                LaunchedEffect(danmakuContext, isDanmakuPrepared, danmakuParser) {
                    if (isDanmakuPrepared && danmakuView != null && danmakuParser != null) {
                        try {
                            danmakuView?.prepare(danmakuParser, danmakuContext)
                        } catch (e: Exception) {
                            Log.e("Danmaku", "Error updating danmaku context", e)
                        }
                    }
                }

                // 视频加载动画 - 仅在SurfaceView模式下显示（TextureView模式在AndroidView内部处理）
                if (playerSettings?.useTextureView != true) {
                    val shouldShowVideoLoading =
                        uiState.isLoading || (!isVideoReady && uiState.videoUrl.isEmpty())
                    if (shouldShowVideoLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp),
                                    strokeWidth = 4.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (uiState.isLoading) "Loading video..." else "Preparing player...",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "SurfaceView mode",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // 弹幕加载动画
                if (uiState.isLoadingDanmaku && !uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Loading danmaku...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // 视频错误显示 - 仅在SurfaceView模式下显示（TextureView模式在AndroidView内部处理）
                if (playerSettings?.useTextureView != true) {
                    uiState.error?.let { error ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Failed to load video",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = error,
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // 弹幕错误显示
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

                val shouldShowDanmaku =
                    danmakuParser != null && playerSettings?.useTextureView != true && danmakuError == null
                if (shouldShowDanmaku) {
                    AndroidView(
                        factory = { ctx ->
                            android.util.Log.d(
                                "Danmaku",
                                "Creating DanmakuView - TextureView mode: ${playerSettings?.useTextureView}"
                            )
                            DanmakuView(ctx).apply {
                                enableDanmakuDrawingCache(true)
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                                bringToFront()

                                android.util.Log.d(
                                    "Danmaku",
                                    "DanmakuView created - Width: $width, Height: $height"
                                )
                                android.util.Log.d("Danmaku", "DanmakuView visibility: $visibility")
                                android.util.Log.d("Danmaku", "DanmakuView elevation: $elevation")

                                setCallback(object : DrawHandler.Callback {
                                    override fun prepared() {
                                        isDanmakuPrepared = true
                                        android.util.Log.d(
                                            "Danmaku",
                                            "DanmakuView prepared - isShown: $isShown, visibility: $visibility"
                                        )
                                        android.util.Log.d(
                                            "Danmaku",
                                            "DanmakuView bounds: left=$left, top=$top, right=$right, bottom=$bottom"
                                        )

                                        try {
                                            val danmakuCount = danmakuParser?.danmakus?.size() ?: 0
                                            android.util.Log.d(
                                                "Danmaku",
                                                "Total danmaku count: $danmakuCount"
                                            )
                                        } catch (e: Exception) {
                                            android.util.Log.e(
                                                "Danmaku",
                                                "Error getting danmaku count: ${e.message}"
                                            )
                                        }

                                        start()
                                        seekTo(currentPosition)
                                        if (!isPlaying) {
                                            pause()
                                        }
                                        android.util.Log.d(
                                            "Danmaku",
                                            "DanmakuView started - isPlaying: $isPlaying"
                                        )
                                    }

                                    override fun updateTimer(timer: DanmakuTimer) {
                                        if (timer.currMillisecond % 5000 < 50) {
                                            android.util.Log.d(
                                                "Danmaku",
                                                "Timer update: ${timer.currMillisecond}ms"
                                            )
                                        }
                                    }

                                    override fun danmakuShown(danmaku: BaseDanmaku?) {
                                        android.util.Log.d(
                                            "Danmaku",
                                            "Danmaku shown: ${danmaku?.text}"
                                        )
                                    }

                                    override fun drawingFinished() {
                                        android.util.Log.v("Danmaku", "Drawing finished")
                                    }
                                })
                                danmakuView = this
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize(),
                        update = { view ->
                            android.util.Log.d(
                                "Danmaku",
                                "DanmakuView update called - prepared: $isDanmakuPrepared"
                            )
                            android.util.Log.d(
                                "Danmaku",
                                "DanmakuView size in update: ${view.width}x${view.height}"
                            )
                            android.util.Log.d(
                                "Danmaku",
                                "DanmakuView visibility in update: ${view.visibility}"
                            )

                            if (!isDanmakuPrepared && danmakuParser != null) {
                                try {
                                    view.prepare(danmakuParser, danmakuContext)
                                    android.util.Log.d(
                                        "Danmaku",
                                        "DanmakuView prepare called successfully"
                                    )
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
            if (isPlaying) {
                viewModel.ijkPlayer.pause()
                isPlaying = false
            } else {
                viewModel.ijkPlayer.start()
                isPlaying = true
            }
        },
        onSeek = { position ->
            viewModel.ijkPlayer.seekTo(position)
        },
        onBackClick = onNavigateBack,
        onDanmakuToggle = {
            viewModel.toggleDanmaku()
            val playerSettings = settings?.playerSettings
            if (playerSettings?.rememberDanmakuEnabled == true) {
                val videoKey = "${uiState.aid}_${uiState.cid}"
                val currentSpeed = playbackSpeed
                videoStateCache[videoKey] = Pair(uiState.isDanmakuVisible, currentSpeed)
            }
        },
        isDanmakuVisible = uiState.isDanmakuVisible,
        onSpeedChange = { speed ->
            playbackSpeed = speed
            viewModel.ijkPlayer.setSpeed(speed)
            val playerSettings = settings?.playerSettings
            if (playerSettings?.rememberSpeed == true) {
                val videoKey = "${uiState.aid}_${uiState.cid}"
                val currentDanmaku = uiState.isDanmakuVisible
                videoStateCache[videoKey] = Pair(currentDanmaku, speed)
            }
        },
        onSpeedClick = {
            showSpeedSelector = true
        },
        onQualityClick = {
            showQualitySelector = true
        },
        onDownloadClick = {
            if (canWriteToPublicDownloads()) {
                viewModel.enqueueDownload(
                    aid = uiState.aid,
                    cid = uiState.cid,
                    title = uiState.title
                )
            } else {
                pendingDownload = true
                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        },
        onDismissRequest = {
            showControls = false
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
                val playerSettings = settings?.playerSettings
                if (playerSettings?.rememberSpeed == true) {
                    val videoKey = "${uiState.aid}_${uiState.cid}"
                    val currentDanmaku = uiState.isDanmakuVisible
                    videoStateCache[videoKey] = Pair(currentDanmaku, speed)
                }
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
    onQualityClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val isRound = isRoundDevice()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onDismissRequest()
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            if (isRound) {
                var sliderPosition by remember { mutableFloatStateOf(0f) }
                var isSeeking by remember { mutableStateOf(false) }

                LaunchedEffect(currentPosition) {
                    if (!isSeeking) sliderPosition = currentPosition.toFloat()
                }

                Box(modifier = Modifier.fillMaxSize().padding(2.dp)) {
                    ArcSeekbar(
                        value = sliderPosition,
                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                        onValueChange = {
                            isSeeking = true
                            sliderPosition = it
                        },
                        onValueChangeFinished = {
                            onSeek(sliderPosition.toLong())
                            isSeeking = false
                        },
                        onDismissRequest = onDismissRequest,
                        strokeWidth = 4.dp,
                        thumbRadius = 6.dp
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onPlayPauseClick,
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Box(modifier = Modifier.align(Alignment.CenterStart)) {
                        IconButton(onClick = onSpeedClick) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Speed, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Text("${playbackSpeed}x", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = Color.White)
                            }
                        }
                    }

                    Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                        IconButton(onClick = onDanmakuToggle) {
                            Icon(
                                imageVector = if (isDanmakuVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                null,
                                tint = if (isDanmakuVisible) Color.White else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onQualityClick, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.HighQuality, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = onDownloadClick, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                if (isLongPressing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FastForward, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            Text("2.0x", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .padding(top = PaddingDefaults.verticalOptContentPadding()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (isLongPressing) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 60.dp)
                            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
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
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .padding(bottom = PaddingDefaults.verticalOptContentPadding())
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismissRequest() }
                ) {
                    var sliderPosition by remember { mutableFloatStateOf(0f) }
                    var isSeeking by remember { mutableStateOf(false) }

                    LaunchedEffect(currentPosition) {
                        if (!isSeeking) sliderPosition = currentPosition.toFloat()
                    }

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
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onPlayPauseClick) {
                                Icon(
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    null, tint = Color.White
                                )
                            }
                            IconButton(onClick = onSpeedClick) {
                                Icon(Icons.Default.Speed, null, tint = Color.White)
                            }
                            IconButton(onClick = onDanmakuToggle) {
                                Icon(
                                    if (isDanmakuVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    null,
                                    tint = if (isDanmakuVisible) Color.White else Color.White.copy(alpha = 0.6f)
                                )
                            }
                            IconButton(onClick = onQualityClick) {
                                Icon(Icons.Default.HighQuality, null, tint = Color.White)
                            }
                            IconButton(onClick = onDownloadClick) {
                                Icon(Icons.Default.Download, null, tint = Color.White)
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
        confirmButton = { },
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

@Composable
fun ArcSeekbar(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 4.dp,
    thumbRadius: Dp = 8.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = Color.White.copy(alpha = 0.3f)
) {
    val density = LocalContext.current.resources.displayMetrics.density
    val strokeWidthPx = strokeWidth.value * density
    val thumbRadiusPx = thumbRadius.value * density

    val startAngle = 135f
    val sweepAngle = 270f

    var isDragging by remember { mutableStateOf(false) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val center = Offset((size.width / 2).toFloat(), (size.height / 2).toFloat())
                        val radius = min(size.width, size.height) / 2 - strokeWidthPx / 2 - thumbRadiusPx
                        val dist = sqrt((offset.x - center.x).pow(2) + (offset.y - center.y).pow(2))

                        val touchThreshold = 25 * density

                        if (dist >= radius - touchThreshold && dist <= radius + touchThreshold) {
                            val angle = (Math.toDegrees(atan2(offset.y - center.y, offset.x - center.x).toDouble()) + 360) % 360
                            var effectiveAngle = angle
                            if (angle < 90) effectiveAngle += 360

                            if (effectiveAngle in startAngle..(startAngle + sweepAngle)) {
                                val progress = (effectiveAngle - startAngle) / sweepAngle
                                val newValue = valueRange.start + progress * (valueRange.endInclusive - valueRange.start)
                                onValueChange(newValue.toFloat().coerceIn(valueRange.start, valueRange.endInclusive))
                                onValueChangeFinished?.invoke()
                            } else {
                                onDismissRequest()
                            }
                        } else {
                            onDismissRequest()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        onValueChangeFinished?.invoke()
                    },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, _ ->
                        val center = Offset((size.width / 2).toFloat(), (size.height / 2).toFloat())
                        val offset = change.position

                        val angle = (Math.toDegrees(atan2(offset.y - center.y, offset.x - center.x).toDouble()) + 360) % 360
                        var effectiveAngle = angle
                        if (angle < 90) effectiveAngle += 360

                        val clampedAngle = effectiveAngle.coerceIn(startAngle.toDouble(), (startAngle + sweepAngle).toDouble())

                        val progress = (clampedAngle - startAngle) / sweepAngle
                        val newValue = valueRange.start + progress * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newValue.toFloat())
                    }
                )
            }
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = min(size.width, size.height) / 2 - strokeWidthPx / 2 - thumbRadiusPx

        drawArc(
            color = inactiveColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )

        val progress = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
        val progressSweep = (progress * sweepAngle).toFloat()

        if (progressSweep > 0) {
            drawArc(
                color = activeColor,
                startAngle = startAngle,
                sweepAngle = progressSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        val thumbAngleRad = Math.toRadians((startAngle + progressSweep).toDouble())
        val thumbX = center.x + radius * cos(thumbAngleRad).toFloat()
        val thumbY = center.y + radius * sin(thumbAngleRad).toFloat()

        drawCircle(color = Color.White, radius = thumbRadiusPx, center = Offset(thumbX, thumbY))
        drawCircle(color = activeColor.copy(alpha = 0.3f), radius = thumbRadiusPx + 4f, center = Offset(thumbX, thumbY))
    }
}