/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.calls.model

import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.CallDirection

data class CallApiData(
    val id: String,
    val callDirection: CallDirection,
    val callDuration: Long,
    val remoteDisplayName: String?,
    val remoteSipUri: String?,
)

fun CallApiData.asCall() = Call(
    id = id,
    direction = callDirection,
    duration = callDuration,
    remoteDisplayName = remoteDisplayName,
    remoteSipUri = remoteSipUri,
)
