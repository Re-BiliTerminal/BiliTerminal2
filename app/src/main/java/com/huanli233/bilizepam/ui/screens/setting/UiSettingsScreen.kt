package com.huanli233.bilizepam.ui.screens.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.materialcore.toVerticalPadding
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.proto.NightMode
import com.huanli233.bilizepam.ui.activity.setup.UiPreviewActivity
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.dialog.AdaptDialog
import com.huanli233.bilizepam.ui.navigation.Screen
import splitties.activities.start
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UiSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settingsState.collectAsState()

    var showUiScaleDialog by remember { mutableStateOf(false) }
    var showDensityDialog by remember { mutableStateOf(false) }
    var showNightModeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val currentSettings = settings ?: return

    val context = LocalContext.current
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    
    // Create ScrollBehavior for TopBar
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    ScreenScaffold(
        scrollState = scrollState,
        topBar = scrollAwareTopBar(
            title = stringResource(id = R.string.settings_ui),
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
                    SettingsItem(
                        title = stringResource(id = R.string.view_preview),
                        onClick = { context.start<UiPreviewActivity>() }
                    )
                }

                item {
                    SettingsCategory(title = stringResource(id = R.string.scale))
                }

                item {
                    SettingsItem(
                        title = stringResource(id = R.string.interface_scale),
                        summary = stringResource(R.string.setting_ui_desc),
                        onClick = { showUiScaleDialog = true }
                    )
                }

                item {
                    SettingsItem(
                        title = stringResource(id = R.string.density),
                        summary = stringResource(R.string.setting_ui_density_desc),
                        onClick = { showDensityDialog = true }
                    )
                }

                item {
                    SettingsCategory(title = stringResource(id = R.string.preference))
                }

                item {
                    val languageDisplayName = getLanguageDisplayName(currentSettings.language)
                    SettingsItem(
                        title = stringResource(id = R.string.settings_language),
                        summary = languageDisplayName,
                        onClick = { showLanguageDialog = true }
                    )
                }

                item {
                    val nightModeEntries = stringArrayResource(R.array.dark_theme_modes)
                    val nightModeSummary = when(currentSettings.theme.nightMode) {
                        NightMode.NIGHT_MODE_AUTO -> nightModeEntries[0]
                        NightMode.NIGHT_MODE_DAY -> nightModeEntries[1]
                        NightMode.NIGHT_MODE_NIGHT -> nightModeEntries[2]
                        else -> nightModeEntries[0]
                    }
                    SettingsItem(
                        title = stringResource(id = R.string.dark_theme),
                        summary = nightModeSummary,
                        onClick = { showNightModeDialog = true }
                    )
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.system_accent_color),
                        checked = currentSettings.theme.followSystemAccent,
                        onCheckedChange = viewModel::updateFollowSystemAccent
                    )
                }

                item {
                    SettingsItem(
                        title = stringResource(id = R.string.theme_color),
                        onClick = { navController.navigate(Screen.ThemeColor.route) }
                    )
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.disable_fullscreen_dialog),
                        checked = currentSettings.theme.fullScreenDialogDisabled,
                        onCheckedChange = viewModel::updateDisableFullscreenDialog
                    )
                }

                item {
                    SettingsCategory(title = stringResource(id = R.string.ui_style_settings))
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.video_card_background_style),
                        summary = stringResource(id = R.string.video_card_background_style_desc),
                        checked = currentSettings.uiSettings.videoCardBackgroundStyle,
                        onCheckedChange = viewModel::updateVideoCardBackgroundStyle
                    )
                }

                item {
                    SwitchSettingsItem(
                        title = stringResource(id = R.string.user_profile_background),
                        summary = stringResource(id = R.string.user_profile_background_desc),
                        checked = currentSettings.uiSettings.userProfileBackgroundEnabled,
                        onCheckedChange = viewModel::updateUserProfileBackgroundEnabled
                    )
                }
            }
        }
    }

    if (showUiScaleDialog) {
        UiScaleDialog(
            currentValue = currentSettings.uiSettings.uiScale,
            onDismiss = { showUiScaleDialog = false },
            onConfirm = {
                viewModel.updateUiScale(it)
                showUiScaleDialog = false
            }
        )
    }

    if (showDensityDialog) {
        DensityDialog(
            currentValue = currentSettings.uiSettings.density,
            onDismiss = { showDensityDialog = false },
            onConfirm = {
                viewModel.updateDensity(it)
                showDensityDialog = false
            }
        )
    }

    if (showNightModeDialog) {
        NightModeDialog(
            currentMode = currentSettings.theme.nightMode,
            onDismiss = { showNightModeDialog = false },
            onConfirm = {
                viewModel.updateNightMode(it)
                showNightModeDialog = false
            }
        )
    }

    if (showLanguageDialog) {
        LanguageDialog(
            currentLanguage = currentSettings.language,
            onDismiss = { showLanguageDialog = false },
            onConfirm = { languageTag ->
                viewModel.updateLanguage(languageTag)
                showLanguageDialog = false
            }
        )
    }
}

