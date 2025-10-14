package com.huanli233.bilizepam.ui.dialog

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay

private const val DEFAULT_ANIMATION_DURATION = 250
private val ScrimColor = Color.Black.copy(alpha = 0.45f)

@Immutable
class AnimatedDialogProperties(
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
    val backgroundDimEnabled: Boolean = false,
    val alignment: Alignment = Alignment.Center,
    val animation: FullScreenDialogAnimation = FullScreenDialogAnimation.Material3Dialog()
)

@Composable
fun BasicAnimatedAlertDialog(
    onDismissRequest: () -> Unit,
    properties: AnimatedDialogProperties = AnimatedDialogProperties(),
    content: @Composable (close: () -> Unit) -> Unit,
) {
    var showDialog by remember { mutableStateOf(true) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        awaitFrame()
        isVisible = true
    }

    fun dismiss() {
        isVisible = false
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = { dismiss() },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            val scrimColor by animateColorAsState(
                targetValue = if (isVisible && properties.backgroundDimEnabled) ScrimColor else Color.Transparent,
                animationSpec = tween(300),
                label = "scrimColor"
            )

            val transition = updateTransition(targetState = isVisible, label = "DialogTransition")
            val scale by transition.animateFloat(
                transitionSpec = {
                    if (targetState) tween(300, easing = FastOutSlowInEasing)
                    else tween(200, easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f))
                }, label = "scale"
            ) { if (it) 1f else 0.8f }
            val alpha by transition.animateFloat(
                transitionSpec = {
                    if (targetState) tween(300)
                    else tween(200)
                }, label = "alpha"
            ) { if (it) 1f else 0f }
            val offsetY by transition.animateDp(
                transitionSpec = {
                    if (targetState) tween(300, easing = FastOutSlowInEasing)
                    else tween(200, easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f))
                }, label = "offsetY"
            ) { if (it) 0.dp else 12.dp }

            Box(
                Modifier
                    .fillMaxSize()
            ) {
                Box(
                    Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {
                            if (properties.dismissOnClickOutside) {
                                detectTapGestures { dismiss() }
                            }
                        }
                )
                Box(
                    modifier = Modifier
                        .align(properties.alignment)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                            translationY = offsetY.toPx()
                        }
                ) {
                    content(::dismiss)
                }
            }

            LaunchedEffect(isVisible) {
                if (!isVisible) {
                    delay(DEFAULT_ANIMATION_DURATION.toLong())
                    showDialog = false
                    onDismissRequest()
                }
            }

            BackHandler(enabled = properties.dismissOnBackPress) {
                dismiss()
            }
        }
    }
}

@Composable
fun AnimatedAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable (close: () -> Unit) -> Unit,
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
    properties: AnimatedDialogProperties = AnimatedDialogProperties()
) {
    BasicAnimatedAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) { close ->
        Surface(
            modifier = modifier.sizeIn(minWidth = 280.dp, maxWidth = 560.dp),
            shape = shape,
            color = containerColor,
            tonalElevation = tonalElevation
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
            ) {
                icon?.let {
                    CompositionLocalProvider(LocalContentColor provides iconContentColor) {
                        Box(Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)) { icon() }
                    }
                }
                title?.let {
                    ProvideContentColorTextStyle(
                        contentColor = titleContentColor,
                        textStyle = MaterialTheme.typography.headlineSmall
                    ) {
                        Box(
                            Modifier
                                .padding(bottom = 16.dp)
                                .align(if (icon == null) Alignment.Start else Alignment.CenterHorizontally)
                        ) { title() }
                    }
                }
                text?.let {
                    ProvideContentColorTextStyle(
                        contentColor = textContentColor,
                        textStyle = MaterialTheme.typography.bodyMedium
                    ) {
                        Box(
                            Modifier
                                .weight(weight = 1f, fill = false)
                                .padding(bottom = 24.dp)
                                .align(Alignment.Start)
                        ) { text() }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    dismissButton?.invoke()
                    confirmButton(close)
                }
            }
        }
    }
}
