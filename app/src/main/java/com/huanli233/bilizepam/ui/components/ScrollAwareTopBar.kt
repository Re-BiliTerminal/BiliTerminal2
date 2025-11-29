package com.huanli233.bilizepam.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material3.PaddingDefaults
import com.huanli233.bilizepam.utils.isRound
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A TopAppBarScrollBehavior defines how a top app bar should behave when the content under it is scrolled.
 * Based on Material3 design patterns.
 */
@Stable
interface TopBarScrollBehavior {
    /**
     * A [TopBarState] that is attached to this behavior and is read and updated when scrolling happens.
     */
    val state: TopBarState

    /**
     * Indicates whether the top app bar is pinned.
     */
    val isPinned: Boolean

    /**
     * An optional [AnimationSpec] that defines how the top app bar snaps to either fully
     * collapsed or fully extended state when a fling or a drag scrolled it into an intermediate position.
     */
    val snapAnimationSpec: AnimationSpec<Float>?

    /**
     * An optional [DecayAnimationSpec] that defined how to fling the top app bar when the user
     * flings the app bar itself, or the content below it.
     */
    val flingAnimationSpec: DecayAnimationSpec<Float>?

    /**
     * A [NestedScrollConnection] that should be attached to a [Modifier.nestedScroll] in order to
     * keep track of the scroll events.
     */
    val nestedScrollConnection: NestedScrollConnection
}

/**
 * A state object that can be hoisted to control and observe the top app bar state.
 */
@Stable
interface TopBarState {
    /**
     * The top app bar's height offset limit in pixels, which represents the limit that a top app bar
     * is allowed to collapse to.
     */
    var heightOffsetLimit: Float

    /**
     * The top app bar's current height offset in pixels. This height offset is applied to the fixed
     * height of the app bar to control the displayed height when content is being scrolled.
     */
    var heightOffset: Float

    /**
     * The total offset of the content scrolled under the top app bar.
     */
    var contentOffset: Float

    /**
     * A value that represents the collapsed height percentage of the app bar.
     * A `0.0` represents a fully expanded bar, and `1.0` represents a fully collapsed bar.
     */
    val collapsedFraction: Float

    companion object {
        /** The default [Saver] implementation for [TopBarState]. */
        val Saver: Saver<TopBarState, *> = listSaver(
            save = { listOf(it.heightOffsetLimit, it.heightOffset, it.contentOffset) },
            restore = {
                TopBarStateImpl(
                    initialHeightOffsetLimit = it[0],
                    initialHeightOffset = it[1],
                    initialContentOffset = it[2]
                )
            }
        )
    }
}

/**
 * Creates a [TopBarState] that is remembered across compositions.
 */
@Composable
fun rememberTopBarState(
    initialHeightOffsetLimit: Float = -Float.MAX_VALUE,
    initialHeightOffset: Float = 0f,
    initialContentOffset: Float = 0f
): TopBarState {
    return rememberSaveable(saver = TopBarState.Saver) {
        TopBarStateImpl(initialHeightOffsetLimit, initialHeightOffset, initialContentOffset)
    }
}

@Stable
private class TopBarStateImpl(
    initialHeightOffsetLimit: Float,
    initialHeightOffset: Float,
    initialContentOffset: Float
) : TopBarState {
    override var heightOffsetLimit by mutableFloatStateOf(initialHeightOffsetLimit)
    
    override var heightOffset: Float
        get() = _heightOffset.floatValue
        set(newOffset) {
            _heightOffset.floatValue = newOffset.coerceIn(
                minimumValue = heightOffsetLimit,
                maximumValue = 0f
            )
        }
    
    override var contentOffset by mutableFloatStateOf(initialContentOffset)
    
    override val collapsedFraction: Float
        get() = if (heightOffsetLimit != 0f) {
            heightOffset / heightOffsetLimit
        } else {
            0f
        }
    
    private var _heightOffset = mutableFloatStateOf(initialHeightOffset)
}

/**
 * Returns a [TopBarScrollBehavior] that adjusts its properties to affect the colors and height of a top app bar.
 * A top app bar that is set up with this [TopBarScrollBehavior] will immediately collapse when
 * the nested content is pulled up, and will immediately appear when the content is pulled down.
 */
@Composable
fun rememberEnterAlwaysScrollBehavior(
    state: TopBarState = rememberTopBarState(),
    canScroll: () -> Boolean = { true },
    snapAnimationSpec: AnimationSpec<Float>? = tween(durationMillis = 150),
    flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay()
): TopBarScrollBehavior = remember(state, canScroll, snapAnimationSpec, flingAnimationSpec) {
    EnterAlwaysScrollBehavior(
        state = state,
        snapAnimationSpec = snapAnimationSpec,
        flingAnimationSpec = flingAnimationSpec,
        canScroll = canScroll
    )
}

