package com.huanli233.bilizepam.ui.screens.image

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.huanli233.bilizepam.utils.MsgUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("${pagerState.currentPage + 1} / ${imageUrls.size}")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (!isSaving) {
                                scope.launch {
                                    isSaving = true
                                    saveImage(context, imageUrls[pagerState.currentPage])
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Download, contentDescription = "保存")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            ZoomableImageItem(imageUrl = imageUrls[page])
        }
    }
}

@Composable
private fun ZoomableImageItem(imageUrl: String) {
    val zoomableState = rememberZoomableImageState()
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ZoomableAsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            state = zoomableState,
            modifier = Modifier.fillMaxSize(),
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
