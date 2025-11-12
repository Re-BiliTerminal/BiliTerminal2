package com.huanli233.bilizepam.ui.screens.image

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.isRoundDevice
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.TimeText
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.ui.components.WearTopBar
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.huanli233.bilizepam.utils.MsgUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@Composable
fun ImageViewerScreen(
    imageUrls: List<String>,
    initialPage: Int = 0,
    onNavigateBack: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { imageUrls.size }
    )
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    val isRound = isRoundDevice() && LocalData.settings.uiSettings.roundMode

    ScreenScaffold(
        modifier = Modifier.fillMaxSize(),
        timeText = if (isRound) { { TimeText() } } else null
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = pagerState.currentPage >= 0 || pagerState.currentPageOffsetFraction < 0
            ) { page ->
                ZoomableImageItem(imageUrl = imageUrls[page])
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { onNavigateBack() }
                    .padding(top = paddingValues.calculateTopPadding())
                    .padding(horizontal = if (isRound) 8.dp else 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White,
                            modifier = Modifier.size(if (isRound) 20.dp else 24.dp)
                        )
                        Spacer(modifier = Modifier.width(if (isRound) 4.dp else 8.dp))
                        Text(
                            text = "${pagerState.currentPage + 1} / ${imageUrls.size}",
                            color = Color.White,
                            style = if (isRound) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = paddingValues.calculateBottomPadding() + 16.dp)
                    .padding(end = 16.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        if (!isSaving) {
                            scope.launch {
                                isSaving = true
                                saveImage(context, imageUrls[pagerState.currentPage])
                                isSaving = false
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(if (isRound) 48.dp else 56.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "保存",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomableImageItem(imageUrl: String) {
    val zoomState = rememberZoomState()
    val context = LocalContext.current
    
    val processedUrl = if (imageUrl.startsWith("http")) imageUrl else "http:$imageUrl"

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(processedUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .zoomable(zoomState),
            contentScale = ContentScale.Fit
        )
    }
}

private suspend fun saveImage(context: Context, imageUrl: String) {
    withContext(Dispatchers.IO) {
        try {
            val fileName = "bilizepam_${System.currentTimeMillis()}.jpg"
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(picturesDir, "BiliZepam")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            
            val file = File(appDir, fileName)
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connect()
            
            connection.getInputStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            
            withContext(Dispatchers.Main) {
                MsgUtil.showMsg("图片已保存到: ${file.absolutePath}")
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                MsgUtil.showMsg("保存失败: ${e.message}")
            }
        }
    }
}
