package com.voximplant.sdk3demo.feature.audiocall.ongoing.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.voximplant.sdk3demo.feature.audiocall.ongoing.AudioCallOngoingRoute

const val audioCallOngoingRoute = "audio_call_ongoing_route"

internal const val usernameArg = "username"

internal class OngoingCallArgs(val username: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(checkNotNull(savedStateHandle.get<String?>(usernameArg)))
}

fun NavController.navigateToAudioCallOngoing(username: String) {
    this.navigate("$audioCallOngoingRoute/$username") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.audioCallOngoingScreen(
) {
    composable(
        route = "$audioCallOngoingRoute/{$usernameArg}",
        arguments = listOf(
            navArgument(usernameArg) { type = NavType.StringType },
        ),
    ) {
        AudioCallOngoingRoute()
    }
}
