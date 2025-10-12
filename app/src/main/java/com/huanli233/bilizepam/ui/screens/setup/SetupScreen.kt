package com.huanli233.bilizepam.ui.screens.setup

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.proto.NightMode
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.data.setting.edit
import com.huanli233.bilizepam.ui.activity.setup.UiPreviewActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import splitties.activities.start

object SetupNavRoutes {
    const val WELCOME = "welcome"
    const val UI_SETUP = "ui_setup"
}

@Composable
fun WelcomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.setup_introduction),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 5.dp)
        )
    }
}

data class UiSetupState(
    val roundMode: Boolean = false,
    val animationsEnabled: Boolean = true,
    val nightMode: NightMode = NightMode.NIGHT_MODE_AUTO,
    val uiScale: String = "1.0",
    val isUiScaleInvalid: Boolean = false
)

class UiSetupViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiSetupState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = LocalData.settings
            _uiState.value = UiSetupState(
                roundMode = settings.uiSettings.roundMode,
                animationsEnabled = settings.theme.animationsEnabled,
                nightMode = settings.theme.nightMode,
                uiScale = settings.uiSettings.uiScale.toString()
            )
        }
    }

    private fun save() {
        viewModelScope.launch {
            val currentState = _uiState.value
            LocalData.edit {
                uiSettings = uiSettings.edit {
                    roundMode = currentState.roundMode
                    currentState.uiScale.toFloatOrNull()?.takeIf { it in 0.25f..5.00f }?.let {
                        uiScale = it
                    }
                }
            }
        }
    }

    fun onRoundModeChanged(isChecked: Boolean) {
        _uiState.update { it.copy(roundMode = isChecked) }
        save()
    }

    fun onAnimationsChanged(isChecked: Boolean) {
        _uiState.update { it.copy(animationsEnabled = isChecked) }
        save()
    }

    fun onNightModeChanged(index: Int) {
        val newMode = when (index) {
            0 -> NightMode.NIGHT_MODE_AUTO
            1 -> NightMode.NIGHT_MODE_DAY
            else -> NightMode.NIGHT_MODE_NIGHT
        }
        _uiState.update { it.copy(nightMode = newMode) }
        save()
    }

    fun onUiScaleChanged(text: String) {
        val scaleValue = text.toFloatOrNull()
        val isInvalid = scaleValue == null || scaleValue !in 0.25f..5.00f
        _uiState.update { it.copy(uiScale = text, isUiScaleInvalid = isInvalid) }
        if (!isInvalid) {
            save()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UiSetupScreen(viewModel: UiSetupViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val darkThemeModes = remember { context.resources.getStringArray(R.array.dark_theme_modes) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .padding(bottom = 30.dp)
    ) {
        SettingSwitchItem(
            text = stringResource(R.string.round_screen_adaptation),
            checked = state.roundMode,
            onCheckedChange = viewModel::onRoundModeChanged
        )

        var isDropdownExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
            modifier = Modifier.padding(top = 2.dp)
        ) {
            val selectionIndex = when (state.nightMode) {
                NightMode.NIGHT_MODE_AUTO, NightMode.UNRECOGNIZED -> 0
                NightMode.NIGHT_MODE_DAY -> 1
                NightMode.NIGHT_MODE_NIGHT -> 2
            }
            OutlinedTextField(
                value = darkThemeModes.getOrElse(selectionIndex) { "" },
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.dark_theme)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                darkThemeModes.forEachIndexed { index, text ->
                    DropdownMenuItem(
                        text = { Text(text) },
                        onClick = {
                            viewModel.onNightModeChanged(index)
                            isDropdownExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.interface_scale),
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            modifier = Modifier.padding(top = 16.dp, bottom = 2.dp)
        )
        OutlinedTextField(
            value = state.uiScale,
            onValueChange = viewModel::onUiScaleChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.interface_size)) },
            isError = state.isUiScaleInvalid,
            supportingText = {
                if (state.isUiScaleInvalid) {
                    Text(stringResource(R.string.invalid_value))
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Text(
            text = stringResource(R.string.interface_size_tip),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )

        Button(
            onClick = { context.start<UiPreviewActivity>() },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.view_preview))
        }
    }
}

@Composable
fun SettingSwitchItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = {})
    }
}