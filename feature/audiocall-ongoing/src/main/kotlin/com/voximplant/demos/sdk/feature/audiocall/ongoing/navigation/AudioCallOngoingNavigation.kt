/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.audiocall.ongoing.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.voximplant.demos.sdk.feature.audiocall.ongoing.AudioCallOngoingRoute

const val audioCallOngoingRoute = "audio_call_ongoing_route"

internal const val idArg = "id"
internal const val displayNameArg = "displayName"

internal class OngoingCallArgs(val id: String, val displayName: String?) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(
                checkNotNull(savedStateHandle.get<String>(idArg)),
                savedStateHandle.get<String>(displayNameArg),
            )
}

fun NavController.navigateToAudioCallOngoing(id: String, displayName: String?) {
    this.navigate("$audioCallOngoingRoute/$id?displayName=$displayName") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.audioCallOngoingScreen(
    onCallEnded: () -> Unit,
) {
    composable(
        route = "$audioCallOngoingRoute/{$idArg}?displayName={$displayNameArg}",
        arguments = listOf(
            navArgument(idArg) { type = NavType.StringType },
            navArgument(displayNameArg) {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        AudioCallOngoingRoute(
            onCallEnded = onCallEnded,
        )
    }
}
