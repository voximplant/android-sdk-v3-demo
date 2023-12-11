package com.voximplant.sdk3demo.core.calls

import com.voximplant.calls.Call
import com.voximplant.calls.CallDirection.INCOMING
import com.voximplant.calls.CallDirection.OUTGOING
import com.voximplant.calls.CallState
import com.voximplant.calls.CallState.CONNECTED
import com.voximplant.calls.CallState.CONNECTING
import com.voximplant.calls.CallState.CREATED
import com.voximplant.calls.CallState.DISCONNECTED
import com.voximplant.calls.CallState.DISCONNECTING
import com.voximplant.calls.CallState.FAILED
import com.voximplant.calls.CallState.RECONNECTING
import com.voximplant.sdk3demo.core.calls.model.CallApiData
import com.voximplant.sdk3demo.core.model.data.CallApiState
import com.voximplant.sdk3demo.core.model.data.CallDirection

fun Call.asCallData() = CallApiData(
    id = id,
    callDirection = when (callDirection) {
        INCOMING -> CallDirection.INCOMING
        OUTGOING -> CallDirection.OUTGOING
    },
    callDuration = callDuration,
    remoteDisplayName = remoteDisplayName,
    remoteSipUri = remoteSipUri,
)

val CallState.asExternalModel
    get() = when (this) {
        CREATED -> CallApiState.CREATED
        CONNECTING -> CallApiState.CONNECTING
        CONNECTED -> CallApiState.CONNECTED
        RECONNECTING -> CallApiState.RECONNECTING
        DISCONNECTING -> CallApiState.DISCONNECTING
        DISCONNECTED -> CallApiState.DISCONNECTED
        FAILED -> CallApiState.FAILED
    }
