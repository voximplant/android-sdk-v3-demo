/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.videocall.incoming.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.voximplant.demos.sdk.feature.videocall.incoming.VideoCallIncomingRoute

const val videoCallIncomingRoute = "video_call_incoming_route"

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

fun NavController.navigateToVideoCallIncoming(id: String, displayName: String?, action: String? = null) {
    this.navigate("$videoCallIncomingRoute/$id?displayName=$displayName&action=$action") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.videoCallIncomingScreen(
    onCallEnded: () -> Unit,
    onCallAnswered: (String, String?) -> Unit,
) {
    composable(
        route = "$videoCallIncomingRoute/{$idArg}?displayName={$displayNameArg}&action={$actionArg}",
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
        VideoCallIncomingRoute(
            onCallEnded = onCallEnded,
            onCallAnswered = onCallAnswered,
        )
    }
}
