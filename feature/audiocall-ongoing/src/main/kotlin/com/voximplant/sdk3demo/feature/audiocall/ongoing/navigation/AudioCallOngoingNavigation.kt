package com.voximplant.sdk3demo.feature.audiocall.ongoing.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.voximplant.sdk3demo.feature.audiocall.ongoing.AudioCallOngoingRoute

const val audioCallOngoingRoute = "audio_call_ongoing_route"

internal const val idArg = "id"
internal const val usernameArg = "username"

internal class OngoingCallArgs(val id: String, val username: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(
                checkNotNull(savedStateHandle.get<String>(idArg)),
                checkNotNull(savedStateHandle.get<String>(usernameArg)),
            )
}

fun NavController.navigateToAudioCallOngoing(id: String, username: String) {
    this.navigate("$audioCallOngoingRoute/$id?username=$username") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.audioCallOngoingScreen(
    onCallEnded: () -> Unit,
) {
    composable(
        route = "$audioCallOngoingRoute/{$idArg}?username={$usernameArg}",
        arguments = listOf(
            navArgument(idArg) { type = NavType.StringType },
            navArgument(usernameArg) { type = NavType.StringType },
        ),
    ) {
        AudioCallOngoingRoute(
            onCallEnded = onCallEnded,
        )
    }
}
