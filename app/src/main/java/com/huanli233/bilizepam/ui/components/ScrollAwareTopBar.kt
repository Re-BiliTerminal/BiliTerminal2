package com.huanli233.bilizepam.ui.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.ui.unit.Dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material3.PaddingDefaults
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.roundToInt

/**
 * ScrollAwareTopBar with ScalingLazyListState
 * Hides when scrolling up, shows when scrolling down (scroll|enterAlways)
 */
@Composable
fun ScrollAwareTopBar(
    title: String,
    modifier: Modifier = Modifier,
    scrollState: ScalingLazyListState,
    showBackIcon: Boolean = true,
    showMenuIcon: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onHeightMeasured: ((Dp) -> Unit)? = null
) {
    var topBarHeightPx by remember { mutableStateOf(0f) }

    var topBarOffsetY by remember { mutableStateOf(0f) }
    var lastScrollPosition by remember { mutableStateOf(-1) }
    var lastScrollOffset by remember { mutableStateOf(0) }
    var accumulatedDelta by remember { mutableStateOf(0f) }

    LaunchedEffect(scrollState) {
        snapshotFlow {
            Triple(
                scrollState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0,
                scrollState.layoutInfo.visibleItemsInfo.firstOrNull()?.offset ?: 0,
                scrollState.isScrollInProgress
            )
        }.distinctUntilChanged().collect { (index, offset, isScrolling) ->
            Log.d("ScrollAwareTopBar", "[ScalingLazy] topBarHeightPx=$topBarHeightPx, index=$index, offset=$offset, isScrolling=$isScrolling")
            
            if (topBarHeightPx > 0) {
                if (lastScrollPosition == -1) {
                    Log.d("ScrollAwareTopBar", "[ScalingLazy] Init: lastScrollPosition=$index, lastScrollOffset=$offset")
                    lastScrollPosition = index
                    lastScrollOffset = offset
                    return@collect
                }

                if (!isScrolling) {
                    Log.d("ScrollAwareTopBar", "[ScalingLazy] Not scrolling, updating last position only")
                    lastScrollPosition = index
                    lastScrollOffset = offset
                    return@collect
                }

                val deltaIndex = index - lastScrollPosition
                val deltaOffset = offset - lastScrollOffset

                val scrollDelta = when {
                    deltaIndex != 0 -> deltaIndex * 1000f + deltaOffset
                    else -> deltaOffset.toFloat()
                }

                Log.d("ScrollAwareTopBar", "[ScalingLazy] deltaIndex=$deltaIndex, deltaOffset=$deltaOffset, scrollDelta=$scrollDelta")

                if (kotlin.math.abs(scrollDelta) > 5f) {
                    val oldAccumulated = accumulatedDelta
                    accumulatedDelta = (accumulatedDelta + scrollDelta).coerceIn(-topBarHeightPx, 0f)
                    topBarOffsetY = accumulatedDelta
                    Log.d("ScrollAwareTopBar", "[ScalingLazy] accumulatedDelta: $oldAccumulated -> $accumulatedDelta, topBarOffsetY=$topBarOffsetY")
                }

                lastScrollPosition = index
                lastScrollOffset = offset
            }
        }
    }

    ScrollAwareTopBarImpl(
        title = title,
        topBarOffsetY = topBarOffsetY,
        topBarHeightPx = topBarHeightPx,
        showBackIcon = showBackIcon,
        showMenuIcon = showMenuIcon,
        onBackClick = onBackClick,
        onMenuClick = onMenuClick,
        modifier = modifier,
        onHeightMeasured = onHeightMeasured,
        onHiddenOffsetMeasured = { topBarHeightPx = it }
    )
}

/**
 * ScrollAwareTopBar with LazyListState
 * Hides when scrolling up, shows when scrolling down (scroll|enterAlways)
 */
@Composable
fun ScrollAwareTopBar(
    title: String,
    modifier: Modifier = Modifier,
    scrollState: LazyListState,
    showBackIcon: Boolean = true,
    showMenuIcon: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onHeightMeasured: ((Dp) -> Unit)? = null
) {
    val density = LocalDensity.current
    var topBarHeightPx by remember { mutableStateOf(0f) }

    var topBarOffsetY by remember { mutableStateOf(0f) }
    var lastScrollPosition by remember { mutableStateOf(-1) }
    var lastScrollOffset by remember { mutableStateOf(0) }
    var accumulatedDelta by remember { mutableStateOf(0f) }

    LaunchedEffect(scrollState) {
        snapshotFlow {
            Triple(
                scrollState.firstVisibleItemIndex,
                scrollState.firstVisibleItemScrollOffset,
                scrollState.isScrollInProgress
            )
        }.distinctUntilChanged().collect { (index, offset, isScrolling) ->
            Log.d("ScrollAwareTopBar", "[LazyList] topBarHeightPx=$topBarHeightPx, index=$index, offset=$offset, isScrolling=$isScrolling")
            
            if (topBarHeightPx > 0) {
                if (lastScrollPosition == -1) {
                    Log.d("ScrollAwareTopBar", "[LazyList] Init: lastScrollPosition=$index, lastScrollOffset=$offset")
                    lastScrollPosition = index
                    lastScrollOffset = offset
                    return@collect
                }

                if (!isScrolling) {
                    Log.d("ScrollAwareTopBar", "[LazyList] Not scrolling, updating last position only")
                    lastScrollPosition = index
                    lastScrollOffset = offset
                    return@collect
                }

                val deltaIndex = index - lastScrollPosition
                val deltaOffset = offset - lastScrollOffset

                val scrollDelta = when {
                    deltaIndex != 0 -> deltaIndex * 1000f + deltaOffset
                    else -> deltaOffset.toFloat()
                }

                Log.d("ScrollAwareTopBar", "[LazyList] deltaIndex=$deltaIndex, deltaOffset=$deltaOffset, scrollDelta=$scrollDelta")

                if (kotlin.math.abs(scrollDelta) > 5f) {
                    val oldAccumulated = accumulatedDelta
                    accumulatedDelta = (accumulatedDelta + scrollDelta).coerceIn(-topBarHeightPx, 0f)
                    topBarOffsetY = accumulatedDelta
                    Log.d("ScrollAwareTopBar", "[LazyList] accumulatedDelta: $oldAccumulated -> $accumulatedDelta, topBarOffsetY=$topBarOffsetY")
                }

                lastScrollPosition = index
                lastScrollOffset = offset
            }
        }
    }

    ScrollAwareTopBarImpl(
        title = title,
        topBarOffsetY = topBarOffsetY,
        topBarHeightPx = topBarHeightPx,
        showBackIcon = showBackIcon,
        showMenuIcon = showMenuIcon,
        onBackClick = onBackClick,
        onMenuClick = onMenuClick,
        modifier = modifier,
        onHeightMeasured = onHeightMeasured,
        onHiddenOffsetMeasured = { topBarHeightPx = it }
    )
}

