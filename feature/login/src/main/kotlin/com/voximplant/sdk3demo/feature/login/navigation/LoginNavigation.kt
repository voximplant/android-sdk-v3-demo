package com.voximplant.sdk3demo.feature.login.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.voximplant.sdk3demo.feature.login.LoginRoute

const val loginRoute = "login_route"

fun NavController.navigateToLogin(navOptions: NavOptions? = null) {
    this.navigate(loginRoute, navOptions)
}

fun NavGraphBuilder.loginScreen() {
    composable(route = loginRoute) {
        LoginRoute()
    }
}
