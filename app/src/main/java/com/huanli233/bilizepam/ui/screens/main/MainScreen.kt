package com.huanli233.bilizepam.ui.screens.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.currentBackStackEntryAsState
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.menu.MenuConfigManager
import com.huanli233.bilizepam.ui.components.TopBar
import com.huanli233.bilizepam.ui.components.TopBarState
import com.huanli233.bilizepam.ui.components.menu.MenuPanel
import com.huanli233.bilizepam.ui.navigation.Screen
import com.huanli233.bilizepam.ui.navigation.allScreens
import com.huanli233.bilizepam.ui.navigation.settingsGraph
import com.huanli233.bilizepam.ui.screens.collection.CollectionDetailScreen
import com.huanli233.bilizepam.ui.screens.download.DownloadListScreen
import com.huanli233.bilizepam.ui.screens.image.ImageViewerScreen
import com.huanli233.bilizepam.ui.screens.player.PlayerScreen
import com.huanli233.bilizepam.ui.screens.recommend.RecommendScreen
import com.huanli233.bilizepam.ui.screens.user.UserProfileScreen
import com.huanli233.bilizepam.ui.screens.video.VideoDetailScreen
import java.net.URLDecoder
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun MainScreen() {
    val menuConfig by remember { mutableStateOf(MenuConfigManager.readMenuConfig()) }
    val contentNavController = rememberNavController()

    var isMenuExpanded by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        while (true) {
            currentTime = LocalTime.now().format(formatter)
            delay(1000L)
        }
    }

    val navBackStackEntry by contentNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isSubScreen = currentRoute != Screen.Recommend.route && !currentRoute.isNullOrEmpty()

    val titleResId = remember(currentRoute) {
        allScreens.find { it.route == currentRoute }?.titleResId ?: R.string.app_name
    }

    val topBarState = if (isMenuExpanded) TopBarState.PAGE else TopBarState.MENU

    Scaffold { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            TopBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!isSubScreen) {
                            isMenuExpanded = !isMenuExpanded
                        } else {
                            contentNavController.popBackStack()
                        }
                    },
                title = stringResource(id = titleResId),
                state = topBarState,
                time = currentTime,
                isMenuScreen = !isSubScreen
            )

            AnimatedContent(
                targetState = isMenuExpanded,
                label = "MenuExpandAnimation",
                transitionSpec = {
                    if (targetState) {
                        slideInVertically { -it } + fadeIn() togetherWith
                                slideOutVertically { it } + fadeOut()
                    } else {
                        slideInVertically { it } + fadeIn() togetherWith
                                slideOutVertically { -it } + fadeOut()
                    }
                }
            ) { expanded ->
                if (expanded) {
                    MenuPanel(
                        Modifier.fillMaxHeight(),
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
                        }
                    )
                } else {
                    NavHost(
                        navController = contentNavController,
                        startDestination = Screen.Recommend.route
                    ) {
                        composable(Screen.Recommend.route) {
                            RecommendScreen(
                                onVideoClick = { videoInfo ->
                                    contentNavController.navigate(
                                        Screen.VideoDetail.createRoute(videoInfo.aid, videoInfo.bvid)
                                    )
                                }
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
                    }
                }
            }
        }
    }
}