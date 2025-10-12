package com.huanli233.bilizepam.ui.dialog

import android.R
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.Window
import android.view.WindowManager
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
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.core.view.WindowCompat
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay

private const val DEFAULT_ANIMATION_DURATION = 250
private val ScrimColor = Color.Black.copy(alpha = 0.45f)

@Immutable
sealed class FullScreenDialogAnimation(
    val enter: EnterTransition,
    val exit: ExitTransition
) {
    data class Material3Dialog(val durationMillis: Int = 300) : FullScreenDialogAnimation(
        enter = fadeIn(animationSpec = tween(durationMillis)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(animationSpec = tween(200, easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)))
    )
}

@Immutable
class FullScreenDialogProperties(
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
    val securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
    val isAppearanceLightNavigationBars: Boolean = true,
    val backgroundDimEnabled: Boolean = true,
    val alignment: Alignment = Alignment.Center,
    val animation: FullScreenDialogAnimation = FullScreenDialogAnimation.Material3Dialog()
)

@Composable
fun BasicFullScreenDialog(
    onDismissRequest: () -> Unit,
    properties: FullScreenDialogProperties = FullScreenDialogProperties(),
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
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                securePolicy = properties.securePolicy
            )
        ) {
            val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
            val parentView = LocalView.current.parent as View
            val activityWindow = LocalView.current.context.getActivityWindow()

            SideEffect {
                if (dialogWindow != null && activityWindow != null) {
                    dialogWindow.apply {
                        attributes = WindowManager.LayoutParams().apply {
                            copyFrom(activityWindow.attributes)
                            type = attributes.type
                        }
                        setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                        setBackgroundDrawableResource(R.color.transparent)
                        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                        statusBarColor = Color.Transparent.toArgb()
                        navigationBarColor = Color.Transparent.toArgb()
                        WindowCompat.getInsetsController(this, parentView).apply {
                            isAppearanceLightStatusBars = properties.isAppearanceLightNavigationBars
                            isAppearanceLightNavigationBars = properties.isAppearanceLightNavigationBars
                        }
                    }
                }
            }

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
                Modifier.fillMaxSize().background(scrimColor)
            ) {
                Box(
                    Modifier.matchParentSize().pointerInput(Unit) {
                        if (properties.dismissOnClickOutside) {
                            detectTapGestures { dismiss() }
                        }
                    }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
fun FullScreenAlertDialog(
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
    properties: FullScreenDialogProperties = FullScreenDialogProperties()
) {
    BasicFullScreenDialog(onDismissRequest = onDismissRequest, properties = properties) { close ->
        Surface(
            modifier = modifier.fillMaxSize(),
            shape = shape,
            color = containerColor,
            tonalElevation = tonalElevation
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .safeContentPadding()
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

private tailrec fun Context.getActivityWindow(): Window? = when (this) {
    is Activity -> window
    is ContextWrapper -> baseContext.getActivityWindow()
    else -> null
}

@Composable
internal fun ProvideContentColorTextStyle(
    contentColor: Color,
    textStyle: TextStyle,
    content: @Composable () -> Unit,
) {
    val mergedStyle = LocalTextStyle.current.merge(textStyle)
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalTextStyle provides mergedStyle,
        content = content
    )
}
