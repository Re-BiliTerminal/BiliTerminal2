package androidx.wear.compose.materialcore

import androidx.compose.foundation.layout.PaddingValues

fun PaddingValues.toVerticalPadding() = PaddingValues(top = calculateTopPadding(), bottom = calculateBottomPadding())