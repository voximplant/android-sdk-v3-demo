/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.calls.model

import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.CallDirection
import com.voximplant.demos.sdk.core.model.data.CallState
import com.voximplant.demos.sdk.core.model.data.CallType

data class CallApiData(
    val id: String,
    var state: CallState,
    val direction: CallDirection,
    val duration: Long,
    val remoteDisplayName: String?,
    val remoteSipUri: String?,
    var type: CallTypeApi,
)

enum class CallTypeApi {
    AudioCall, VideoCall,
}

fun CallApiData.asCall() = Call(
    id = id,
    state = state,
    direction = direction,
    duration = duration,
    remoteDisplayName = remoteDisplayName,
    remoteSipUri = remoteSipUri,
    type = callTypeMap(type),
)

fun callTypeMap(callTypeApi: CallTypeApi): CallType {
    return when (callTypeApi) {
        CallTypeApi.AudioCall -> CallType.AudioCall
        CallTypeApi.VideoCall -> CallType.VideoCall
    }
}