private class EnterAlwaysScrollBehavior(
    override val state: TopBarState,
    override val snapAnimationSpec: AnimationSpec<Float>?,
    override val flingAnimationSpec: DecayAnimationSpec<Float>?,
    val canScroll: () -> Boolean = { true }
) : TopBarScrollBehavior {
    override val isPinned: Boolean = false
    
    override val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (!canScroll()) return Offset.Zero
            
            // Only consume scroll if we can actually collapse/expand the TopBar
            if (state.heightOffsetLimit == -Float.MAX_VALUE) {
                return Offset.Zero
            }
            
            // EnterAlways behavior: TopBar and content should scroll together
            // Only consume scroll in very specific cases to maintain coordination
            
            // Don't consume scroll in onPreScroll for EnterAlways behavior
            // Let content scroll first, TopBar will follow in onPostScroll
            // This ensures content and TopBar move together
            return Offset.Zero
        }
        
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            if (!canScroll()) return Offset.Zero
            
            // Only update if heightOffsetLimit is set
            if (state.heightOffsetLimit != -Float.MAX_VALUE) {
                state.contentOffset += consumed.y
                // Update TopBar offset based on content scroll
                state.heightOffset += consumed.y
            }
            return Offset.Zero
        }
        
        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            val superConsumed = super.onPostFling(consumed, available)
            
            // Snap behavior: 当滚动停止时，根据当前位置和滚动方向决定是完全展开还是完全隐藏
            if (state.heightOffsetLimit != -Float.MAX_VALUE) {
                val currentOffset = state.heightOffset
                
                // 使用不对称的阈值：
                // - 向上滚动（隐藏）：只需要隐藏 20% 就会完全隐藏
                // - 向下滚动（显示）：需要显示 80% 才会完全展开
                val hideThreshold = state.heightOffsetLimit * 0.8f  // 隐藏 20% 就触发
                val showThreshold = state.heightOffsetLimit * 0.2f  // 显示 80% 才触发
                
                val targetOffset = when {
                    // 如果已经隐藏超过 20%，就完全隐藏
                    currentOffset < hideThreshold -> state.heightOffsetLimit
                    // 如果显示超过 80%，就完全展开
                    currentOffset > showThreshold -> 0f
                    // 中间状态：根据速度方向决定
                    consumed.y < 0 -> state.heightOffsetLimit  // 向上滚动，隐藏
                    else -> 0f  // 向下滚动，展开
                }
                
                // 使用动画平滑过渡到目标位置
                if (currentOffset != targetOffset) {
                    AnimationState(initialValue = currentOffset).animateTo(
                        targetValue = targetOffset,
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = FastOutLinearInEasing
                        )
                    ) {
                        state.heightOffset = value
                    }
                }
            }
            
            if (available.y > 0f && 
                (state.heightOffset == 0f || state.heightOffset == state.heightOffsetLimit)) {
                state.contentOffset = 0f
            }
            
            return superConsumed
        }
    }
}

/**
 * ScrollAwareTopBar with Material3 ScrollBehavior
 */
@Composable
fun ScrollAwareTopBar(
    title: String,
    modifier: Modifier = Modifier,
    scrollBehavior: TopBarScrollBehavior? = null,
    showBackIcon: Boolean = true,
    showMenuIcon: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onHeightMeasured: ((Dp) -> Unit)? = null
) {
    ScrollAwareTopBarImpl(
        title = title,
        scrollBehavior = scrollBehavior,
        showBackIcon = showBackIcon,
        showMenuIcon = showMenuIcon,
        onBackClick = onBackClick,
        onMenuClick = onMenuClick,
        modifier = modifier,
        onHeightMeasured = onHeightMeasured
    )
}

/**
 * Creates a ScrollAwareTopBar with ScalingLazyListState.
 * Returns the TopBarScrollBehavior that should be applied to scrollable content.
 * 
 * Usage example:
 * ```
 * val scrollBehavior = scrollAwareTopBar(
 *     title = "My Title",
 *     scrollState = scalingLazyListState
 * )
 * 
 * ScalingLazyColumn(
 *     state = scalingLazyListState,
 *     modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
 * ) {
 *     // Your content
 * }
 * ```
 */
@Composable
fun scrollAwareTopBar(
    title: String,
    modifier: Modifier = Modifier,
    scrollState: ScalingLazyListState,
    showBackIcon: Boolean = true,
    showMenuIcon: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onHeightMeasured: ((Dp) -> Unit)? = null
): TopBarScrollBehavior {
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()
    
    ScrollAwareTopBar(
        title = title,
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        showBackIcon = showBackIcon,
        showMenuIcon = showMenuIcon,
        onBackClick = onBackClick,
        onMenuClick = onMenuClick,
        onHeightMeasured = onHeightMeasured
    )
    
    return scrollBehavior
}

/**
 * Creates a ScrollAwareTopBar with LazyListState.
 * Returns the TopBarScrollBehavior that should be applied to scrollable content.
 * 
 * Usage example:
 * ```
 * val scrollBehavior = scrollAwareTopBar(
 *     title = "My Title",
 *     scrollState = lazyListState
 * )
 * 
 * LazyColumn(
 *     state = lazyListState,
 *     modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
 * ) {
 *     // Your content
 * }
 * ```
 */
