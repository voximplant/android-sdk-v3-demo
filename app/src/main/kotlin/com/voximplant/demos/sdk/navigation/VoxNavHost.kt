/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.compose.NavHost
import com.voximplant.demos.sdk.feature.audiocall.incoming.navigation.audioCallIncomingScreen
import com.voximplant.demos.sdk.feature.audiocall.incoming.navigation.navigateToAudioCallIncoming
import com.voximplant.demos.sdk.feature.audiocall.navigation.audioCallRoute
import com.voximplant.demos.sdk.feature.audiocall.navigation.audioCallScreen
import com.voximplant.demos.sdk.feature.audiocall.navigation.navigateToAudioCall
import com.voximplant.demos.sdk.feature.audiocall.ongoing.navigation.audioCallOngoingScreen
import com.voximplant.demos.sdk.feature.audiocall.ongoing.navigation.navigateToAudioCallOngoing
import com.voximplant.demos.sdk.feature.catalog.navigation.catalogRoute
import com.voximplant.demos.sdk.feature.catalog.navigation.catalogScreen
import com.voximplant.demos.sdk.feature.login.navigation.loginScreen
import com.voximplant.demos.sdk.feature.login.navigation.navigateToLogin
import com.voximplant.demos.sdk.ui.VoxAppState

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
                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    navController.popBackStack()
                }
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
