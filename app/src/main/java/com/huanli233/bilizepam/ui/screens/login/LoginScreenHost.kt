package com.huanli233.bilizepam.ui.screens.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.PaddingDefaults
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
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
    val scope = rememberCoroutineScope()
    
    val qrCodeScrollState = rememberScrollState()
    val importScrollState = rememberScrollState()
    
    val currentScrollState by remember {
        derivedStateOf {
            when (pagerState.currentPage) {
                0 -> qrCodeScrollState
                1 -> importScrollState
                else -> qrCodeScrollState
            }
        }
    }
    
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    ScreenScaffold(
        scrollState = currentScrollState,
        topBar = scrollAwareTopBar(
            title = stringResource(R.string.login),
            showBackIcon = true,
            onBackClick = onSkip,
            scrollBehavior = scrollBehavior
        ),
        topBarScrollBehavior = scrollBehavior
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    0 -> QrCodeLoginScreen(
                        scrollState = qrCodeScrollState,
                        paddingValues = paddingValues,
                        onNavigateToImport = { scope.launch { pagerState.animateScrollToPage(1) } },
                        onSkip = onSkip,
                        onLoginSuccess = onLoginSuccess
                    )
                    1 -> ImportLoginScreen(
                        scrollState = importScrollState,
                        paddingValues = paddingValues,
                        onLoginSuccess = onLoginSuccess
                    )
                }
            }

            DotsIndicator(
                modifier = Modifier
                    .padding(bottom = PaddingDefaults.verticalOptContentPadding())
                    .align(Alignment.BottomCenter),
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
}