/**
 * ScrollAwareTopBar with ScrollState
 * Hides when scrolling up, shows when scrolling down (scroll|enterAlways)
 */
@Composable
fun ScrollAwareTopBar(
    title: String,
    modifier: Modifier = Modifier,
    scrollState: ScrollState?,
    showBackIcon: Boolean = true,
    showMenuIcon: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onHeightMeasured: ((Dp) -> Unit)? = null
) {
    val density = LocalDensity.current
    var topBarHeightPx by remember { mutableStateOf(0f) }

    var topBarOffsetY by remember { mutableStateOf(0f) }
    var lastScrollValue by remember { mutableStateOf(-1) }
    var accumulatedDelta by remember { mutableStateOf(0f) }

    if (scrollState != null) {
        LaunchedEffect(scrollState) {
            snapshotFlow {
                Pair(scrollState.value, scrollState.isScrollInProgress)
            }.distinctUntilChanged()
                .collect { (scrollValue, isScrolling) ->
                    Log.d("ScrollAwareTopBar", "[ScrollState] topBarHeightPx=$topBarHeightPx, scrollValue=$scrollValue, isScrolling=$isScrolling")
                    
                    if (topBarHeightPx > 0) {
                        if (lastScrollValue == -1) {
                            Log.d("ScrollAwareTopBar", "[ScrollState] Init: lastScrollValue=$scrollValue")
                            lastScrollValue = scrollValue
                            return@collect
                        }

                        if (!isScrolling) {
                            Log.d("ScrollAwareTopBar", "[ScrollState] Not scrolling, updating last value only")
                            lastScrollValue = scrollValue
                            return@collect
                        }

                        val delta = (scrollValue - lastScrollValue).toFloat()

                        Log.d("ScrollAwareTopBar", "[ScrollState] delta=$delta")

                        if (kotlin.math.abs(delta) > 5f) {
                            val oldAccumulated = accumulatedDelta
                            accumulatedDelta = (accumulatedDelta + delta).coerceIn(-topBarHeightPx, 0f)
                            topBarOffsetY = accumulatedDelta
                            Log.d("ScrollAwareTopBar", "[ScrollState] accumulatedDelta: $oldAccumulated -> $accumulatedDelta, topBarOffsetY=$topBarOffsetY")
                        }

                        lastScrollValue = scrollValue
                    }
                }
        }
    }

    ScrollAwareTopBarImpl(
        title = title,
        topBarOffsetY = topBarOffsetY,
        topBarHeightPx = topBarHeightPx,
        showBackIcon = showBackIcon,
        showMenuIcon = showMenuIcon,
        onBackClick = onBackClick,
        onMenuClick = onMenuClick,
        modifier = modifier,
        onHeightMeasured = onHeightMeasured,
        onHiddenOffsetMeasured = { topBarHeightPx = it }
    )
}

/**
 * Base implementation that renders the actual TopBar with animation
 */
@Composable
private fun ScrollAwareTopBarImpl(
    title: String,
    topBarOffsetY: Float,
    topBarHeightPx: Float,
    showBackIcon: Boolean,
    showMenuIcon: Boolean,
    onBackClick: (() -> Unit)?,
    onMenuClick: (() -> Unit)?,
    modifier: Modifier,
    onHeightMeasured: ((Dp) -> Unit)? = null,
    onHiddenOffsetMeasured: ((Float) -> Unit)? = null
) {
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
    val topPadding = with(LocalDensity.current) { (systemBarsPadding.calculateTopPadding() + PaddingDefaults.verticalOptContentPadding()).toPx() }

    val animatedOffset by animateFloatAsState(
        targetValue = topBarOffsetY,
        animationSpec = tween(durationMillis = 150),
        label = "topBarOffset"
    )

    val density = LocalDensity.current
    Box(
        modifier = modifier
            .offset { IntOffset(0, animatedOffset.roundToInt()) }
            .onGloballyPositioned { coordinates ->
                val h = with(density) {
                    coordinates.size.height.toDp()
                }
                val hPx = coordinates.size.height.toFloat()
                val hiddenOffset = hPx + topPadding
                onHeightMeasured?.invoke(h)
                onHiddenOffsetMeasured?.invoke(hiddenOffset)
            }
    ) {
        WearTopBar(
            title = title,
            showBackIcon = showBackIcon,
            showMenuIcon = showMenuIcon,
            onBackClick = onBackClick,
            onMenuClick = onMenuClick
        )
    }
}
