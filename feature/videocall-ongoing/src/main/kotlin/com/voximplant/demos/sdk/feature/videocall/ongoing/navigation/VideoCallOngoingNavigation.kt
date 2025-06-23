/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.videocall.ongoing.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.voximplant.demos.sdk.feature.videocall.ongoing.VideoCallOngoingRoute

const val videoCallOngoingRoute = "video_call_ongoing_route"

internal const val idArg = "id"
internal const val displayNameArg = "displayName"

internal class OngoingVideoCallArgs(val id: String, val displayName: String?) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(
                checkNotNull(savedStateHandle.get<String>(idArg)),
                savedStateHandle.get<String>(displayNameArg),
            )
}

fun NavController.navigateToVideoCallOngoing(id: String, displayName: String?) {
    var isContainsOngoing = false
    this.currentBackStackEntry?.destination?.hierarchy?.forEach {
        if (it.route != null) {
            isContainsOngoing = it.route?.contains("ongoing_route") == true
        }
    }
    this.navigate("$videoCallOngoingRoute/$id?displayName=$displayName") {
        if (isContainsOngoing) {
            popUpTo(graph.startDestinationId)
        }
        launchSingleTop = true
    }
}

fun NavGraphBuilder.videoCallOngoingScreen(
    onCallEnded: () -> Unit,
) {
    composable(
        route = "$videoCallOngoingRoute/{$idArg}?displayName={$displayNameArg}",
        arguments = listOf(
            navArgument(idArg) { type = NavType.StringType },
            navArgument(displayNameArg) {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        VideoCallOngoingRoute(
            onCallEnded = onCallEnded,
        )
    }
}



