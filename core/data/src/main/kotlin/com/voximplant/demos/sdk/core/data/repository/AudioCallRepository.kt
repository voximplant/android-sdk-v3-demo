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
import com.voximplant.demos.sdk.core.model.data.CallDirection
import com.voximplant.demos.sdk.core.model.data.CallState
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
    val callFlow: Flow<Call?>
        get() = combine(callDataSource.callApiDataFlow, callDataSource.duration) { callApiData, duration ->
            if (callApiData == null) return@combine null

            Call(
                id = callApiData.id,
                state = callApiData.state,
                direction = callApiData.callDirection,
                duration = duration,
                remoteDisplayName = callApiData.remoteDisplayName,
                remoteSipUri = callApiData.remoteSipUri,
            )
        }

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
            notifier.cancelCallNotification()
            coroutineScope.launch {
                callFlow.firstOrNull()?.id?.let { id ->
                    startCall(id)
                }
            }
        },
    )

    init {
        coroutineScope.launch {
            callFlow.collect { call ->
                val ongoingAudioCallService = Intent(context, OngoingAudioCallService::class.java)

                if (call?.state is CallState.Created) {
                    if (call.direction == CallDirection.INCOMING) {
                        br.register(context)
                        notifier.postIncomingCallNotification(call.id, call.remoteDisplayName)
                    }
                } else if (call?.state is CallState.Connected) {
                    if (call.duration != 0L) return@collect

                    br.register(context)
                    ongoingAudioCallService.apply {
                        putExtra("id", call.id)
                        putExtra("displayName", call.remoteDisplayName)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(ongoingAudioCallService)
                    } else {
                        context.startService(ongoingAudioCallService)
                    }
                } else if (call == null || call.state is CallState.Disconnected || call.state is CallState.Failed) {
                    br.unregister(context)
                    notifier.cancelCallNotification()
                    context.stopService(ongoingAudioCallService)
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
        notifier.cancelCallNotification()
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
