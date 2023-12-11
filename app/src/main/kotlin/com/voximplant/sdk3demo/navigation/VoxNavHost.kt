package com.voximplant.sdk3demo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.voximplant.sdk3demo.feature.audiocall.navigation.audioCallRoute
import com.voximplant.sdk3demo.feature.audiocall.navigation.audioCallScreen
import com.voximplant.sdk3demo.feature.audiocall.navigation.navigateToAudioCall
import com.voximplant.sdk3demo.feature.audiocall.ongoing.navigation.audioCallOngoingScreen
import com.voximplant.sdk3demo.feature.audiocall.ongoing.navigation.navigateToAudioCallOngoing
import com.voximplant.sdk3demo.feature.catalog.navigation.catalogRoute
import com.voximplant.sdk3demo.feature.catalog.navigation.catalogScreen
import com.voximplant.sdk3demo.feature.login.navigation.loginScreen
import com.voximplant.sdk3demo.feature.login.navigation.navigateToLogin
import com.voximplant.sdk3demo.ui.VoxAppState

@Composable
fun VoxNavHost(
    appState: VoxAppState,
    modifier: Modifier = Modifier,
    startDestination: String = catalogRoute,
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        catalogScreen(
            onLoginClick = {
                navController.navigateToLogin()
            },
            onModuleClick = { route ->
                if (route == audioCallRoute) {
                    navController.navigateToAudioCall()
                }
            },
        )
        loginScreen(
            onLoginSuccess = {
                navController.popBackStack()
            }
        )
        audioCallScreen(
            onBackClick = {
                navController.popBackStack()
            },
            onLoginClick = {
                navController.navigateToLogin()
            },
            onCallClick = { callId, username ->
                navController.navigateToAudioCallOngoing(callId, username)
            },
        )
        audioCallOngoingScreen(
            onCallEnded = {
                navController.popBackStack()
            },
        )
    }
}
