/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.calls

import com.voximplant.android.sdk.calls.Call
import com.voximplant.android.sdk.calls.CallDirection.Incoming
import com.voximplant.android.sdk.calls.CallDirection.Outgoing
import com.voximplant.demos.sdk.core.calls.model.CallApiData
import com.voximplant.demos.sdk.core.model.data.CallDirection
import com.voximplant.demos.sdk.core.model.data.CallState

fun Call.asCallData() = CallApiData(
    id = id,
    callDirection = when (callDirection) {
        Incoming -> CallDirection.INCOMING
        Outgoing -> CallDirection.OUTGOING
    },
    callDuration = callDuration,
    remoteDisplayName = remoteDisplayName,
    remoteSipUri = remoteSipUri,
)

val com.voximplant.android.sdk.calls.CallState.asInternalModel
    get() = when (this) {
        com.voximplant.android.sdk.calls.CallState.Created -> CallState.Created
        com.voximplant.android.sdk.calls.CallState.Connecting -> CallState.Connecting
        com.voximplant.android.sdk.calls.CallState.Connected -> CallState.Connected
        com.voximplant.android.sdk.calls.CallState.Reconnecting -> CallState.Reconnecting
        com.voximplant.android.sdk.calls.CallState.Disconnecting -> CallState.Disconnecting
        com.voximplant.android.sdk.calls.CallState.Disconnected -> CallState.Disconnected
        com.voximplant.android.sdk.calls.CallState.Failed -> CallState.Failed(null)
    }

