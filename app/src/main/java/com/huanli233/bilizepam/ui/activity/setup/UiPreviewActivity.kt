package com.huanli233.bilizepam.ui.activity.setup

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.activity.base.BaseActivity
import com.huanli233.bilizepam.ui.components.TopBar
import com.huanli233.bilizepam.ui.components.TopBarState
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

    LaunchedEffect(Unit) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        while (true) {
            currentTime = LocalTime.now().format(formatter)
            delay(1000L)
        }
    }

    Scaffold { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            TopBar(
                modifier = Modifier.fillMaxWidth()
                    .clickable { onFinish },
                title = stringResource(R.string.view_preview),
                state = remember { TopBarState.PAGE },
                time = currentTime,
                isMenuScreen = false
            )
        }
    }
}