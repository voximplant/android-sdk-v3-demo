/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.foundation.model

import com.voximplant.demos.sdk.core.model.data.User

data class NetworkUser(
    val username: String,
    val displayName: String,
)

fun NetworkUser.asUser() = User(
    username = username,
    displayName = displayName,
)
