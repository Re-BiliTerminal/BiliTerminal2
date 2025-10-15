package com.huanli233.bilizepam.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.huanli233.bilizepam.ui.common.CustomSnackbarHost
import com.huanli233.bilizepam.ui.screens.main.MainScreen
import com.huanli233.bilizepam.ui.screens.main.MainUiState
import com.huanli233.bilizepam.ui.screens.main.MainViewModel
import com.huanli233.bilizepam.ui.screens.setup.SetupScreen

@Composable
fun AppNavHost(mainViewModel: MainViewModel = hiltViewModel()) {
    val uiState by mainViewModel.uiState.collectAsState()
    val navController = rememberSwipeDismissableNavController()

    val startDestination = when (uiState) {
        is MainUiState.NeedsSetup -> NavGraph.SETUP
        is MainUiState.NeedsLogin -> NavGraph.LOGIN
        is MainUiState.Ready -> NavGraph.MAIN
        else -> NavGraph.MAIN
    }

    Box {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(NavGraph.SETUP) {
                SetupScreen(
                    onSetupComplete = {
                        mainViewModel.onSetupComplete()
                        navController.navigate(NavGraph.LOGIN) {
                            popUpTo(NavGraph.SETUP) { inclusive = true }
                        }
                    }
                )
            }

            loginGraph(
                navController = navController,
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
        MainScreen(navController)
    }
}