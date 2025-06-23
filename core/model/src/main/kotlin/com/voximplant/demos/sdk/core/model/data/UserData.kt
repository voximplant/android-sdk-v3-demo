/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.model.data

data class UserData(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
    val node: Node?,
    val shouldHideNotificationPermissionRequest: Boolean,
    val shouldHideMicrophonePermissionRequest: Boolean,
    val shouldHideCameraPermissionRequest: Boolean,
)
