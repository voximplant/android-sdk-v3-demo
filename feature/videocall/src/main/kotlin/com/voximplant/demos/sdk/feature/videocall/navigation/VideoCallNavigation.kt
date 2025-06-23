/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.videocall.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.voximplant.demos.sdk.feature.videocall.VideoCallRoute

const val videoCallRoute = "video_call_route"

fun NavController.navigateToVideoCall() {
    this.navigate(videoCallRoute) {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.videoCallScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onCallCreated: (String, String?) -> Unit,
) {
    composable(route = videoCallRoute) {
        VideoCallRoute(
            onBackClick = onBackClick,
            onLoginClick = onLoginClick,
            onCallCreated = onCallCreated,
        )
    }
}
