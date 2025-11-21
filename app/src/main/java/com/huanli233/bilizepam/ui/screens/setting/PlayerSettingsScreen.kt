package com.huanli233.bilizepam.ui.screens.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.setting.edit
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.dialog.AdaptDialog

@Composable
fun PlayerSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settingsState.collectAsState()
    val currentSettings = settings ?: return

    var showQualityDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScalingLazyListState()
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    ScreenScaffold(
        scrollState = scrollState,
        topBar = scrollAwareTopBar(
            title = stringResource(id = R.string.settings_player),
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
                    SettingsCategory(title = stringResource(id = R.string.video_playback))
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.use_software_decoder),
                        summary = stringResource(id = R.string.use_software_decoder_desc),
                        checked = currentSettings.playerSettings.useSoftwareDecoder,
                        onCheckedChange = { 
                            viewModel.updatePlayerSettings(
                                currentSettings.playerSettings.edit {
                                    useSoftwareDecoder = it
                                }
                            )
                        }
                    )
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.use_texture_view),
                        summary = stringResource(id = R.string.use_texture_view_desc),
                        checked = currentSettings.playerSettings.useTextureView,
                        onCheckedChange = { 
                            viewModel.updatePlayerSettings(
                                currentSettings.playerSettings.edit {
                                    useTextureView = it
                                }
                            )
                        }
                    )
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.auto_play),
                        summary = stringResource(id = R.string.auto_play_desc),
                        checked = currentSettings.playerSettings.autoPlay,
                        onCheckedChange = { 
                            viewModel.updatePlayerSettings(
                                currentSettings.playerSettings.edit {
                                    autoPlay = it
                                }
                            )
                        }
                    )
                }

                item {
                    SettingsCategory(title = stringResource(id = R.string.playback_quality))
                }

                item {
                    SettingsItem(
                        title = stringResource(id = R.string.default_quality),
                        summary = when (currentSettings.playerSettings.defaultQuality) {
                            16 -> "流畅 360P"
                            32 -> "清晰 480P"
                            64 -> "高清 720P"
                            80 -> "高清 1080P"
                            else -> "自动"
                        },
                        onClick = { showQualityDialog = true }
                    )
                }
            }
        }
    }

    if (showQualityDialog) {
        QualitySelectionDialog(
            currentQuality = currentSettings.playerSettings.defaultQuality,
            onDismiss = { showQualityDialog = false },
            onConfirm = { quality ->
                viewModel.updatePlayerSettings(
                    currentSettings.playerSettings.edit {
                        defaultQuality = quality
                    }
                )
                showQualityDialog = false
            }
        )
    }
}

@Composable
private fun QualitySelectionDialog(
    currentQuality: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val qualityOptions = listOf(
        0 to "自动",
        16 to "流畅 360P",
        32 to "清晰 480P",
        64 to "高清 720P",
        80 to "高清 1080P"
    )

    var selectedQuality by remember { mutableStateOf(currentQuality) }

    AdaptDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.default_quality)) },
        text = {
            Column(Modifier.selectableGroup()) {
                qualityOptions.forEach { (quality, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (quality == selectedQuality),
                                onClick = { selectedQuality = quality },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (quality == selectedQuality),
                            onClick = null
                        )
                        Text(
                            text = label,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = { close ->
            androidx.compose.material3.TextButton(
                onClick = {
                    onConfirm(selectedQuality)
                    close()
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}
