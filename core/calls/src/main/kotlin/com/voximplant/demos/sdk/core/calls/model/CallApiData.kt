/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.calls.model

import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.CallDirection
import com.voximplant.demos.sdk.core.model.data.CallState

data class CallApiData(
    val id: String,
    var state: CallState,
    val direction: CallDirection,
    val duration: Long,
    val remoteDisplayName: String?,
    val remoteSipUri: String?,
)

fun CallApiData.asCall() = Call(
    id = id,
    state = state,
    direction = direction,
    duration = duration,
    remoteDisplayName = remoteDisplayName,
    remoteSipUri = remoteSipUri,
)
