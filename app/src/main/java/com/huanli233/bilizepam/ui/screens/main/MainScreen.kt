package com.huanli233.bilizepam.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.navigation.navArgument
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.account.AccountManager
import com.huanli233.bilizepam.data.menu.MenuConfigManager
import com.huanli233.bilizepam.ui.components.menu.MenuPanel
import com.huanli233.bilizepam.ui.navigation.NavGraph
import com.huanli233.bilizepam.ui.navigation.Screen
import com.huanli233.bilizepam.ui.navigation.loginGraph
import com.huanli233.bilizepam.ui.navigation.settingsGraph
import com.huanli233.bilizepam.ui.screens.collection.CollectionDetailScreen
import com.huanli233.bilizepam.ui.screens.comment.CommentDetailScreen
import com.huanli233.bilizepam.ui.screens.download.DownloadListScreen
import com.huanli233.bilizepam.ui.screens.dynamic.DynamicDetailScreen
import com.huanli233.bilizepam.ui.screens.dynamic.DynamicHomeScreen
import com.huanli233.bilizepam.ui.screens.favorite.FavoriteScreen
import com.huanli233.bilizepam.ui.screens.favorite.FavoriteVideosScreen
import com.huanli233.bilizepam.ui.screens.favorite.OpusFavoriteScreen
import com.huanli233.bilizepam.ui.screens.follow.FollowingScreen
import com.huanli233.bilizepam.ui.screens.history.HistoryScreen
import com.huanli233.bilizepam.ui.screens.image.ImageViewerScreen
import com.huanli233.bilizepam.ui.screens.watchlater.WatchLaterScreen
import com.huanli233.bilizepam.ui.screens.opus.OpusDetailScreen
import com.huanli233.bilizepam.ui.screens.player.PlayerScreen
import com.huanli233.bilizepam.ui.screens.recommend.RecommendScreen
import com.huanli233.bilizepam.ui.screens.popular.PopularScreen
import com.huanli233.bilizepam.ui.screens.precious.PreciousScreen
import com.huanli233.bilizepam.ui.screens.bangumi.BangumiDetailScreen
import com.huanli233.bilizepam.ui.screens.series.SeriesDetailScreen
import com.huanli233.bilizepam.ui.screens.user.MySpaceScreen
import com.huanli233.bilizepam.ui.screens.user.UserProfileScreen
import com.huanli233.bilizepam.ui.screens.video.VideoDetailScreen
import com.huanli233.bilizepam.ui.screens.comment.WriteReplyScreen
import com.huanli233.bilizepam.ui.screens.search.SearchScreen
import com.huanli233.bilizepam.ui.screens.search.SearchResultScreen
import java.net.URLDecoder

