package com.huanli233.bilizepam.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.huanli233.bilizepam.ui.screens.login.LoginScreenHost

fun NavGraphBuilder.loginGraph(onLoginSuccess: () -> Unit, onSkip: () -> Unit) {
    navigation(
        route = NavGraph.LOGIN,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreenHost(onLoginSuccess = onLoginSuccess, onSkip = onSkip)
        }
    }
}