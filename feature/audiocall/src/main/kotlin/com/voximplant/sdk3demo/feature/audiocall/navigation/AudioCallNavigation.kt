package com.voximplant.sdk3demo.feature.audiocall.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.voximplant.sdk3demo.feature.audiocall.AudioCallRoute

const val audioCallRoute = "audio_call_route"

fun NavController.navigateToAudioCall() {
    this.navigate(audioCallRoute) {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.audioCallScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onCallClick: (String, String) -> Unit,
) {
    composable(route = audioCallRoute) {
        AudioCallRoute(
            onBackClick = onBackClick,
            onLoginClick = onLoginClick,
            onCallCreated = onCallClick,
        )
    }
}
