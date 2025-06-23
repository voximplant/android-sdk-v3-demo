/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.audiocall.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.voximplant.demos.sdk.feature.audiocall.AudioCallRoute

const val audioCallRoute = "audio_call_route"

fun NavController.navigateToAudioCall() {
    this.navigate(audioCallRoute) {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.audioCallScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onCallCreated: (String, String?) -> Unit,
) {
    composable(route = audioCallRoute) {
        AudioCallRoute(
            onBackClick = onBackClick,
            onLoginClick = onLoginClick,
            onCallCreated = onCallCreated,
        )
    }
}
