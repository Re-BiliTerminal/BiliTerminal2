package com.huanli233.bilizepam.ui.screens.setting

import android.app.Activity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.huanli233.bilizepam.ui.theme.AppSeedColors
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeColorScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settingsState.collectAsState()
    val currentThemeKey = settings?.theme?.colorTheme
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun formatThemeName(key: String): String {
        return key.split('_').joinToString(" ") { word ->
            word.lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(AppSeedColors.colorMap.keys.toList()) { themeKey ->
            val isSelected = currentThemeKey == themeKey
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                scope.launch {
                                    viewModel.updateColorTheme(themeKey)
                                    (context as? Activity)?.recreate()
                                }
                            }
                        },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null
                )
                Text(
                    text = formatThemeName(themeKey),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}