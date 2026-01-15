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
    var showSpeedDialog by remember { mutableStateOf(false) }

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
                        title = stringResource(id = R.string.enable_one_finger_zoom),
                        summary = stringResource(id = R.string.enable_one_finger_zoom_desc),
                        checked = currentSettings.playerSettings.enableOneFingerZoom,
                        onCheckedChange = {
                            viewModel.updatePlayerSettings(
                                currentSettings.playerSettings.edit {
                                    enableOneFingerZoom = it
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

                item {
                    SettingsCategory(title = stringResource(id = R.string.preference))
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.default_danmaku_enabled),
                        summary = stringResource(id = R.string.default_danmaku_enabled_desc),
                        checked = currentSettings.playerSettings.defaultDanmakuEnabled,
                        onCheckedChange = {
                            viewModel.updatePlayerSettings(
                                currentSettings.playerSettings.edit {
                                    defaultDanmakuEnabled = it
                                }
                            )
                        }
                    )
                }

                item {
                    SettingsItem(
                        title = stringResource(id = R.string.default_speed),
                        summary = "${currentSettings.playerSettings.defaultSpeed}x",
                        onClick = { showSpeedDialog = true }
                    )
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.remember_danmaku_enabled),
                        summary = stringResource(id = R.string.remember_danmaku_enabled_desc),
                        checked = currentSettings.playerSettings.rememberDanmakuEnabled,
                        onCheckedChange = {
                            viewModel.updatePlayerSettings(
                                currentSettings.playerSettings.edit {
                                    rememberDanmakuEnabled = it
                                }
                            )
                        }
                    )
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.remember_speed),
                        summary = stringResource(id = R.string.remember_speed_desc),
                        checked = currentSettings.playerSettings.rememberSpeed,
                        onCheckedChange = {
                            viewModel.updatePlayerSettings(
                                currentSettings.playerSettings.edit {
                                    rememberSpeed = it
                                }
                            )
                        }
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

    if (showSpeedDialog) {
        SpeedSelectionDialog(
            currentSpeed = currentSettings.playerSettings.defaultSpeed,
            onDismiss = { showSpeedDialog = false },
            onConfirm = { speed ->
                viewModel.updatePlayerSettings(
                    currentSettings.playerSettings.edit {
                        defaultSpeed = speed
                    }
                )
                showSpeedDialog = false
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

@Composable
private fun SpeedSelectionDialog(
    currentSpeed: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    val speedOptions = listOf(
        0.5f to "0.5x",
        0.75f to "0.75x",
        1.0f to "1.0x",
        1.25f to "1.25x",
        1.5f to "1.5x",
        1.75f to "1.75x",
        2.0f to "2.0x"
    )

    var selectedSpeed by remember { mutableStateOf(currentSpeed) }

    AdaptDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.default_speed)) },
        text = {
            Column(Modifier.selectableGroup()) {
                speedOptions.forEach { (speed, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (speed == selectedSpeed),
                                onClick = { selectedSpeed = speed },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (speed == selectedSpeed),
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
                    onConfirm(selectedSpeed)
                    close()
                }
            ) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}
