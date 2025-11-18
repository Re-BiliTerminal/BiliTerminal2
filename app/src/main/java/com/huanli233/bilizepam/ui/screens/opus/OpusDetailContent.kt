package com.huanli233.bilizepam.ui.screens.opus

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.huanli233.biliwebapi.bean.opus.Opus
import com.huanli233.bilizepam.utils.extensions.formatNumber

@Composable
fun OpusDetailContent(
    opus: Opus,
    scrollState: ScrollState,
    padding: PaddingValues,
    onUserClick: (Long) -> Unit,
    onVideoClick: (String) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onLikeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(padding)
            .padding(horizontal = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        AuthorCard(
            author = opus.modules.moduleAuthor,
            onUserClick = onUserClick
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        opus.modules.moduleTop?.let { moduleTop ->
            TopModule(
                moduleTop = moduleTop,
                onImageClick = onImageClick
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        opus.modules.moduleTitle?.let { moduleTitle ->
            TitleModule(title = moduleTitle.text)
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        OpusContent(
            opus = opus,
            onUserClick = onUserClick,
            onVideoClick = onVideoClick,
            onImageClick = onImageClick
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        ActionBar(
            stat = opus.modules.moduleStat,
            onLikeClick = onLikeClick,
            onFavoriteClick = onFavoriteClick,
            onShareClick = onShareClick
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AuthorCard(
    author: com.huanli233.biliwebapi.bean.user.UserInfo,
    onUserClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick(author.mid) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(author.face)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = author.name.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                author.sign?.let { sign ->
                    if (sign.isNotEmpty()) {
                        Text(
                            text = sign,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionBar(
    stat: com.huanli233.biliwebapi.bean.opus.OpusStatModule,
    onLikeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionButton(
                icon = if (stat.like.status) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                text = stat.like.count.formatNumber(),
                onClick = onLikeClick,
                isActive = stat.like.status
            )
            
            ActionButton(
                icon = if (stat.favorite?.status == true) Icons.Filled.Star else Icons.Outlined.Star,
                text = stat.favorite?.count?.formatNumber() ?: "0",
                onClick = onFavoriteClick,
                isActive = stat.favorite?.status == true
            )
            
            ActionButton(
                icon = Icons.Outlined.Share,
                text = stat.forward.count.formatNumber(),
                onClick = onShareClick,
                isActive = false
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    isActive: Boolean = false
) {
    val tintColor = if (isActive) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = tintColor,
            fontSize = 10.sp
        )
    }
}

@Composable
fun TopModule(
    moduleTop: com.huanli233.biliwebapi.bean.opus.OpusTopModule,
    onImageClick: (List<String>, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            moduleTop.display.album.pics.forEachIndexed { index, pic ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pic.url)
                        .crossfade(200)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onImageClick(
                                moduleTop.display.album.pics.map { it.url },
                                index
                            )
                        },
                    contentScale = ContentScale.Fit
                )
                if (index < moduleTop.display.album.pics.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TitleModule(title: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
