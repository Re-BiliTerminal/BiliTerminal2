package com.huanli233.bilizepam.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AlertDialog
import com.huanli233.bilizepam.data.setting.LocalData
import kotlinx.coroutines.flow.map

@Composable
fun AdaptDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable (() -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties()
) {
    val fullScreenDisabled by remember { LocalData.settingsStateFlow.map { it?.theme?.fullScreenDialogDisabled == true } }.collectAsState(null)
    
    if (fullScreenDisabled == true) {
        var showDialog by remember { mutableStateOf(true) }
        if (showDialog) {
            AnimatedAlertDialog(
                onDismissRequest = onDismissRequest,
                confirmButton = { confirmButton { showDialog = false; onDismissRequest() } },
                modifier = modifier,
                dismissButton = dismissButton,
                icon = icon,
                title = title,
                text = text,
                shape = shape,
                containerColor = containerColor,
                iconContentColor = iconContentColor,
                titleContentColor = titleContentColor,
                textContentColor = textContentColor,
                tonalElevation = tonalElevation,
                properties = AnimatedDialogProperties(
                    dismissOnBackPress = properties.dismissOnBackPress,
                    dismissOnClickOutside = properties.dismissOnClickOutside,
                )
            )
        }
    } else {
        AlertDialog(
            visible = true,
            onDismissRequest = onDismissRequest,
            confirmButton = { confirmButton(onDismissRequest) },
            title = { title?.invoke() ?: Spacer(Modifier.width(0.dp)) },
            dismissButton = { dismissButton?.let { it() } },
            icon = icon,
            text = text
        )
    }
}