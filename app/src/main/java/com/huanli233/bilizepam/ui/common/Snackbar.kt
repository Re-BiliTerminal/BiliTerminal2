package com.huanli233.bilizepam.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

enum class SnackbarDuration(val time: Long) {
    Short(2000L),
    Long(4000L)
}

data class SnackbarMessage(
    val message: String,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val id: String = UUID.randomUUID().toString()
)

object SnackbarManager {
    private val _messages = MutableStateFlow<SnackbarMessage?>(null)
    val message = _messages.asStateFlow()

    fun show(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        _messages.update { SnackbarMessage(message, duration) }
    }

    fun dismiss() {
        _messages.update { null }
    }
}

@Composable
fun CustomSnackbar(
    message: SnackbarMessage,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.6f),
        contentColor = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.6f),
        shadowElevation = 6.dp
    ) {
        Text(
            text = message.message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
        )
    }
}


@Composable
fun CustomSnackbarHost() {
    val snackbarMessage by SnackbarManager.message.collectAsState()

    LaunchedEffect(snackbarMessage?.id) {
        snackbarMessage?.let { message ->
            delay(message.duration.time)
            if (SnackbarManager.message.value?.id == message.id) {
                SnackbarManager.dismiss()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedContent(
            targetState = snackbarMessage,
            label = "SnackbarAnimation",
            transitionSpec = {
                val enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    initialOffsetY = { it }
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 220)
                ) + scaleIn(
                    initialScale = 0.9f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )

                val exit = slideOutVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    targetOffsetY = { it }
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 220)
                ) + scaleOut(
                    targetScale = 0.9f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )

                (enter).togetherWith(exit)
            }
        ) { message ->
            message?.let {
                CustomSnackbar(
                    message = it,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}