/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.audiocall.incoming.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.voximplant.demos.sdk.feature.audiocall.incoming.AudioCallIncomingRoute

const val audioCallIncomingRoute = "audio_call_incoming_route"

internal const val idArg = "id"
internal const val displayNameArg = "displayName"
internal const val actionArg = "action"

internal class IncomingCallArgs(val id: String, val displayName: String?) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(
                checkNotNull(savedStateHandle.get<String>(idArg)),
                savedStateHandle.get<String>(displayNameArg),
            )
}

fun NavController.navigateToAudioCallIncoming(id: String, displayName: String?, action: String? = null) {
    this.navigate("$audioCallIncomingRoute/$id?displayName=$displayName&action=$action") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.audioCallIncomingScreen(
    onCallEnded: () -> Unit,
    onCallAnswered: (String, String?) -> Unit,
) {
    composable(
        route = "$audioCallIncomingRoute/{$idArg}?displayName={$displayNameArg}&action={$actionArg}",
        arguments = listOf(
            navArgument(idArg) { type = NavType.StringType },
            navArgument(displayNameArg) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(actionArg) {
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
