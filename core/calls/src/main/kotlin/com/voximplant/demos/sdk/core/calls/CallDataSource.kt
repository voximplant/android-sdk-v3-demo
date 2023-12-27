/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.calls

import android.util.Log
import com.voximplant.android.sdk.calls.Call
import com.voximplant.android.sdk.calls.CallCallback
import com.voximplant.android.sdk.calls.CallDirection
import com.voximplant.android.sdk.calls.CallException
import com.voximplant.android.sdk.calls.CallListener
import com.voximplant.android.sdk.calls.CallManager
import com.voximplant.android.sdk.calls.CallSettings
import com.voximplant.android.sdk.calls.IncomingCallListener
import com.voximplant.android.sdk.calls.RejectMode
import com.voximplant.demos.sdk.core.calls.model.CallApiData
import com.voximplant.demos.sdk.core.model.data.CallState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.scheduleAtFixedRate

class CallDataSource @Inject constructor(
    private val callManager: CallManager,
    private val coroutineScope: CoroutineScope,
) {
    private var activeCall: Call? = null

    private val callListener = object : CallListener {

        override fun onCallConnected(call: Call, headers: Map<String, String>?) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData())
            }
            startCallTimer(call)
        }

        override fun onCallDisconnected(call: Call, headers: Map<String, String>?, answeredElsewhere: Boolean) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData())
                activeCall?.setCallListener(null)
                activeCall = null
                callTimer.cancel()
                callTimer.purge()
                _duration.value = 0L
            }
        }

        override fun onCallFailed(call: Call, code: Int, description: String, headers: Map<String, String>?) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData().copy(state = CallState.Failed(description)))
                activeCall?.setCallListener(null)
                activeCall = null
                callTimer.cancel()
                callTimer.purge()
                _duration.value = 0L
            }
        }

        override fun onCallRinging(call: Call, headers: Map<String, String>?) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData())
            }
        }

        override fun onCallReconnected(call: Call) {
            if (call.state == com.voximplant.android.sdk.calls.CallState.Created) {
                startCall(call.id)
            }
        }
    }

    private val incomingCallListener = object : IncomingCallListener {
        override fun onIncomingCall(call: Call, hasIncomingVideo: Boolean, headers: Map<String, String>?) {
            if (activeCall != null) {
                // Indicates that the user is unavailable only on the device with an active call.
                call.reject(RejectMode.Busy, null)
                return
            }

            call.setCallListener(callListener)
            activeCall = call
            coroutineScope.launch {
                _callApiDataFlow.emit(null)
                _isMuted.value = false
                _isOnHold.value = false

                _callApiDataFlow.emit(call.asCallData())
            }
        }
    }

    private val _callApiDataFlow: MutableStateFlow<CallApiData?> = MutableStateFlow(null)
    val callApiDataFlow: Flow<CallApiData?> = _callApiDataFlow.asStateFlow()

    private val _duration: MutableStateFlow<Long> = MutableStateFlow(0L)
    val duration: Flow<Long> = _duration.asStateFlow()

    private val _isMuted: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isMuted: Flow<Boolean> = _isMuted.asStateFlow()

    private val _isOnHold: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isOnHold: Flow<Boolean> = _isOnHold.asStateFlow()

    private var callTimer: Timer = Timer("callTimer")

    fun createCall(username: String): Result<CallApiData> {
        callManager.call(username, CallSettings()).let { call ->
            if (call != null) {
                coroutineScope.launch {
                    _callApiDataFlow.emit(null)
                    _isMuted.value = false
                    _isOnHold.value = false

                    _callApiDataFlow.emit(call.asCallData())
                }
                activeCall = call
                return Result.success(call.asCallData())
            } else {
                return Result.failure(IllegalStateException())
            }
        }
    }

    fun startListeningForIncomingCalls() {
        callManager.setIncomingCallListener(incomingCallListener)
    }

    fun stopListeningForIncomingCalls() {
        callManager.setIncomingCallListener(null)
    }

    fun startCall(id: String): Result<CallApiData> {
        Log.d("DemoV3", "startCall: $activeCall")
        coroutineScope.launch {
            _callApiDataFlow.emit(_callApiDataFlow.value?.copy(state = CallState.Connecting))
        }
        activeCall?.let { call ->
            if (call.id != id) return Result.failure(Throwable("Call not found"))

            when (call.callDirection) {
                CallDirection.Outgoing -> {
                    return try {
                        call.setCallListener(callListener)
                        try {
                            call.start()
                        } catch (exception: CallException) {
                            Log.e("Voximplant", exception.message, exception)
                        }
                        Result.success(call.asCallData())
                    } catch (exception: CallException) {
                        Result.failure(exception)
                    }
                }

                CallDirection.Incoming -> {
                    call.setCallListener(callListener)
                    try {
                        call.answer(CallSettings())
                    } catch (exception: CallException) {
                        Log.e("Voximplant", exception.message, exception)
                    }
                    return Result.success(call.asCallData())
                }
            }
        } ?: return Result.failure(Throwable("Call not found"))
    }

    fun mute(value: Boolean) {
        _isMuted.value = value
        activeCall?.muteAudio(value)
    }

    fun hold(value: Boolean) {
        activeCall?.hold(value, object : CallCallback {
            override fun onFailure(exception: CallException) {
                Log.e("DemoV3", "CallDataSource::hold failed")
            }

            override fun onSuccess() {
                _isOnHold.value = value
            }

        })
    }

    fun hangUp() {
        coroutineScope.launch {
            _callApiDataFlow.emit(_callApiDataFlow.value?.copy(state = CallState.Disconnecting))
            activeCall?.hangup(null)
        }
    }

    fun reject() {
        try {
            activeCall?.reject(RejectMode.Decline, null)
        } catch (exception: CallException) {
            Log.e("Voximplant", exception.message, exception)
        }
    }

    private fun startCallTimer(call: Call) {
        callTimer = Timer("callTimer").apply {
            scheduleAtFixedRate(delay = TIMER_DELAY_MS, TIMER_DELAY_MS) {
                _duration.value = call.callDuration
            }
        }
    }
}

private const val TIMER_DELAY_MS = 1000L