@Composable
fun scrollAwareTopBar(
    title: String,
    modifier: Modifier = Modifier,
    scrollState: LazyListState,
    showBackIcon: Boolean = true,
    showMenuIcon: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onHeightMeasured: ((Dp) -> Unit)? = null
): TopBarScrollBehavior {
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()
    
    ScrollAwareTopBar(
        title = title,
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        showBackIcon = showBackIcon,
        showMenuIcon = showMenuIcon,
        onBackClick = onBackClick,
        onMenuClick = onMenuClick,
        onHeightMeasured = onHeightMeasured
    )
    
    return scrollBehavior
}

/**
 * Creates a ScrollAwareTopBar with ScrollState.
 * Returns the TopBarScrollBehavior that should be applied to scrollable content.
 * 
 * Usage example:
 * ```
 * val scrollBehavior = scrollAwareTopBar(
 *     title = "My Title",
 *     scrollState = scrollState
 * )
 * 
 * Column(
 *     modifier = Modifier
 *         .verticalScroll(scrollState)
 *         .nestedScroll(scrollBehavior?.nestedScrollConnection ?: NestedScrollConnection())
 * ) {
 *     // Your content
 * }
 * ```
 */
@Composable
fun scrollAwareTopBar(
    title: String,
    modifier: Modifier = Modifier,
    scrollState: ScrollState?,
    showBackIcon: Boolean = true,
    showMenuIcon: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onHeightMeasured: ((Dp) -> Unit)? = null
): TopBarScrollBehavior? {
    val scrollBehavior = if (scrollState != null) {
        rememberEnterAlwaysScrollBehavior()
    } else {
        null
    }
    
    ScrollAwareTopBar(
        title = title,
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        showBackIcon = showBackIcon,
        showMenuIcon = showMenuIcon,
        onBackClick = onBackClick,
        onMenuClick = onMenuClick,
        onHeightMeasured = onHeightMeasured
    )
    
    return scrollBehavior
}

/**
 * Creates a ScrollAwareTopBar composable that can be used directly in ScreenScaffold.
 * This is the preferred way to create a topBar for ScreenScaffold.
 */
@Composable
fun scrollAwareTopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackIcon: Boolean = true,
    showMenuIcon: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onHeightMeasured: ((Dp) -> Unit)? = null,
    scrollBehavior: TopBarScrollBehavior? = null, // Allow external ScrollBehavior
    onScrollBehaviorCreated: ((TopBarScrollBehavior) -> Unit)? = null // Callback to pass ScrollBehavior back
): @Composable () -> Unit {
    return {
        val actualScrollBehavior = scrollBehavior ?: rememberEnterAlwaysScrollBehavior()
        
        // Notify the parent about the ScrollBehavior
        LaunchedEffect(actualScrollBehavior) {
            onScrollBehaviorCreated?.invoke(actualScrollBehavior)
        }
        
        ScrollAwareTopBar(
            title = title,
            modifier = modifier,
            scrollBehavior = actualScrollBehavior,
            showBackIcon = showBackIcon,
            showMenuIcon = showMenuIcon,
            onBackClick = onBackClick,
            onMenuClick = onMenuClick,
            onHeightMeasured = onHeightMeasured
        )
    }
}

/**
 * Base implementation that renders the actual TopBar with Material3 ScrollBehavior
 */
@Composable
    private fun ScrollAwareTopBarImpl(
    title: String,
    scrollBehavior: TopBarScrollBehavior?,
    showBackIcon: Boolean,
    showMenuIcon: Boolean,
    onBackClick: (() -> Unit)?,
    onMenuClick: (() -> Unit)?,
    modifier: Modifier,
    onHeightMeasured: ((Dp) -> Unit)? = null
) {
    // WearTopBar now handles its own status bar padding
    val density = LocalDensity.current

    // Calculate offset from scroll behavior
    val heightOffset = scrollBehavior?.state?.heightOffset ?: 0f

    val paddings = WindowInsets.systemBars.asPaddingValues()
    val verticalPadding = with(density) { if (isRound) PaddingDefaults.verticalContentPadding().toPx() else 0f }
    Box(
        modifier = modifier
            .offset { IntOffset(0, heightOffset.roundToInt()) }
            .onGloballyPositioned { coordinates ->
                val h = with(density) {
                    coordinates.size.height.toDp()
                }
                val hPx = coordinates.size.height.toFloat()
                
                // Update scroll behavior state with height information
                // TopBar 现在包含状态栏，但我们只需要隐藏内容部分
                scrollBehavior?.state?.let { state ->
                    // 只隐藏 TopBar 内容部分，保留状态栏
                    val systemBarsPadding = paddings
                    val statusBarHeight = with(density) { 
                        systemBarsPadding.calculateTopPadding().toPx() 
                    }
                    val contentHeight = hPx + statusBarHeight
                    val hiddenOffset = -contentHeight - verticalPadding
                    if (state.heightOffsetLimit != hiddenOffset) {
                        state.heightOffsetLimit = hiddenOffset
                    }
                }
                
                onHeightMeasured?.invoke(h)
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
