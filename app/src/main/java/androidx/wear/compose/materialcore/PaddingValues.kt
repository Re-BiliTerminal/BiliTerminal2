package androidx.wear.compose.materialcore

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

fun PaddingValues.toVerticalPadding() = PaddingValues(top = calculateTopPadding(), bottom = calculateBottomPadding())

fun PaddingValues.toHorizontalPadding(layoutDirection: LayoutDirection) = PaddingValues(start = calculateStartPadding(layoutDirection), end = calculateEndPadding(layoutDirection))

@Composable
fun PaddingValues.copy(
    start: Dp = this.calculateStartPadding(LocalLayoutDirection.current),
    top: Dp = this.calculateTopPadding(),
    end: Dp = this.calculateEndPadding(LocalLayoutDirection.current),
    bottom: Dp = this.calculateBottomPadding()
) = PaddingValues(
    start = start,
    top = top,
    end = end,
    bottom = bottom
)

val PaddingValues.start: Dp
    @Composable get() = calculateStartPadding(LocalLayoutDirection.current)

val PaddingValues.end: Dp
    @Composable get() = calculateEndPadding(LocalLayoutDirection.current)

val PaddingValues.top: Dp
    get() = calculateTopPadding()

val PaddingValues.bottom: Dp
    get() = calculateBottomPadding()
@Composable
operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    return PaddingValues(
        start = this.start + other.start,
        top = this.top + other.top,
        end = this.end + other.end,
        bottom = this.bottom + other.bottom
    )
}

@Composable
operator fun PaddingValues.minus(other: PaddingValues): PaddingValues {
    return PaddingValues(
        start = this.start - other.start,
        top = this.top - other.top,
        end = this.end - other.end,
        bottom = this.bottom - other.bottom
    )
}