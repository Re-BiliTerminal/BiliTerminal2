package com.huanli233.bilizepam.ui.screens.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.PaddingDefaults
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.ScrollAwareTopBar
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.WormIndicatorType
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoginScreenHost(
    onLoginSuccess: () -> Unit,
    onSkip: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    var topBarHeight by remember { mutableStateOf(0.dp) }

    ScreenScaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        PaddingValues(
                            top = topBarHeight + paddingValues.calculateTopPadding() + PaddingDefaults.verticalOptContentPadding(),
                            bottom = PaddingDefaults.verticalOptContentPadding()
                        )
                    )
            ) {

                val scope = rememberCoroutineScope()

                Box(Modifier.weight(1f)) {
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = pagerState.currentPage > 0 || pagerState.currentPageOffsetFraction < 0
                    ) { page ->
                        when (page) {
                            0 -> QrCodeLoginScreen(
                                onNavigateToImport = { scope.launch { pagerState.animateScrollToPage(1) } },
                                onSkip = onSkip,
                                onLoginSuccess = onLoginSuccess
                            )
                            1 -> ImportLoginScreen(
                                onLoginSuccess = onLoginSuccess
                            )
                        }
                    }

                    DotsIndicator(
                        modifier = Modifier.padding(bottom = PaddingDefaults.verticalOptContentPadding()).align(Alignment.BottomCenter),
                        dotCount = pagerState.pageCount,
                        dotSpacing = 8.dp,
                        type = WormIndicatorType(
                            dotsGraphic = DotGraphic(
                                16.dp,
                                borderWidth = 2.dp,
                                borderColor = MaterialTheme.colorScheme.primary,
                                color = Color.Transparent,
                            ),
                            wormDotGraphic = DotGraphic(
                                16.dp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        ),
                        pagerState = pagerState
                    )
                }
            }
            
            ScrollAwareTopBar(
                title = stringResource(R.string.login),
                modifier = Modifier.padding(PaddingValues(top = paddingValues.calculateTopPadding())),
                scrollState = null,
                showBackIcon = true,
                onBackClick = onSkip,
                onHeightMeasured = { height ->
                    topBarHeight = height
                }
            )
        }
    }
}