@Composable
private fun UiScaleDialog(
    currentValue: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var textValue by remember { mutableStateOf(currentValue.toString()) }

    AdaptDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.interface_scale)) },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text(stringResource(id = R.string.interface_scale)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = android.R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(textValue.toFloatOrNull() ?: 1.0f) },
                enabled = textValue.toFloatOrNull() != null
            ) {
                Text(stringResource(id = android.R.string.ok))
            }
        }
    )
}

@Composable
private fun DensityDialog(
    currentValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val initialText = if (currentValue > 0) currentValue.toString() else ""
    var textValue by remember { mutableStateOf(initialText) }

    AdaptDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.density)) },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text(stringResource(id = R.string.density)) },
                placeholder = { Text("Auto") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = android.R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(textValue.toIntOrNull() ?: 0) }
            ) {
                Text(stringResource(id = android.R.string.ok))
            }
        }
    )
}

@Composable
private fun NightModeDialog(
    currentMode: NightMode,
    onDismiss: () -> Unit,
    onConfirm: (NightMode) -> Unit
) {
    var selectedMode by remember { mutableStateOf(currentMode) }
    val nightModeEntries = stringArrayResource(R.array.dark_theme_modes)
    val options = listOf(
        nightModeEntries[0] to NightMode.NIGHT_MODE_AUTO,
        nightModeEntries[1] to NightMode.NIGHT_MODE_DAY,
        nightModeEntries[2] to NightMode.NIGHT_MODE_NIGHT,
    )

    AdaptDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.dark_theme)) },
        text = {
            Column(Modifier.selectableGroup()) {
                options.forEach { (text, mode) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (selectedMode == mode),
                                onClick = { selectedMode = mode },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedMode == mode),
                            onClick = null
                        )
                        Text(
                            text = text,
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = android.R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedMode) }) {
                Text(stringResource(id = android.R.string.ok))
            }
        }
    )
}

@Composable
private fun LanguageDialog(
    currentLanguage: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }
    
    val languageOptions = listOf(
        stringResource(R.string.language_system) to "",
        stringResource(R.string.language_simplified_chinese) to "zh-CN",
        stringResource(R.string.language_traditional_chinese) to "zh-TW",
        stringResource(R.string.language_english) to "en",
        stringResource(R.string.language_japanese) to "ja",
    )

    AdaptDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.settings_language)) },
        text = {
            Column(Modifier.selectableGroup()) {
                languageOptions.forEach { (displayName, languageTag) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (selectedLanguage == languageTag),
                                onClick = { selectedLanguage = languageTag },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedLanguage == languageTag),
                            onClick = null
                        )
                        Text(
                            text = displayName,
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = android.R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedLanguage) }) {
                Text(stringResource(id = android.R.string.ok))
            }
        }
    )
}

@Composable
private fun getLanguageDisplayName(languageTag: String): String {
    return when {
        languageTag.isEmpty() || languageTag == "SYSTEM" -> stringResource(R.string.language_system)
        languageTag == "zh-CN" -> stringResource(R.string.language_simplified_chinese)
        languageTag == "zh-TW" -> stringResource(R.string.language_traditional_chinese)
        languageTag == "en" -> stringResource(R.string.language_english)
        languageTag == "ja" -> stringResource(R.string.language_japanese)
        else -> {
            try {
                val locale = Locale.forLanguageTag(languageTag)
                locale.getDisplayName(locale)
            } catch (e: Exception) {
                languageTag
            }
        }
    }
}