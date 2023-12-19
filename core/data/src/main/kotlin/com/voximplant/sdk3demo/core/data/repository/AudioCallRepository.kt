package com.voximplant.sdk3demo.core.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import com.voximplant.core.notifications.Notifier
import com.voximplant.core.notifications.OngoingAudioCallService
import com.voximplant.sdk3demo.core.calls.CallDataSource
import com.voximplant.sdk3demo.core.calls.model.asCall
import com.voximplant.sdk3demo.core.common.VoxBroadcastReceiver
import com.voximplant.sdk3demo.core.model.data.Call
import com.voximplant.sdk3demo.core.model.data.CallApiState
import com.voximplant.sdk3demo.core.model.data.CallDirection
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class AudioCallRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val callDataSource: CallDataSource,
    private val notifier: Notifier,
    coroutineScope: CoroutineScope,
) {
    val call: Flow<Call?>
        get() = callDataSource.callApiDataFlow.map { callApiData -> callApiData?.asCall() }

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

    fun startListeningIncomingCalls() {
        callDataSource.startListeningIncomingCalls()
    }

    fun stopListeningIncomingCalls() {
        callDataSource.stopListeningIncomingCalls()
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
