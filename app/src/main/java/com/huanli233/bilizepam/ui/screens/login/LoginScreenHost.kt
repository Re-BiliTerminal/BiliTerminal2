package com.huanli233.bilizepam.ui.screens.login

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.PaddingDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.verticalContentPadding
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.WearTopBar
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

    ScreenScaffold {
        Box(modifier = Modifier.fillMaxSize().padding(vertical = PaddingDefaults.verticalOptContentPadding())) {
            Column(modifier = Modifier.fillMaxSize()) {
                WearTopBar(
                    title = stringResource(R.string.login),
                    showBackIcon = true,
                    modifier = Modifier.clickable { onSkip() }
                )

                val scope = rememberCoroutineScope()

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
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
                    modifier = Modifier.padding(bottom = PaddingDefaults.verticalOptContentPadding()),
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
}

@Composable
private fun WormDotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    selectedColor: Color,
    unselectedColor: Color,
    dotSize: androidx.compose.ui.unit.Dp,
    dotSpacing: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val spacing = dotSpacing.value
    val size = dotSize.value
    
    Canvas(
        modifier = modifier
            .width((totalDots * (size + spacing) - spacing).dp)
            .height(size.dp)
    ) {
        val canvasWidth = this.size.width
        val canvasHeight = this.size.height
        val dotRadius = size / 2
        
        repeat(totalDots) { index ->
            val x = (index * (size + spacing) + dotRadius) * density
            val y = canvasHeight / 2
            
            if (index == selectedIndex) {
                drawCircle(
                    color = selectedColor,
                    radius = dotRadius * density * 1.5f,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            } else {
                drawCircle(
                    color = unselectedColor,
                    radius = dotRadius * density,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }
    }
}
