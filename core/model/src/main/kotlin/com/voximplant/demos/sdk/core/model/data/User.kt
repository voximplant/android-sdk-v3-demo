/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.model.data

data class User(
    val username: String,
    val displayName: String,
)

fun User.isNotEmpty() = username.isNotEmpty()
