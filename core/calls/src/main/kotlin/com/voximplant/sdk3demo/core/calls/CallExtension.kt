package com.voximplant.sdk3demo.core.calls

import com.voximplant.android.sdk.calls.Call
import com.voximplant.android.sdk.calls.CallDirection.Incoming
import com.voximplant.android.sdk.calls.CallDirection.Outgoing
import com.voximplant.android.sdk.calls.CallState
import com.voximplant.android.sdk.calls.CallState.Connected
import com.voximplant.android.sdk.calls.CallState.Connecting
import com.voximplant.android.sdk.calls.CallState.Created
import com.voximplant.android.sdk.calls.CallState.Disconnected
import com.voximplant.android.sdk.calls.CallState.Disconnecting
import com.voximplant.android.sdk.calls.CallState.Failed
import com.voximplant.android.sdk.calls.CallState.Reconnecting
import com.voximplant.sdk3demo.core.calls.model.CallApiData
import com.voximplant.sdk3demo.core.model.data.CallApiState
import com.voximplant.sdk3demo.core.model.data.CallDirection

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

val CallState.asExternalModel
    get() = when (this) {
        Created -> CallApiState.CREATED
        Connecting -> CallApiState.CONNECTING
        Connected -> CallApiState.CONNECTED
        Reconnecting -> CallApiState.RECONNECTING
        Disconnecting -> CallApiState.DISCONNECTING
        Disconnected -> CallApiState.DISCONNECTED
        Failed -> CallApiState.FAILED
    }
