package com.voximplant.sdk3demo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.voximplant.sdk3demo.feature.audiocall.incoming.navigation.audioCallIncomingScreen
import com.voximplant.sdk3demo.feature.audiocall.incoming.navigation.navigateToAudioCallIncoming
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
            onIncomingCall = { callId, displayName ->
                navController.navigateToAudioCallIncoming(callId, displayName)
            },
            onCallCreated = { callId, displayName ->
                navController.navigateToAudioCallOngoing(callId, displayName)
            },
        )
        audioCallIncomingScreen(
            onCallEnded = {
                navController.popBackStack()
            },
            onCallAnswered = { callId, displayName ->
                navController.popBackStack()
                navController.navigateToAudioCallOngoing(callId, displayName)
            },
        )
        audioCallOngoingScreen(
            onCallEnded = {
                navController.popBackStack()
            },
        )
    }
}
