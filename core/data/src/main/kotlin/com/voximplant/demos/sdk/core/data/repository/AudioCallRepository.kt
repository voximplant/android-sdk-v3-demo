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
    coroutineScope: CoroutineScope,
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
                val audioCallIncomingService = Intent(context, AudioCallIncomingService::class.java)
                val audioCallOngoingService = Intent(context, AudioCallOngoingService::class.java)

                when (call?.state) {
                    is CallState.Created -> {
                        if (call.direction == CallDirection.INCOMING) {
                            br.register(context)
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
                        context.stopService(audioCallIncomingService)
                        context.stopService(audioCallOngoingService)
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

    fun refuseCall() = callDataSource.refuseCall()

    fun startCall(id: String): Result<Call> {
        notifier.cancelCallNotification()
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
        notifier.cancelCallNotification()
        callDataSource.reject()
    }
}
