/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import com.voximplant.demos.sdk.core.calls.CallDataSource
import com.voximplant.demos.sdk.core.calls.model.asCall
import com.voximplant.demos.sdk.core.common.VoxBroadcastReceiver
import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.CallApiState
import com.voximplant.demos.sdk.core.model.data.CallDirection
import com.voximplant.demos.sdk.core.notifications.Notifier
import com.voximplant.demos.sdk.core.notifications.OngoingAudioCallService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

class AudioCallRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val callDataSource: CallDataSource,
    private val notifier: Notifier,
    coroutineScope: CoroutineScope,
) {
    val call: Flow<Call?>
        get() = combine(callDataSource.callApiDataFlow, callDataSource.duration) { callApiData, duration ->
            if (callApiData == null) return@combine null

            Call(
                id = callApiData.id,
                direction = callApiData.callDirection,
                duration = duration,
                remoteDisplayName = callApiData.remoteDisplayName,
                remoteSipUri = callApiData.remoteSipUri,
            )
        }

    val state: Flow<CallApiState?>
        get() = callDataSource.callStateFlow

    val isMuted: Flow<Boolean>
        get() = callDataSource.isMuted

    val isOnHold: Flow<Boolean>
        get() = callDataSource.isOnHold

    private val br = VoxBroadcastReceiver(
        onHangUpReceived = {
            hangUp()
        },
        onRejectReceived = {
            reject()
        },
        onAnswerReceived = {
            coroutineScope.launch {
                call.firstOrNull()?.id?.let { id ->
                    startCall(id)
                }
            }
        },
    )

    init {
        coroutineScope.launch {
            combine(call, state, ::Pair).collect {
                val call = it.first ?: return@collect
                val state = it.second ?: return@collect

                val ongoingCallIntent = Intent(context, OngoingAudioCallService::class.java).apply {
                    putExtra("id", call.id)
                    putExtra("displayName", call.remoteDisplayName)
                }

                if (state == CallApiState.CREATED) {
                    if (call.direction == CallDirection.INCOMING) {
                        br.register(context)
                        notifier.postIncomingCallNotification(call.id, call.remoteDisplayName)
                    }
                } else if (state == CallApiState.CONNECTED) {
                    if (call.duration != 0L) return@collect

                    br.register(context)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(ongoingCallIntent)
                    } else {
                        context.startService(ongoingCallIntent)
                    }
                } else if (state == CallApiState.DISCONNECTED || state == CallApiState.FAILED) {
                    br.unregister(context)
                    notifier.cancelCallNotification()
                    context.stopService(ongoingCallIntent)
                }
            }
        }
    }

    fun startListeningForIncomingCalls() {
        callDataSource.startListeningForIncomingCalls()
    }

    fun stopListeningForIncomingCalls() {
        callDataSource.stopListeningForIncomingCalls()
    }

    fun createCall(username: String): Result<Call> {
        callDataSource.createCall(username).let { callDataResult ->
            callDataResult.fold(
                onSuccess = { callData ->
                    return Result.success(callData.asCall())
                },
                onFailure = { throwable ->
                    return Result.failure(throwable)
                },
            )
        }
    }

    fun startCall(id: String): Result<Call> {
        callDataSource.startCall(id).let { callDataResult ->
            callDataResult.fold(
                onSuccess = { callData ->
                    return Result.success(callData.asCall())
                },
                onFailure = { throwable ->
                    return Result.failure(throwable)
                },
            )
        }
    }

    fun mute(value: Boolean) {
        callDataSource.mute(value)
    }

    fun hold(value: Boolean) {
        callDataSource.hold(value)
    }

    fun hangUp() {
        callDataSource.hangUp()
    }

    fun reject() {
        callDataSource.reject()
    }
}
