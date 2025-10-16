package com.huanli233.bilizepam.ui.screens.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import com.huanli233.bilizepam.ui.screens.download.DownloadListScreen
import com.huanli233.bilizepam.ui.screens.image.ImageViewerScreen
import com.huanli233.bilizepam.ui.screens.player.PlayerScreen
import com.huanli233.bilizepam.ui.screens.recommend.RecommendScreen
import com.huanli233.bilizepam.ui.screens.user.UserProfileScreen
import com.huanli233.bilizepam.ui.screens.video.VideoDetailScreen
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

    AnimatedContent(
        targetState = isMenuExpanded,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally { it } + fadeIn()).togetherWith(
                    slideOutHorizontally { -it } + fadeOut()
                )
            } else {
                (slideInHorizontally { -it } + fadeIn()).togetherWith(
                    slideOutHorizontally { it } + fadeOut()
                )
            }
        },
        label = "MenuContentTransition",
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
    ) { showMenu ->
        if (showMenu) {
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
        } else {
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
                route = "image_viewer/{imageUrl}/{initialPage}",
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
        }
    }
}