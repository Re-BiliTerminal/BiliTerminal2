package com.huanli233.bilizepam.ui.widget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.pager.PagerState
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.type.IndicatorType
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DotsIndicator(
    dotCount: Int,
    modifier: Modifier = Modifier,
    dotSpacing: Dp = 12.dp,
    type: IndicatorType,
    pagerState: PagerState,
) {
    val coroutineScope = rememberCoroutineScope()
    DotsIndicator(
        dotCount = dotCount,
        modifier = modifier,
        dotSpacing = dotSpacing,
        type = type,
        currentPage = pagerState.currentPage,
        currentPageOffsetFraction = { pagerState.currentPageOffsetFraction },
    ) { dotIndex ->
        coroutineScope.launch { pagerState.animateScrollToPage(dotIndex) }
    }
}