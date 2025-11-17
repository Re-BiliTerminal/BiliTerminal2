package com.huanli233.bilizepam.ui.screens.dynamic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.huanli233.biliwebapi.bean.dynamic.Dynamic
import com.huanli233.bilizepam.ui.components.RichText
import com.huanli233.bilizepam.ui.components.VideoCard
import com.valentinilk.shimmer.shimmer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DynamicCard(
    dynamic: Dynamic,
    onClick: () -> Unit,
    onUserClick: (Long) -> Unit = {},
    onVideoClick: (String) -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onLikeClick: ((String, Boolean) -> Unit)? = null,
    onDynamicClick: (Dynamic) -> Unit = {},
    showFullContent: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onUserClick(dynamic.modules.authorModule.mid) }
                ) {
                    var isAvatarLoading by remember { mutableStateOf(true) }
                    
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(dynamic.modules.authorModule.face)
                            .crossfade(200)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                        onSuccess = { isAvatarLoading = false },
                        onError = { isAvatarLoading = false }
                    )
                    
                    if (isAvatarLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .shimmer()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dynamic.modules.authorModule.name.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = dynamic.modules.authorModule.pubAction ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            dynamic.modules.contentModule.desc?.let { desc ->
                if (desc.text.isNotBlank()) {
                    val atList = desc.richTextNodes
                        .filter { it.type == "RICH_TEXT_NODE_TYPE_AT" }
                        .mapNotNull { node ->
                            node.text.removePrefix("@").let { name ->
                                node.rid?.let { mid ->
                                    name to mid
                                }
                            }
                        }
                    
                    RichText(
                        text = desc.text,
                        emotes = desc.richTextNodes.mapNotNull { it.emoji }.associateBy { it.text },
                        atList = atList,
                        style = MaterialTheme.typography.bodyMedium,
                        onUserClick = onUserClick,
                        onBvidClick = onVideoClick,
                        onAvidClick = { aid -> onVideoClick("av$aid") },
                        onCvidClick = { cvid -> /* TODO: 跳转到文章 */ },
                        onUrlClick = { url -> /* TODO: 处理URL */ }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            dynamic.modules.contentModule.major?.let { major ->
                when (major.type) {
                    "MAJOR_TYPE_ARCHIVE" -> {
                        major.archive?.let { archive ->
                            val author = dynamic.modules.authorModule.let { author ->
                                com.huanli233.biliwebapi.bean.user.UserInfo(
                                    mid = author.mid,
                                    title = null,
                                    name = author.name,
                                    face = author.face,
                                    vip = null,
                                    official = com.huanli233.biliwebapi.bean.user.Official(
                                        role = 0,
                                        title = "",
                                        desc = "",
                                        type = -1
                                    ),
                                    follower = 0,
                                    sex = "",
                                    sign = null,
                                    rank = 0,
                                    level = 0,
                                    silence = 0,
                                    coins = 0,
                                    fansBadge = false,
                                    fansMedal = null,
                                    pendant = com.huanli233.biliwebapi.bean.user.Pendant(
                                        pid = 0,
                                        name = "",
                                        image = "",
                                        expire = 0,
                                    ),
                                    nameplate = com.huanli233.biliwebapi.bean.user.Nameplate(
                                        nid = 0,
                                        name = "",
                                        image = "",
                                        imageSmall = "",
                                        level = "",
                                        condition = ""
                                    ),
                                    isFollowed = false,
                                    topPhoto = "",
                                    systemNotice = null,
                                    liveRoom = null,
                                    series = com.huanli233.biliwebapi.bean.user.SeriesStatus(
                                        userUpgradeStatus = 0,
                                        showUpgradeWindow = false
                                    ),
                                    isSeniorMember = 0,
                                    contract = com.huanli233.biliwebapi.bean.user.ContractStatus(
                                        isDisplay = false,
                                        isFollowDisplay = false
                                    ),
                                    school = com.huanli233.biliwebapi.bean.user.School(name = "")
                                )
                            }
                            VideoCard(
                                videoInfo = archive.toVideoInfo(author),
                                onClick = { onVideoClick(it.bvid) },
                                modifier = Modifier
                            )
                        }
                    }
                    "MAJOR_TYPE_DRAW" -> {
                        major.opus?.let { opus ->
                            if (opus.pics.isNotEmpty()) {
                                OpusImageGrid(
                                    pics = opus.pics,
                                    onImageClick = { index -> 
                                        onImageClick(opus.pics.map { it.url }, index)
                                    }
                                )
                            }
                        }
                    }
                    "MAJOR_TYPE_OPUS" -> {
                        major.opus?.let { opus ->
                            OpusMajorContent(
                                opus = opus,
                                onImageClick = { index ->
                                    onImageClick(opus.pics.map { it.url }, index)
                                },
                                onUserClick = onUserClick,
                                onVideoClick = onVideoClick
                            )
                        }
                    }
                }
            }
            
            dynamic.origin?.let { originDynamic ->
                Spacer(modifier = Modifier.height(8.dp))
                OriginDynamicCard(
                    origin = originDynamic,
                    onClick = { onDynamicClick(originDynamic) }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val isLiked = dynamic.modules.statsModule.like.status
                val likeCount = dynamic.modules.statsModule.like.count
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = if (onLikeClick != null) {
                        Modifier
                            .clickable { 
                                onLikeClick(dynamic.id, isLiked)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    } else Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCount(likeCount.toLong()),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCount((dynamic.modules.statsModule.forward?.count ?: 0).toLong()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageMajorContent(imageUrl: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        var isLoading by remember { mutableStateOf(true) }
        
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(if (imageUrl.startsWith("http")) imageUrl else "http:$imageUrl")
                .crossfade(200)
                .build(),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
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
}

@Composable
private fun OpusMajorContent(
    opus: com.huanli233.biliwebapi.bean.opus.DynamicOpus,
    onImageClick: (Int) -> Unit = {},
    onUserClick: (Long) -> Unit = {},
    onVideoClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opus.summary?.let { summary ->
            if (summary.text.isNotBlank()) {
                val atList = summary.richTextNodes
                    .filter { it.type == "RICH_TEXT_NODE_TYPE_AT" }
                    .mapNotNull { node ->
                        node.text.removePrefix("@").let { name ->
                            node.rid?.let { mid ->
                                name to mid
                            }
                        }
                    }
                
                RichText(
                    text = summary.text,
                    emotes = summary.richTextNodes.mapNotNull { it.emoji }.associateBy { it.text },
                    atList = atList,
                    style = MaterialTheme.typography.bodyMedium,
                    onUserClick = onUserClick,
                    onBvidClick = onVideoClick,
                    onAvidClick = { aid -> onVideoClick("av$aid") },
                    onCvidClick = { cvid -> /* TODO: 跳转到文章 */ },
                    onUrlClick = { url -> /* TODO: 处理URL */ }
                )
            }
        }
        
        if (opus.pics.isNotEmpty()) {
            OpusImageGrid(
                pics = opus.pics,
                onImageClick = onImageClick
            )
        }
    }
}

@Composable
private fun OpusImageGrid(
    pics: List<com.huanli233.biliwebapi.bean.opus.OpusPicture>,
    onImageClick: (Int) -> Unit = {}
) {
    val imageCount = pics.size
    
    when {
        imageCount == 1 -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onImageClick(0) }
            ) {
                ImageMajorContent(imageUrl = pics[0].url)
            }
        }
        imageCount == 2 -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                pics.forEachIndexed { index, pic ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { onImageClick(index) }
                    ) {
                        AsyncImage(
                            model = pic.url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
        imageCount == 3 -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clickable { onImageClick(0) }
                ) {
                    AsyncImage(
                        model = pics[0].url,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..2) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clickable { onImageClick(i) }
                        ) {
                            AsyncImage(
                                model = pics[i].url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
        imageCount == 4 -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (row in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (col in 0..1) {
                            val index = row * 2 + col
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable { onImageClick(index) }
                            ) {
                                AsyncImage(
                                    model = pics[index].url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
        imageCount >= 5 -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val rows = (imageCount + 2) / 3
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val startIdx = row * 3
                        val endIdx = minOf(startIdx + 3, imageCount)
                        for (col in startIdx until endIdx) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable { onImageClick(col) }
                            ) {
                                AsyncImage(
                                    model = pics[col].url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        repeat(3 - (endIdx - startIdx)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OriginDynamicCard(
    origin: com.huanli233.biliwebapi.bean.dynamic.Dynamic,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "@${origin.modules.authorModule.name}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            origin.modules.contentModule.desc?.let { desc ->
                if (desc.text.isNotBlank()) {
                    Text(
                        text = desc.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            origin.modules.contentModule.major?.let { major ->
                when (major.type) {
                    "MAJOR_TYPE_ARCHIVE" -> {
                        major.archive?.let { video ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .aspectRatio(16f / 9f)
                                ) {
                                    AsyncImage(
                                        model = video.cover,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Text(
                                    text = video.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    "MAJOR_TYPE_DRAW", "MAJOR_TYPE_OPUS" -> {
                        major.opus?.let { opus ->
                            if (opus.pics.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    opus.pics.take(3).forEach { pic ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                        ) {
                                            AsyncImage(
                                                model = pic.url,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                    if (opus.pics.size < 3) {
                                        repeat(3 - opus.pics.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                            opus.summary?.let { summary ->
                                if (summary.text.isNotBlank()) {
                                    Text(
                                        text = summary.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatCount(count: Long): String {
    return when {
        count >= 100000000 -> String.format("%.1f亿", count / 100000000.0)
        count >= 10000 -> String.format("%.1f万", count / 10000.0)
        else -> count.toString()
    }
}
