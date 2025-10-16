package com.huanli233.bilizepam.ui.screens.login

import android.graphics.Bitmap
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.utils.QRCodeUtil
import com.huanli233.bilizepam.utils.extensions.LoadState
import com.valentinilk.shimmer.shimmer

@Composable
fun QrCodeLoginScreen(
    viewModel: QrCodeLoginViewModel = hiltViewModel(),
    onNavigateToImport: () -> Unit,
    onLoginSuccess: () -> Unit,
    onSkip: () -> Unit,
) {
    val qrcodeState by viewModel.qrcodeState.collectAsState(LoadState.Loading())
    val loginPollState by viewModel.qrCodeLoginState.collectAsState()
    val needRefresh by viewModel.needRefresh.collectAsState()

    var qrScaleLevel by remember { mutableIntStateOf(1) }
    val qrScaleTarget = when (qrScaleLevel) {
        0 -> 0.7f
        1 -> 0.85f
        else -> 1.0f
    }
    val qrScale by animateFloatAsState(targetValue = qrScaleTarget, animationSpec = tween(500), label = "qrScale")

    val statusText = when {
        loginPollState.isSuccess -> {
            val data = (loginPollState as LoadState.Success).data
            if (data.finished) {
                LaunchedEffect(Unit) { onLoginSuccess() }
                stringResource(R.string.login_success)
            } else {
                when (data.code) {
                    -1 -> stringResource(R.string.login_qrcode_network_error)
                    0 -> stringResource(R.string.login_qrcode_logining)
                    86090 -> stringResource(R.string.login_qrcode_scanned)
                    86101 -> stringResource(R.string.login_qrcode_wating)
                    86038 -> stringResource(R.string.login_qrcode_expired)
                    else -> stringResource(R.string.login_qrcode_api_error)
                }
            }
        }
        qrcodeState.isLoading -> stringResource(R.string.requesting_qrcode)
        qrcodeState is LoadState.Error -> (qrcodeState as LoadState.Error).error.message ?: stringResource(R.string.login_qrcode_network_error)
        else -> stringResource(R.string.requesting_qrcode)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(qrScale)
                .aspectRatio(1f),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (val state = qrcodeState) {
                    is LoadState.Loading<*> -> CircularProgressIndicator()
                    is LoadState.Success -> {
                        val qrBitmap by produceState<Bitmap?>(initialValue = null, key1 = state.data) {
                            value = QRCodeUtil.createQRCodeBitmap(state.data, 320, 320)
                        }
                        Crossfade(qrBitmap) {
                            it?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(MaterialTheme.shapes.medium)
                                        .clickable {
                                            if (needRefresh) {
                                                viewModel.loadQrcode()
                                            } else {
                                                qrScaleLevel = (qrScaleLevel + 1) % 3
                                            }
                                        }
                                )
                            } ?: Box(Modifier.fillMaxSize().shimmer())
                        }
                    }
                    is LoadState.Error -> {
                        Image(
                            painter = painterResource(id = R.drawable.loading_2233_error),
                            contentDescription = stringResource(R.string.error),
                            modifier = Modifier.clickable { viewModel.loadQrcode() }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Crossfade(statusText, Modifier.animateContentSize()) {
            Text(
                text = it,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onNavigateToImport) {
            Text(stringResource(R.string.import_from_other_device))
        }
        Button(onClick = onSkip) { Text(stringResource(R.string.skip)) }
    }
}