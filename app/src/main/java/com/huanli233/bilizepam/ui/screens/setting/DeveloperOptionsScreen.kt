package com.huanli233.bilizepam.ui.screens.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.dialog.AdaptDialog

@Composable
fun DeveloperOptionsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var showClearSettingsDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScalingLazyListState()
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    ScreenScaffold(
        scrollState = scrollState,
        topBar = scrollAwareTopBar(
            title = stringResource(id = R.string.developer_options),
            showBackIcon = true,
            onBackClick = { navController.popBackStack() },
            scrollBehavior = scrollBehavior
        ),
        topBarScrollBehavior = scrollBehavior
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = scrollState,
                contentPadding = paddingValues
            ) {
                item {
                    SettingsCategory(title = stringResource(id = R.string.data_management))
                }

                item {
                    SettingsItem(
                        icon = Icons.Outlined.Delete,
                        title = stringResource(id = R.string.clear_settings),
                        summary = stringResource(id = R.string.clear_settings_desc),
                        onClick = { showClearSettingsDialog = true }
                    )
                }
            }
        }
    }

    if (showClearSettingsDialog) {
        AdaptDialog(
            onDismissRequest = { showClearSettingsDialog = false },
            title = { Text(stringResource(id = R.string.clear_settings)) },
            text = { Text(stringResource(id = R.string.clear_settings_warning)) },
            confirmButton = { close ->
                TextButton(
                    onClick = {
                        viewModel.clearAllSettings()
                        close()
                    }
                ) {
                    Text(stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearSettingsDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

