package com.voximplant.sdk3demo.feature.audiocall.incoming.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.voximplant.sdk3demo.feature.audiocall.incoming.AudioCallIncomingRoute

const val audioCallIncomingRoute = "audio_call_incoming_route"

internal const val idArg = "id"
internal const val displayNameArg = "displayName"

internal class IncomingCallArgs(val id: String, val displayName: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(
                checkNotNull(savedStateHandle.get<String>(idArg)),
                checkNotNull(savedStateHandle.get<String>(displayNameArg)),
            )
}

fun NavController.navigateToAudioCallIncoming(id: String, displayName: String?) {
    this.navigate("$audioCallIncomingRoute/$id?displayName=$displayName") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.audioCallIncomingScreen(
    onCallEnded: () -> Unit,
    onCallAnswered: (String) -> Unit,
) {
    composable(
        route = "$audioCallIncomingRoute/{$idArg}?displayName={$displayNameArg}",
        arguments = listOf(
            navArgument(idArg) { type = NavType.StringType },
            navArgument(displayNameArg) {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        AudioCallIncomingRoute(
            onCallEnded = onCallEnded,
            onCallAnswered = onCallAnswered,
        )
    }
}
