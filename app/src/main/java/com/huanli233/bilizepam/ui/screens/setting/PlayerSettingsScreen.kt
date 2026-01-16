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
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showMaxCountDialog by remember { mutableStateOf(false) }
    var showTransparencyDialog by remember { mutableStateOf(false) }
    var showScrollSpeedDialog by remember { mutableStateOf(false) }
    var showStrokeWidthDialog by remember { mutableStateOf(false) }
    var showAreaTopDialog by remember { mutableStateOf(false) }
    var showAreaBottomDialog by remember { mutableStateOf(false) }

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

                item {
                    SettingsCategory(title = stringResource(id = R.string.danmaku_settings))
                }

                item {
                    SettingsItem(
                        title = stringResource(id = R.string.danmaku_font_size),
                        summary = "${currentSettings.playerSettings.danmakuFontSize}sp",
                        onClick = { showFontSizeDialog = true }
                    )
                }

                item {
                    SettingsItem(
                        title = stringResource(id = R.string.danmaku_max_count),
                        summary = "${currentSettings.playerSettings.danmakuMaxCount}",
                        onClick = { showMaxCountDialog = true }
                    )
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.danmaku_scroll_enabled),
                        summary = stringResource(id = R.string.danmaku_scroll_enabled_desc),
                        checked = currentSettings.playerSettings.danmakuScrollEnabled,
                        onCheckedChange = {
                            viewModel.updatePlayerSettings(
                                currentSettings.playerSettings.edit {
                                    danmakuScrollEnabled = it
                                }
                            )
                        }
                    )
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.danmaku_top_enabled),
                        summary = stringResource(id = R.string.danmaku_top_enabled_desc),
                        checked = currentSettings.playerSettings.danmakuTopEnabled,
                        onCheckedChange = {
                            viewModel.updatePlayerSettings(
                                currentSettings.playerSettings.edit {
                                    danmakuTopEnabled = it
                                }
                            )
                        }
                    )
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.danmaku_bottom_enabled),
                        summary = stringResource(id = R.string.danmaku_bottom_enabled_desc),
                        checked = currentSettings.playerSettings.danmakuBottomEnabled,
                        onCheckedChange = {
                            viewModel.updatePlayerSettings(
                                currentSettings.playerSettings.edit {
                                    danmakuBottomEnabled = it
                                }
                            )
                        }
                    )
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.danmaku_advanced_enabled),
                        summary = stringResource(id = R.string.danmaku_advanced_enabled_desc),
                        checked = currentSettings.playerSettings.danmakuAdvancedEnabled,
                        onCheckedChange = {
                            viewModel.updatePlayerSettings(
                                currentSettings.playerSettings.edit {
                                    danmakuAdvancedEnabled = it
                                }
                            )
                        }
                    )
                }

                item {
                    SettingsItem(
                        title = stringResource(id = R.string.danmaku_transparency),
                        summary = "${(currentSettings.playerSettings.danmakuTransparency * 100).toInt()}%",
                        onClick = { showTransparencyDialog = true }
                    )
                }

                item {
                    SettingsItem(
                        title = stringResource(id = R.string.danmaku_scroll_speed),
                        summary = "${currentSettings.playerSettings.danmakuScrollSpeed}x",
                        onClick = { showScrollSpeedDialog = true }
                    )
                }

                item {
                    SettingsItem(
                        title = stringResource(id = R.string.danmaku_stroke_width),
                        summary = "${currentSettings.playerSettings.danmakuStrokeWidth}",
                        onClick = { showStrokeWidthDialog = true }
                    )
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.danmaku_merge_duplicate),
                        summary = stringResource(id = R.string.danmaku_merge_duplicate_desc),
                        checked = currentSettings.playerSettings.danmakuMergeDuplicate,
                        onCheckedChange = {
                            viewModel.updatePlayerSettings(
                                currentSettings.playerSettings.edit {
                                    danmakuMergeDuplicate = it
                                }
                            )
                        }
                    )
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.danmaku_bold),
                        summary = stringResource(id = R.string.danmaku_bold_desc),
                        checked = currentSettings.playerSettings.danmakuBold,
                        onCheckedChange = {
                            viewModel.updatePlayerSettings(
                                currentSettings.playerSettings.edit {
                                    danmakuBold = it
                                }
                            )
                        }
                    )
                }

                item {
                    SettingsItem(
                        title = stringResource(id = R.string.danmaku_area_top),
                        summary = "${(currentSettings.playerSettings.danmakuAreaTop * 100).toInt()}%",
                        onClick = { showAreaTopDialog = true }
                    )
                }

                item {
                    SettingsItem(
                        title = stringResource(id = R.string.danmaku_area_bottom),
                        summary = "${(currentSettings.playerSettings.danmakuAreaBottom * 100).toInt()}%",
                        onClick = { showAreaBottomDialog = true }
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

    if (showFontSizeDialog) {
        FontSizeSelectionDialog(
            currentFontSize = currentSettings.playerSettings.danmakuFontSize,
            onDismiss = { showFontSizeDialog = false },
            onConfirm = { fontSize ->
                viewModel.updatePlayerSettings(
                    currentSettings.playerSettings.edit {
                        danmakuFontSize = fontSize
                    }
                )
                showFontSizeDialog = false
            }
        )
    }

    if (showMaxCountDialog) {
        MaxCountSelectionDialog(
            currentMaxCount = currentSettings.playerSettings.danmakuMaxCount,
            onDismiss = { showMaxCountDialog = false },
            onConfirm = { maxCount ->
                viewModel.updatePlayerSettings(
                    currentSettings.playerSettings.edit {
                        danmakuMaxCount = maxCount
                    }
                )
                showMaxCountDialog = false
            }
        )
    }

    if (showTransparencyDialog) {
        TransparencySelectionDialog(
            currentTransparency = currentSettings.playerSettings.danmakuTransparency,
            onDismiss = { showTransparencyDialog = false },
            onConfirm = { transparency ->
                viewModel.updatePlayerSettings(
                    currentSettings.playerSettings.edit {
                        danmakuTransparency = transparency
                    }
                )
                showTransparencyDialog = false
            }
        )
    }

    if (showScrollSpeedDialog) {
        ScrollSpeedSelectionDialog(
            currentSpeed = currentSettings.playerSettings.danmakuScrollSpeed,
            onDismiss = { showScrollSpeedDialog = false },
            onConfirm = { speed ->
                viewModel.updatePlayerSettings(
                    currentSettings.playerSettings.edit {
                        danmakuScrollSpeed = speed
                    }
                )
                showScrollSpeedDialog = false
            }
        )
    }

    if (showStrokeWidthDialog) {
        StrokeWidthSelectionDialog(
            currentWidth = currentSettings.playerSettings.danmakuStrokeWidth,
            onDismiss = { showStrokeWidthDialog = false },
            onConfirm = { width ->
                viewModel.updatePlayerSettings(
                    currentSettings.playerSettings.edit {
                        danmakuStrokeWidth = width
                    }
                )
                showStrokeWidthDialog = false
            }
        )
    }

    if (showAreaTopDialog) {
        AreaTopSelectionDialog(
            currentArea = currentSettings.playerSettings.danmakuAreaTop,
            onDismiss = { showAreaTopDialog = false },
            onConfirm = { area ->
                viewModel.updatePlayerSettings(
                    currentSettings.playerSettings.edit {
                        danmakuAreaTop = area
                    }
                )
                showAreaTopDialog = false
            }
        )
    }

    if (showAreaBottomDialog) {
        AreaBottomSelectionDialog(
            currentArea = currentSettings.playerSettings.danmakuAreaBottom,
            onDismiss = { showAreaBottomDialog = false },
            onConfirm = { area ->
                viewModel.updatePlayerSettings(
                    currentSettings.playerSettings.edit {
                        danmakuAreaBottom = area
                    }
                )
                showAreaBottomDialog = false
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

@Composable
private fun FontSizeSelectionDialog(
    currentFontSize: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    val fontSizeOptions = listOf(
        10f to "10sp",
        12f to "12sp",
        14f to "14sp",
        16f to "16sp",
        18f to "18sp",
        20f to "20sp",
        22f to "22sp",
        24f to "24sp"
    )

    var selectedFontSize by remember { mutableStateOf(currentFontSize) }

    AdaptDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.danmaku_font_size)) },
        text = {
            Column(Modifier.selectableGroup()) {
                fontSizeOptions.forEach { (fontSize, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (fontSize == selectedFontSize),
                                onClick = { selectedFontSize = fontSize },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (fontSize == selectedFontSize),
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
                    onConfirm(selectedFontSize)
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

@Composable
private fun MaxCountSelectionDialog(
    currentMaxCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val maxCountOptions = listOf(
        10 to "10",
        20 to "20",
        30 to "30",
        50 to "50",
        80 to "80",
        100 to "100",
        150 to "150",
        200 to "200"
    )

    var selectedMaxCount by remember { mutableStateOf(currentMaxCount) }

    AdaptDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.danmaku_max_count)) },
        text = {
            Column(Modifier.selectableGroup()) {
                maxCountOptions.forEach { (maxCount, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (maxCount == selectedMaxCount),
                                onClick = { selectedMaxCount = maxCount },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (maxCount == selectedMaxCount),
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
                    onConfirm(selectedMaxCount)
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

@Composable
private fun TransparencySelectionDialog(
    currentTransparency: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    val transparencyOptions = listOf(
        0.0f to "0%",
        0.25f to "25%",
        0.5f to "50%",
        0.75f to "75%",
        0.8f to "80%",
        0.9f to "90%",
        1.0f to "100%"
    )

    var selectedTransparency by remember { mutableStateOf(currentTransparency) }

    AdaptDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.danmaku_transparency)) },
        text = {
            Column(Modifier.selectableGroup()) {
                transparencyOptions.forEach { (transparency, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (transparency == selectedTransparency),
                                onClick = { selectedTransparency = transparency },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (transparency == selectedTransparency),
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
                    onConfirm(selectedTransparency)
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

@Composable
private fun ScrollSpeedSelectionDialog(
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
        2.0f to "2.0x",
        3.0f to "3.0x"
    )

    var selectedSpeed by remember { mutableStateOf(currentSpeed) }

    AdaptDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.danmaku_scroll_speed)) },
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

@Composable
private fun StrokeWidthSelectionDialog(
    currentWidth: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    val widthOptions = listOf(
        0f to "0",
        1f to "1",
        2f to "2",
        3f to "3",
        4f to "4",
        5f to "5"
    )

    var selectedWidth by remember { mutableStateOf(currentWidth) }

    AdaptDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.danmaku_stroke_width)) },
        text = {
            Column(Modifier.selectableGroup()) {
                widthOptions.forEach { (width, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (width == selectedWidth),
                                onClick = { selectedWidth = width },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (width == selectedWidth),
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
                    onConfirm(selectedWidth)
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

@Composable
private fun AreaTopSelectionDialog(
    currentArea: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    val areaOptions = listOf(
        0.0f to "0%",
        0.1f to "10%",
        0.2f to "20%",
        0.3f to "30%",
        0.4f to "40%",
        0.5f to "50%"
    )

    var selectedArea by remember { mutableStateOf(currentArea) }

    AdaptDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.danmaku_area_top)) },
        text = {
            Column(Modifier.selectableGroup()) {
                areaOptions.forEach { (area, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (area == selectedArea),
                                onClick = { selectedArea = area },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (area == selectedArea),
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
                    onConfirm(selectedArea)
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

@Composable
private fun AreaBottomSelectionDialog(
    currentArea: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    val areaOptions = listOf(
        0.0f to "0%",
        0.1f to "10%",
        0.2f to "20%",
        0.3f to "30%",
        0.4f to "40%",
        0.5f to "50%"
    )

    var selectedArea by remember { mutableStateOf(currentArea) }

    AdaptDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.danmaku_area_bottom)) },
        text = {
            Column(Modifier.selectableGroup()) {
                areaOptions.forEach { (area, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (area == selectedArea),
                                onClick = { selectedArea = area },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (area == selectedArea),
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
                    onConfirm(selectedArea)
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
