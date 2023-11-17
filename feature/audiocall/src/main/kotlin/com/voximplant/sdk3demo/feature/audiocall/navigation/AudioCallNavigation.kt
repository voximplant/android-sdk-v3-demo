package com.voximplant.sdk3demo.feature.audiocall.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.voximplant.sdk3demo.feature.audiocall.AudioCallRoute

const val audioCallRoute = "audio_call_route"

fun NavController.navigateToAudioCall(navOptions: NavOptions? = null) {
    this.navigate(audioCallRoute, navOptions)
}

fun NavGraphBuilder.audioCallScreen() {
    composable(route = audioCallRoute) {
        AudioCallRoute()
    }
}
