package com.huanli233.bilizepam.ui.activity.setup

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.activity.base.BaseActivity
import com.huanli233.bilizepam.ui.components.WearTopBar
import com.huanli233.bilizepam.ui.theme.BiliZepamTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class UiPreviewActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BiliZepamTheme {
                UiPreviewContent { finish() }
            }
        }
    }

}

@Composable
fun UiPreviewContent(
    onFinish: () -> Unit
) {
    var currentTime by remember { mutableStateOf("") }
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)

    LaunchedEffect(Unit) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        while (true) {
            currentTime = LocalTime.now().format(formatter)
            delay(1000L)
        }
    }

    ScreenScaffold(scrollState = scrollState) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                WearTopBar(
                    title = stringResource(R.string.view_preview),
                    showBackIcon = true,
                    modifier = Modifier.clickable { onFinish() }
                )
            }
            item {
                Text(
                    text = currentTime,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            item {
                Text(
                    text = stringResource(R.string.interface_size_tip),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}