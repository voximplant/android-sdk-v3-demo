/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.foundation.model

import com.voximplant.demos.sdk.core.model.data.UserData

data class NetworkUserData(
    val user: NetworkUser,
    val accessToken: String,
    val refreshToken: String,
)

fun NetworkUserData.asUserData() = UserData(
    user = user.asUser(),
    accessToken = accessToken,
    refreshToken = refreshToken,
    node = null,
)
