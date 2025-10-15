package com.huanli233.bilizepam.ui.screens.recommend

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.valentinilk.shimmer.shimmer

@Composable
fun VideoCard(
    videoInfo: VideoInfo,
    onClick: (VideoInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = { onClick(videoInfo) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 封面缩略图
            Box(
                modifier = Modifier
                    .size(80.dp, 50.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                var isLoading by remember { mutableStateOf(true) }
                
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(videoInfo.pic)
                        .crossfade(200)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    onSuccess = { isLoading = false },
                    onError = { isLoading = false }
                )
                
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .shimmer()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 视频信息
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp)
            ) {
                // 标题 - 最多2行
                Text(
                    text = videoInfo.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // UP主和播放量 - 单行显示
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = videoInfo.owner.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = formatViews(videoInfo.stat.view),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatViews(views: Int): String {
    return when {
        views >= 10000 -> "${views / 10000}万"
        views >= 1000 -> "${views / 1000}千"
        else -> views.toString()
    }
}
