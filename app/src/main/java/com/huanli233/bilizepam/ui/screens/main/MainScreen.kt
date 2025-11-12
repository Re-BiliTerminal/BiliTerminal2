package com.huanli233.bilizepam.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.huanli233.bilizepam.data.menu.MenuConfigManager
import com.huanli233.bilizepam.ui.components.menu.MenuPanel
import com.huanli233.bilizepam.ui.navigation.NavGraph
import com.huanli233.bilizepam.ui.navigation.Screen
import com.huanli233.bilizepam.ui.navigation.loginGraph
import com.huanli233.bilizepam.ui.navigation.settingsGraph
import com.huanli233.bilizepam.ui.screens.collection.CollectionDetailScreen
import com.huanli233.bilizepam.ui.screens.comment.CommentDetailScreen
import com.huanli233.bilizepam.ui.screens.download.DownloadListScreen
import com.huanli233.bilizepam.ui.screens.dynamic.DynamicHomeScreen
import com.huanli233.bilizepam.ui.screens.image.ImageViewerScreen
import com.huanli233.bilizepam.ui.screens.player.PlayerScreen
import com.huanli233.bilizepam.ui.screens.recommend.RecommendScreen
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
                    onMenuClick = { isMenuExpanded = !isMenuExpanded }
                )
            }

            composable(Screen.Dynamic.route) {
                DynamicHomeScreen(
                    onDynamicClick = { dynamic ->
                        // TODO: Navigate to dynamic detail
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
                    onNavigateBack = { contentNavController.popBackStack() }
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

            composable("download_list") {
                DownloadListScreen(
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