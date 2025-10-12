package com.huanli233.bilizepam.ui.screens.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanli233.bilizepam.R
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.WormIndicatorType
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenHost(
    onLoginSuccess: () -> Unit,
    onSkip: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> QrCodeLoginScreen(
                        onNavigateToImport = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        onSkip = onSkip,
                        onLoginSuccess = onLoginSuccess
                    )
                    1 -> ImportLoginScreen(
                        onLoginSuccess = onLoginSuccess
                    )
                }
            }

            DotsIndicator(
                dotCount = pagerState.pageCount,
                pagerState = pagerState,
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
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
