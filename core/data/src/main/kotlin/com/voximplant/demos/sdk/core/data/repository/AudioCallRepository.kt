/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
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
import com.voximplant.demos.sdk.core.notifications.AudioCallIncomingService
import com.voximplant.demos.sdk.core.notifications.AudioCallOngoingService
import com.voximplant.demos.sdk.core.notifications.Notifier
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
    private val coroutineScope: CoroutineScope,
) {
    val callFlow: Flow<Call?>
        get() = combine(callDataSource.callApiDataFlow, callDataSource.duration) { callApiData, duration ->
            if (callApiData == null) return@combine null

            Call(
                id = callApiData.id,
                state = callApiData.state,
                direction = callApiData.direction,
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
            coroutineScope.launch {
                callFlow.firstOrNull()?.id?.let { id ->
                    startCall(id)
                }
            }
        },
    )

    private val audioCallIncomingService = Intent(context, AudioCallIncomingService::class.java)
    private val audioCallOngoingService = Intent(context, AudioCallOngoingService::class.java)

    private var pushHandled: Boolean = false

    private fun startIncomingCallService(call: Call) {
        pushHandled = false
        audioCallIncomingService.apply {
            putExtra("id", call.id)
            putExtra("displayName", call.remoteDisplayName)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(audioCallIncomingService)
        } else {
            context.startService(audioCallIncomingService)
        }
    }

    init {
        coroutineScope.launch {
            callFlow.collect { call ->

                when (call?.state) {
                    is CallState.Created -> {
                        if (call.direction == CallDirection.INCOMING) {
                            br.register(context)
                            if (pushHandled) {
                                startIncomingCallService(call)
                            } else {
                                notifier.postIncomingCallNotification(call.id, call.remoteDisplayName)
                            }
                        }
                    }

                    is CallState.Connected -> {
                        if (call.duration != 0L) return@collect

                        br.register(context)
                        audioCallOngoingService.apply {
                            putExtra("id", call.id)
                            putExtra("displayName", call.remoteDisplayName)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(audioCallOngoingService)
                        } else {
                            context.startService(audioCallOngoingService)
                        }
                    }

                    is CallState.Disconnected,
                    is CallState.Failed,
                    null,
                    -> {
                        br.unregister(context)
                        notifier.cancelCallNotification()
                        context.stopService(audioCallIncomingService)
                        context.stopService(audioCallOngoingService)
                        pushHandled = false
                    }

                    else -> {}
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

    fun refuseCall(call: Call) = callDataSource.refuseCall(call)

    fun startCall(id: String): Result<Call> {
        notifier.cancelCallNotification()
        context.stopService(audioCallIncomingService)
        pushHandled = false
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

    fun handlePush() {
        pushHandled = true

        coroutineScope.launch {
            callFlow.firstOrNull()?.let { call ->
                if (call.state is CallState.Created) {
                    if (call.direction == CallDirection.INCOMING) {
                        startIncomingCallService(call)
                    }
                }
            }
        }
    }

    fun mute(value: Boolean) {
        callDataSource.mute(value)
    }

    fun hold(value: Boolean) {
        callDataSource.hold(value)
    }

    fun hangUp() {
        context.stopService(audioCallOngoingService)
        callDataSource.hangUp()
    }

    fun reject() {
        notifier.cancelCallNotification()
        context.stopService(audioCallIncomingService)
        callDataSource.reject()
        pushHandled = false
    }

    fun sendDtmf(value: String) = callDataSource.sendDtmf(value)
}