@Composable
fun MainScreen(mainNavController: androidx.navigation.NavController) {
    val contentNavController = rememberSwipeDismissableNavController()
    val menuConfig by remember { mutableStateOf(MenuConfigManager.readMenuConfig()) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    
    val menuGestureBlocker = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (abs(available.x) > abs(available.y)) {
                    Offset(available.x, 0f)
                } else {
                    Offset.Zero
                }
            }
            
            override suspend fun onPreFling(available: Velocity): Velocity {
                return if (abs(available.x) > abs(available.y)) {
                    Velocity(available.x, 0f)
                } else {
                    Velocity.Zero
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SwipeDismissableNavHost(
            navController = contentNavController,
            startDestination = Screen.Recommend.route
        ) {
            composable(Screen.Recommend.route) {
                RecommendScreen(
                    onVideoClick = { videoInfo ->
                        contentNavController.navigate(
                            Screen.VideoDetail.createRoute(videoInfo.aid, videoInfo.bvid)
                        )
                    },
                    onMenuClick = { isMenuExpanded = !isMenuExpanded },
                    onPopularClick = { contentNavController.navigate("popular") },
                    onPreciousClick = { contentNavController.navigate("precious") }
                )
            }

            composable("popular") {
                PopularScreen(
                    onVideoClick = { videoInfo ->
                        contentNavController.navigate(
                            Screen.VideoDetail.createRoute(videoInfo.aid, videoInfo.bvid)
                        )
                    },
                    onNavigateBack = { contentNavController.popBackStack() }
                )
            }

            composable("precious") {
                PreciousScreen(
                    onVideoClick = { videoInfo ->
                        contentNavController.navigate(
                            Screen.VideoDetail.createRoute(videoInfo.aid, videoInfo.bvid)
                        )
                    },
                    onNavigateBack = { contentNavController.popBackStack() }
                )
            }

            composable(
                route = "bangumi/{mediaId}",
                arguments = listOf(
                    navArgument("mediaId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val mediaId = backStackEntry.arguments?.getLong("mediaId") ?: 0L
                BangumiDetailScreen(
                    mediaId = mediaId,
                    onNavigateBack = { contentNavController.popBackStack() },
                    onPlayEpisode = { aid, cid ->
                        contentNavController.navigate("video_detail/$aid/")
                    }
                )
            }

            composable(
                route = "bangumi_from_ep/{epId}",
                arguments = listOf(
                    navArgument("epId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val epId = backStackEntry.arguments?.getLong("epId") ?: 0L
                val bangumiRepository = remember { com.huanli233.bilizepam.data.repository.BangumiRepository() }
                var mediaId by remember { mutableStateOf<Long?>(null) }
                var error by remember { mutableStateOf<String?>(null) }
                
                LaunchedEffect(epId) {
                    bangumiRepository.getMediaIdFromEpId(epId).fold(
                        onSuccess = { id -> mediaId = id },
                        onFailure = { e -> error = e.message }
                    )
                }
                
                when {
                    mediaId != null -> {
                        BangumiDetailScreen(
                            mediaId = mediaId!!,
                            onNavigateBack = { contentNavController.popBackStack() },
                            onPlayEpisode = { aid, cid ->
                                contentNavController.navigate("video_detail/$aid/")
                            }
                        )
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("加载失败: $error")
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            composable(
                route = "series/{type}/{mid}/{id}/{name}",
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("mid") { type = NavType.LongType },
                    navArgument("id") { type = NavType.LongType },
                    navArgument("name") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val seriesType = backStackEntry.arguments?.getString("type") ?: "series"
                val mid = backStackEntry.arguments?.getLong("mid") ?: 0L
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                val name = backStackEntry.arguments?.getString("name") ?: ""
                
                SeriesDetailScreen(
                    type = seriesType,
                    mid = mid,
                    id = id,
                    name = URLDecoder.decode(name, "UTF-8"),
                    onNavigateBack = { contentNavController.popBackStack() },
                    onVideoClick = { videoInfo ->
                        contentNavController.navigate(
                            Screen.VideoDetail.createRoute(videoInfo.aid, videoInfo.bvid)
                        )
                    }
                )
            }

            composable(Screen.Dynamic.route) {
                DynamicHomeScreen(
                    onDynamicClick = { dynamic ->
                        val major = dynamic.modules.contentModule.major
                        when {
                            major?.type == "MAJOR_TYPE_OPUS" && major.opus != null -> {
                                val opusId = dynamic.id
                                contentNavController.navigate(
                                    Screen.OpusDetail.createRoute(opusId)
                                )
                            }
                            else -> {
                                contentNavController.navigate(
                                    Screen.DynamicDetail.createRoute(dynamic.id)
                                )
                            }
                        }
                    },
                    onUserClick = { mid ->
                        contentNavController.navigate("user/$mid")
                    },
                    onVideoClick = { bvid ->
                        contentNavController.navigate("video_detail/0/$bvid")
                    },
                    onImageClick = { imageUrls, initialPage ->
                        val encodedUrls = imageUrls.joinToString(",") { java.net.URLEncoder.encode(it, "UTF-8") }
                        contentNavController.navigate("imageViewer/$encodedUrls/$initialPage")
                    },
                    onMenuClick = { isMenuExpanded = !isMenuExpanded }
                )
            }

            composable(
                route = Screen.VideoDetail.route,
                arguments = listOf(
                    navArgument("avid") { type = NavType.LongType },
                    navArgument("bvid") { type = NavType.StringType }
                )
            ) {
                VideoDetailScreen(navController = contentNavController)
            }

            composable(
                route = Screen.DynamicDetail.route,
                arguments = listOf(
                    navArgument("dynamicId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val dynamicId = backStackEntry.arguments?.getString("dynamicId") ?: ""
                DynamicDetailScreen(
                    dynamicId = dynamicId,
                    navController = contentNavController,
                    onNavigateBack = { contentNavController.popBackStack() },
                    onUserClick = { mid ->
                        contentNavController.navigate("user/$mid")
                    },
                    onVideoClick = { bvid ->
                        contentNavController.navigate("video_detail/0/$bvid")
                    },
                    onImageClick = { imageUrls, initialPage ->
                        val encodedUrls = imageUrls.joinToString(",") { java.net.URLEncoder.encode(it, "UTF-8") }
                        contentNavController.navigate("imageViewer/$encodedUrls/$initialPage")
                    },
                    onDynamicClick = { dynamic ->
                        val major = dynamic.modules.contentModule.major
                        when {
                            major?.type == "MAJOR_TYPE_OPUS" && major.opus != null -> {
                                val opusId = dynamic.id
                                contentNavController.navigate(
                                    Screen.OpusDetail.createRoute(opusId)
                                )
                            }
                            else -> {
                                contentNavController.navigate(
                                    Screen.DynamicDetail.createRoute(dynamic.id)
                                )
                            }
                        }
                    }
                )
            }

            composable(
                route = Screen.OpusDetail.route,
                arguments = listOf(
                    navArgument("opusId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val opusId = backStackEntry.arguments?.getString("opusId") ?: ""
                OpusDetailScreen(
                    opusId = opusId,
                    onNavigateBack = { contentNavController.popBackStack() },
                    onUserClick = { mid ->
                        contentNavController.navigate("user/$mid")
                    },
                    onVideoClick = { bvid ->
                        contentNavController.navigate("video_detail/0/$bvid")
                    },
                    onImageClick = { imageUrls, initialPage ->
                        val encodedUrls = imageUrls.joinToString(",") { java.net.URLEncoder.encode(it, "UTF-8") }
                        contentNavController.navigate("imageViewer/$encodedUrls/$initialPage")
                    },
                    onCommentDetailClick = { replyId, oid ->
                        contentNavController.navigate("comment_detail/$replyId?oid=$oid&type=11")
                    },
                    onWriteReplyClick = { oid, rpid, parent, parentSender ->
                        contentNavController.navigate("write_reply/$oid/$rpid/$parent?parentSender=${parentSender ?: ""}")
                    }
                )
            }

            composable(
                route = "comment_detail/{replyId}?oid={oid}&type={type}",
                arguments = listOf(
                    navArgument("replyId") { type = NavType.LongType },
                    navArgument("oid") { 
                        type = NavType.LongType
                        defaultValue = -1
                    },
                    navArgument("type") { 
                        type = NavType.IntType
                        defaultValue = 1
                    }
                )
            ) { backStackEntry ->
                val replyId = backStackEntry.arguments?.getLong("replyId") ?: 0L
                val oidArg = backStackEntry.arguments?.getLong("oid") ?: -1L
                val oid = if (oidArg == -1L) null else oidArg
                val type = backStackEntry.arguments?.getInt("type") ?: 1
                CommentDetailScreen(
                    replyId = replyId,
                    oid = oid ?: 0L,
                    type = type,
                    onBackClick = { contentNavController.popBackStack() },
                    onWriteReplyClick = { oid, rpid, parent, parentSender ->
                        contentNavController.navigate("write_reply/$oid/$rpid/$parent?parentSender=${parentSender ?: ""}")
                    },
                    onUserClick = { userId ->
                        contentNavController.navigate("user/$userId")
                    },
                    onOpusClick = { opusId ->
                        contentNavController.navigate("opus_detail/$opusId")
                    },
                    onVideoClick = { aid, bvid ->
                        contentNavController.navigate("video_detail/$aid/$bvid")
                    }
                )
            }

            composable(
                route = "write_reply/{oid}/{rpid}/{parent}?parentSender={parentSender}",
                arguments = listOf(
                    navArgument("oid") { type = NavType.LongType },
                    navArgument("rpid") { type = NavType.LongType },
                    navArgument("parent") { type = NavType.LongType },
                    navArgument("parentSender") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val oid = backStackEntry.arguments?.getLong("oid") ?: 0L
                val rpid = backStackEntry.arguments?.getLong("rpid") ?: 0L
                val parent = backStackEntry.arguments?.getLong("parent") ?: 0L
                val parentSender = backStackEntry.arguments?.getString("parentSender")
                WriteReplyScreen(
                    oid = oid,
                    rpid = rpid,
                    parent = parent,
                    parentSender = parentSender,
                    onBackClick = { contentNavController.popBackStack() },
                    onReplySuccess = { 
                        // 回复成功后返回上一页
                        contentNavController.popBackStack()
                    }
                )
            }

            composable(
                route = "image/{imageUrl}/{initialPage}",
                arguments = listOf(
                    navArgument("imageUrl") { type = NavType.StringType },
                    navArgument("initialPage") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
                val imageUrl = URLDecoder.decode(encodedUrl, "UTF-8")
                val initialPage = backStackEntry.arguments?.getInt("initialPage") ?: 0
                ImageViewerScreen(
                    imageUrls = listOf(imageUrl),
                    initialPage = initialPage,
                    onNavigateBack = { contentNavController.popBackStack() }
                )
            }

            composable(
                route = "imageViewer/{imageUrls}/{initialPage}",
                arguments = listOf(
                    navArgument("imageUrls") { type = NavType.StringType },
                    navArgument("initialPage") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val encodedUrls = backStackEntry.arguments?.getString("imageUrls") ?: ""
                val imageUrls = encodedUrls.split(",").map { URLDecoder.decode(it, "UTF-8") }
                val initialPage = backStackEntry.arguments?.getInt("initialPage") ?: 0
                ImageViewerScreen(
                    imageUrls = imageUrls,
                    initialPage = initialPage,
                    onNavigateBack = { contentNavController.popBackStack() }
                )
            }

            composable(
                route = "user/{mid}",
                arguments = listOf(
                    navArgument("mid") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val mid = backStackEntry.arguments?.getLong("mid") ?: 0
                UserProfileScreen(
                    mid = mid,
                    onNavigateBack = { contentNavController.popBackStack() },
                    onDynamicClick = { dynamicId ->
                        contentNavController.navigate("dynamic_detail/$dynamicId")
                    },
                    onVideoClick = { videoInfo ->
                        contentNavController.navigate(
                            Screen.VideoDetail.createRoute(videoInfo.aid, videoInfo.bvid)
                        )
                    },
                    onOpusClick = { opusId ->
                        contentNavController.navigate("opus_detail/$opusId")
                    },
                    onImageClick = { imageUrls, initialPage ->
                        val encodedUrls = imageUrls.joinToString(",") { java.net.URLEncoder.encode(it, "UTF-8") }
                        contentNavController.navigate("imageViewer/$encodedUrls/$initialPage")
                    },
                    onSeriesClick = { type, mid, id, name ->
                        val encodedName = java.net.URLEncoder.encode(name, "UTF-8")
                        contentNavController.navigate("series/$type/$mid/$id/$encodedName")
                    }
                )
            }

            composable(
                route = "collection/{seasonId}",
                arguments = listOf(
                    navArgument("seasonId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val seasonId = backStackEntry.arguments?.getLong("seasonId") ?: 0
                CollectionDetailScreen(
                    seasonId = seasonId,
                    onNavigateBack = { contentNavController.popBackStack() }
                )
            }

            composable(
                route = "player/{aid}/{cid}",
                arguments = listOf(
                    navArgument("aid") { type = NavType.LongType },
                    navArgument("cid") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val aid = backStackEntry.arguments?.getLong("aid") ?: 0
                val cid = backStackEntry.arguments?.getLong("cid") ?: 0
                PlayerScreen(
                    aid = aid,
                    cid = cid,
                    onNavigateBack = { contentNavController.popBackStack() }
                )
            }

            composable(Screen.DownloadList.route) {
                DownloadListScreen(
                    onNavigateBack = { contentNavController.popBackStack() }
                )
            }

            composable(Screen.MySpace.route) {
                MySpaceScreen(
                    onNavigateToUserProfile = { userId ->
                        contentNavController.navigate("user/$userId")
                    },
                    onNavigateToHistory = {
                        contentNavController.navigate("history")
                    },
                    onNavigateToWatchLater = {
                        contentNavController.navigate("watch_later")
                    },
                    onNavigateToFavorite = {
                        contentNavController.navigate("favorite")
                    },
                    onNavigateToFollowing = {
                        contentNavController.navigate("following")
                    },
                    onMenuClick = { isMenuExpanded = !isMenuExpanded }
                )
            }

            composable("history") {
                HistoryScreen(
                    onVideoClick = { videoInfo ->
                        contentNavController.navigate(
                            Screen.VideoDetail.createRoute(videoInfo.aid, videoInfo.bvid)
                        )
                    },
                    onNavigateBack = { contentNavController.popBackStack() }
                )
            }

            composable("watch_later") {
                WatchLaterScreen(
                    onVideoClick = { videoInfo ->
                        contentNavController.navigate(
                            Screen.VideoDetail.createRoute(videoInfo.aid, videoInfo.bvid)
                        )
                    },
                    onNavigateBack = { contentNavController.popBackStack() }
                )
            }

            composable("favorite") {
                FavoriteScreen(
                    onFolderClick = { fid, name ->
                        contentNavController.navigate("favorite_videos/$fid/$name")
                    },
                    onOpusFavoriteClick = {
                        contentNavController.navigate("opus_favorite")
                    },
                    onNavigateBack = { contentNavController.popBackStack() }
                )
            }

            composable(
                route = "favorite_videos/{fid}/{name}",
                arguments = listOf(
                    navArgument("fid") { type = NavType.LongType },
                    navArgument("name") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val fid = backStackEntry.arguments?.getLong("fid") ?: 0L
                val name = backStackEntry.arguments?.getString("name") ?: ""
                val mid = AccountManager.currentAccount.accountId
                
                FavoriteVideosScreen(
                    mid = mid,
                    fid = fid,
                    folderName = name,
                    onVideoClick = { videoInfo ->
                        contentNavController.navigate(
                            Screen.VideoDetail.createRoute(videoInfo.aid, videoInfo.bvid)
                        )
                    },
                    onNavigateBack = { contentNavController.popBackStack() }
                )
            }

            composable("opus_favorite") {
                OpusFavoriteScreen(
                    onOpusClick = { opusId ->
                        contentNavController.navigate("opus_detail/$opusId")
                    },
                    onNavigateBack = { contentNavController.popBackStack() }
                )
            }

            composable("following") {
                val mid = AccountManager.currentAccount.accountId
                FollowingScreen(
                    mid = mid,
                    onUserClick = { userId ->
                        contentNavController.navigate("user/$userId")
                    },
                    onNavigateBack = { contentNavController.popBackStack() }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onMenuClick = { isMenuExpanded = !isMenuExpanded },
                    onSearch = { query ->
                        contentNavController.navigate(Screen.SearchResult.createRoute(query))
                    }
                )
            }

            composable(
                route = Screen.SearchResult.route,
                arguments = listOf(
                    navArgument("keyword") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedKeyword = backStackEntry.arguments?.getString("keyword") ?: ""
                val query = URLDecoder.decode(encodedKeyword, "UTF-8")
                SearchResultScreen(
                    query = query,
                    onNavigateBack = { contentNavController.popBackStack() },
                    onVideoClick = { aid, bvid ->
                        contentNavController.navigate(Screen.VideoDetail.createRoute(aid, bvid))
                    },
                    onOpusClick = { opusId ->
                        contentNavController.navigate("opus_detail/$opusId")
                    },
                    onUserClick = { mid ->
                        contentNavController.navigate("user/$mid")
                    }
                )
            }

            settingsGraph(contentNavController)

            loginGraph(
                contentNavController,
                onLoginSuccess = {
                    contentNavController.navigate(menuConfig.firstDestination) {
                        popUpTo(NavGraph.LOGIN) { inclusive = true }
                    }
                },
                onSkip = {
                    contentNavController.navigate(menuConfig.firstDestination) {
                        popUpTo(NavGraph.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        AnimatedVisibility(
            visible = isMenuExpanded,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isMenuExpanded) {
                        Modifier
                            .systemGestureExclusion()
                            .nestedScroll(menuGestureBlocker)
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        val change = event.changes.firstOrNull() ?: continue
                                        val dragX = change.position.x - change.previousPosition.x
                                        val dragY = change.position.y - change.previousPosition.y
                                        
                                        if (abs(dragX) > abs(dragY) && abs(dragX) > 0) {
                                            event.changes.forEach { it.consume() }
                                        }
                                    }
                                }
                            }
                    } else {
                        Modifier
                    }
                )
        ) {
            MenuPanel(
                modifier = Modifier.fillMaxSize(),
                menuItems = menuConfig.menuItems,
                onSelect = { route ->
                    contentNavController.navigate(route) {
                        popUpTo(Screen.Recommend.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    isMenuExpanded = false
                },
                onDismiss = { isMenuExpanded = false }
            )
        }
    }
}