package com.huanli233.bilizepam.ui.screens.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
        modifier = Modifier.fillMaxSize()
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