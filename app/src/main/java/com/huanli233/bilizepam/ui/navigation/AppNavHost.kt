package com.huanli233.bilizepam.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.common.CustomSnackbarHost
import com.huanli233.bilizepam.ui.screens.main.MainScreen
import com.huanli233.bilizepam.ui.screens.main.MainUiState
import com.huanli233.bilizepam.ui.screens.main.MainViewModel
import com.huanli233.bilizepam.ui.screens.setup.SetupNavRoutes
import com.huanli233.bilizepam.ui.screens.setup.UiSetupScreen
import com.huanli233.bilizepam.ui.screens.setup.WelcomeScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(mainViewModel: MainViewModel = hiltViewModel()) {
    val uiState by mainViewModel.uiState.collectAsState()
    val navController = rememberNavController()

    val startDestination = when (uiState) {
        is MainUiState.NeedsSetup -> NavGraph.SETUP
        is MainUiState.NeedsLogin -> NavGraph.LOGIN
        is MainUiState.Ready -> NavGraph.MAIN
        else -> NavGraph.MAIN
    }

    Box {
        NavHost(navController = navController, startDestination = startDestination) {
            navigation(
                route = NavGraph.SETUP,
                startDestination = "setup"
            ) {
                composable("setup") {
                    val innerNavController = rememberNavController()
                    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val coroutineScope = rememberCoroutineScope()

                    val topBarTitle by remember(currentRoute) {
                        derivedStateOf {
                            when (currentRoute) {
                                SetupNavRoutes.WELCOME -> R.string.welcome
                                SetupNavRoutes.UI_SETUP -> R.string.initialize_setting
                                else -> R.string.app_name
                            }
                        }
                    }

                    val isRootScreen = currentRoute == SetupNavRoutes.WELCOME

                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { Text(stringResource(id = topBarTitle)) },
                                navigationIcon = {
                                    if (!isRootScreen) {
                                        IconButton(onClick = { innerNavController.popBackStack() }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                        }
                                    }
                                }
                            )
                        },
                        floatingActionButton = {
                            FloatingActionButton(onClick = {
                                when (currentRoute) {
                                    SetupNavRoutes.WELCOME -> {
                                        innerNavController.navigate(SetupNavRoutes.UI_SETUP)
                                    }
                                    SetupNavRoutes.UI_SETUP -> {
                                        coroutineScope.launch {
                                            mainViewModel.onSetupComplete()
                                            navController.navigate(NavGraph.LOGIN) {
                                                popUpTo(NavGraph.SETUP) { inclusive = true }
                                            }
                                        }
                                    }
                                }
                            }) {
                                when (currentRoute) {
                                    SetupNavRoutes.WELCOME -> Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next")
                                    else -> Icon(Icons.Filled.Check, "Finish")
                                }
                            }
                        }
                    ) { paddingValues ->
                        NavHost(
                            navController = innerNavController,
                            startDestination = SetupNavRoutes.WELCOME,
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            val animationSpec = tween<IntOffset>(400)
                            composable(
                                route = SetupNavRoutes.WELCOME,
                                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = animationSpec) },
                                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = animationSpec) }
                            ) {
                                WelcomeScreen()
                            }
                            composable(
                                route = SetupNavRoutes.UI_SETUP,
                                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec) },
                                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = animationSpec) }
                            ) {
                                UiSetupScreen()
                            }
                        }
                    }
                }
            }

            loginGraph(
                onLoginSuccess = {
                    navController.navigate(NavGraph.MAIN) {
                        popUpTo(NavGraph.LOGIN) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(NavGraph.MAIN) {
                        popUpTo(NavGraph.LOGIN) { inclusive = true }
                    }
                }
            )

            mainGraph(navController)
        }
        CustomSnackbarHost()
    }
}

fun NavGraphBuilder.mainGraph(navController: NavController) {
    composable(NavGraph.MAIN) {
        MainScreen()
    }
}