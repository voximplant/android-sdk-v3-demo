package com.voximplant.sdk3demo.core.calls.model

import com.voximplant.sdk3demo.core.model.data.Call
import com.voximplant.sdk3demo.core.model.data.CallDirection

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
