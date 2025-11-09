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
 * Material3 behavior: Hides when scrolling up, shows when scrolling down (scroll|enterAlways)
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

    LaunchedEffect(scrollState) {
        var lastScrollY = 0f
        var scrollVelocity = 0f
        
        snapshotFlow {
            val firstItem = scrollState.layoutInfo.visibleItemsInfo.firstOrNull()
            val currentScrollY = (firstItem?.index ?: 0) * 200f + (firstItem?.offset ?: 0)
            Pair(currentScrollY, scrollState.isScrollInProgress)
        }.collect { (currentScrollY, isScrolling) ->
            
            if (topBarHeightPx > 0 && isScrolling) {
                val deltaY = currentScrollY - lastScrollY

                if (kotlin.math.abs(deltaY) > 15f) {
                    scrollVelocity = deltaY * 0.3f
                    
                    val newOffset = (topBarOffsetY - scrollVelocity).coerceIn(-topBarHeightPx, 0f)

                    if (kotlin.math.abs(newOffset - topBarOffsetY) > 3f) {
                        topBarOffsetY = newOffset
                    }
                }
                
                lastScrollY = currentScrollY
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
 * Material3 behavior: Hides when scrolling up, shows when scrolling down (scroll|enterAlways)
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

    LaunchedEffect(scrollState) {
        var lastScrollY = 0f
        
        snapshotFlow {
            val currentScrollY = scrollState.firstVisibleItemIndex * 200f + scrollState.firstVisibleItemScrollOffset
            Pair(currentScrollY, scrollState.isScrollInProgress)
        }.collect { (currentScrollY, isScrolling) ->
            
            if (topBarHeightPx > 0 && isScrolling) {
                val deltaY = currentScrollY - lastScrollY

                if (kotlin.math.abs(deltaY) > 20f) {
                    val scrollDirection = if (deltaY > 0) 1f else -1f
                    val scrollAmount = kotlin.math.abs(deltaY) * 0.25f
                    
                    val newOffset = (topBarOffsetY - scrollDirection * scrollAmount).coerceIn(-topBarHeightPx, 0f)
                    
                    if (kotlin.math.abs(newOffset - topBarOffsetY) > 4f) {
                        topBarOffsetY = newOffset
                    }
                }
                
                lastScrollY = currentScrollY
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
 * Material3 behavior: Hides when scrolling up, shows when scrolling down (scroll|enterAlways)
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

    if (scrollState != null) {
        LaunchedEffect(scrollState) {
            var lastScrollValue = scrollState.value
            
            snapshotFlow {
                Pair(scrollState.value, scrollState.isScrollInProgress)
            }.collect { (scrollValue, isScrolling) ->
                
                if (topBarHeightPx > 0 && isScrolling) {
                    val delta = scrollValue - lastScrollValue

                    if (kotlin.math.abs(delta) > 25f) {
                        val scrollDirection = if (delta > 0) 1f else -1f
                        val scrollAmount = kotlin.math.abs(delta) * 0.3f
                        
                        val newOffset = (topBarOffsetY - scrollDirection * scrollAmount).coerceIn(-topBarHeightPx, 0f)
                        
                        if (kotlin.math.abs(newOffset - topBarOffsetY) > 5f) {
                            topBarOffsetY = newOffset
                        }
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
