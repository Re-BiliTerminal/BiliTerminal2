package com.huanli233.bilizepam.ui.screens.setup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.data.proto.NightMode
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.data.setting.edit
import com.huanli233.bilizepam.ui.activity.setup.UiPreviewActivity
import com.huanli233.bilizepam.ui.dialog.AdaptDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import splitties.activities.start

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
                theme = theme.edit {
                    animationsEnabled = currentState.animationsEnabled
                    nightMode = currentState.nightMode
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

@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: UiSetupViewModel = viewModel()
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentStep) {
            0 -> WelcomeStep(onNext = { currentStep = 1 })
            1 -> UiSetupStep(
                state = state,
                viewModel = viewModel,
                onNext = onSetupComplete
            )
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)

    ScreenScaffold(
        scrollState = scrollState,
        topBar = scrollAwareTopBar(
            title = stringResource(R.string.welcome),
            showBackIcon = false
        )
    ) { paddingValues ->
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            contentPadding = paddingValues
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.setup_introduction),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            item {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(stringResource(R.string.next))
                }
            }
        }
    }
}

@Composable
private fun UiSetupStep(
    state: UiSetupState,
    viewModel: UiSetupViewModel,
    onNext: () -> Unit
) {
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    val context = LocalContext.current
    val darkThemeModes = remember { context.resources.getStringArray(R.array.dark_theme_modes) }

    ScreenScaffold(
        scrollState = scrollState,
        topBar = scrollAwareTopBar(
            title = stringResource(R.string.initialize_setting),
            showBackIcon = false
        )
    ) { paddingValues ->
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            contentPadding = paddingValues
        ) {

            item {
                val selectionIndex = when (state.nightMode) {
                    NightMode.NIGHT_MODE_AUTO, NightMode.UNRECOGNIZED -> 0
                    NightMode.NIGHT_MODE_DAY -> 1
                    NightMode.NIGHT_MODE_NIGHT -> 2
                }
                var showDialog by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDialog = true }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dark_theme),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = darkThemeModes.getOrElse(selectionIndex) { "" },
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (showDialog) {
                    AdaptDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(stringResource(R.string.dark_theme)) },
                        text = {
                            Column {
                                darkThemeModes.forEachIndexed { index, mode ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.onNightModeChanged(index)
                                                showDialog = false
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = index == selectionIndex,
                                            onClick = {
                                                viewModel.onNightModeChanged(index)
                                                showDialog = false
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = mode)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.interface_scale),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = state.uiScale,
                        onValueChange = viewModel::onUiScaleChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        isError = state.isUiScaleInvalid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    if (state.isUiScaleInvalid) {
                        Text(
                            text = stringResource(R.string.invalid_value),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { context.start<UiPreviewActivity>() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(stringResource(R.string.view_preview))
                }
            }

            item {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        }
    }
}
