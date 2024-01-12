/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.model.data

data class Call(
    val id: String,
    val state: CallState,
    val direction: CallDirection,
    val duration: Long,
    val remoteDisplayName: String?,
    val remoteSipUri: String?,
)
