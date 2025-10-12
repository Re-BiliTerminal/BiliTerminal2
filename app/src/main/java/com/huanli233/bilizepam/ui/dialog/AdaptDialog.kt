package com.huanli233.bilizepam.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
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
    val fullScreenDisabled by LocalData.settingsStateFlow.map { it?.theme?.fullScreenDialogDisabled == true }.collectAsState(null)
    var showDialog by remember { mutableStateOf(true) }
    if (fullScreenDisabled == true && showDialog) {
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
    } else if (showDialog) {
        FullScreenAlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = confirmButton,
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
            properties = FullScreenDialogProperties(
                dismissOnBackPress = properties.dismissOnBackPress,
                dismissOnClickOutside = properties.dismissOnClickOutside
            )
        )
    }
}