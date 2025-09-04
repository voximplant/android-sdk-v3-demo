/*
 * Copyright (c) 2011 - 2025, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.calls

import com.voximplant.android.sdk.calls.Call
import com.voximplant.android.sdk.calls.CallCallback
import com.voximplant.android.sdk.calls.CallDirection
import com.voximplant.android.sdk.calls.CallDisconnectReason
import com.voximplant.android.sdk.calls.CallException
import com.voximplant.android.sdk.calls.CallListener
import com.voximplant.android.sdk.calls.CallSettings
import com.voximplant.android.sdk.calls.IncomingCallListener
import com.voximplant.android.sdk.calls.LocalVideoStream
import com.voximplant.android.sdk.calls.RejectMode
import com.voximplant.android.sdk.calls.RemoteVideoStream
import com.voximplant.android.sdk.calls.VICalls
import com.voximplant.demos.sdk.core.calls.model.CallApiData
import com.voximplant.demos.sdk.core.calls.model.CallTypeApi
import com.voximplant.demos.sdk.core.logger.Logger
import com.voximplant.demos.sdk.core.model.data.CallState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.scheduleAtFixedRate

class CallDataSource @Inject constructor(
    private val callManager: VICalls,
    private val coroutineScope: CoroutineScope,
) {
    private var activeCall: Call? = null

    private val _remoteVideoStreamFlow: MutableStateFlow<RemoteVideoStream?> = MutableStateFlow(null)
    val remoteVideoStreamFlow: StateFlow<RemoteVideoStream?> = _remoteVideoStreamFlow.asStateFlow()

    private val callListener = object : CallListener {

        override fun onCallConnected(call: Call, headers: Map<String, String>?) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData())
            }
            startCallTimer(call)
        }

        override fun onCallDisconnected(call: Call, headers: Map<String, String>?, disconnectReason: CallDisconnectReason) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData())
                activeCall?.setCallListener(null)
                activeCall = null
                callTimer.cancel()
                callTimer.purge()
                _duration.value = 0L
                _remoteVideoStreamFlow.value = null
            }
        }

        override fun onCallFailed(call: Call, code: Int, description: String?, headers: Map<String, String>?) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData().copy(state = CallState.Failed(description)))
                activeCall?.setCallListener(null)
                activeCall = null
                callTimer.cancel()
                callTimer.purge()
                _duration.value = 0L
                _remoteVideoStreamFlow.value = null
            }
        }

        override fun onStartRinging(call: Call, headers: Map<String, String>?) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData())
            }
        }

        override fun onCallReconnecting(call: Call) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData())
            }
        }

        override fun onCallReconnected(call: Call) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData())
            }

            when (suspendedAction) {
                is SuspendedAction.Reject -> reject()
                is SuspendedAction.Answer -> startCall(call.id)
                null -> {}
            }
            suspendedAction = null
        }

        override fun onRemoteVideoStreamAdded(call: Call, videoStream: RemoteVideoStream) {
            _remoteVideoStreamFlow.value = videoStream
        }

        override fun onRemoteVideoStreamRemoved(call: Call, videoStream: RemoteVideoStream) {
            _remoteVideoStreamFlow.value = null
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

                _callApiDataFlow.emit(call.asCallData().apply {
                    type = if (hasIncomingVideo) CallTypeApi.VideoCall else CallTypeApi.AudioCall
                })
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

    private var suspendedAction: SuspendedAction? = null

    fun createCall(username: String, stream: LocalVideoStream? = null): Result<CallApiData> {
        callManager.createCall(username, CallSettings().apply {
            localVideoStream = stream
            receiveVideo = stream != null
        }).let { call ->
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

    fun clearCall(call: com.voximplant.demos.sdk.core.model.data.Call) {
        coroutineScope.launch {
            if (call.id == activeCall?.id) return@launch
            if (call.state is CallState.Created || call.state is CallState.Disconnected || call.state is CallState.Failed) {
                Logger.debug("CallDataSource::clearCall")
                _callApiDataFlow.emit(null)
                _isMuted.value = false
                _isOnHold.value = false
                activeCall = null
            } else {
                Logger.error("CallDataSource::clearCall: Only a recently created, canceled or failed call can be cleared.")
            }
        }
    }

    fun startListeningForIncomingCalls() {
        callManager.setIncomingCallListener(incomingCallListener)
    }

    fun stopListeningForIncomingCalls() {
        callManager.setIncomingCallListener(null)
    }

    fun startCall(id: String, stream: LocalVideoStream? = null): Result<CallApiData> {
        Logger.debug("startCall: $activeCall")
        coroutineScope.launch {
            _callApiDataFlow.emit(_callApiDataFlow.value?.copy(state = CallState.Connecting))
        }
        activeCall?.let { call ->
            if (call.id != id) {
                coroutineScope.launch {
                    _callApiDataFlow.emit(_callApiDataFlow.value?.copy(state = CallState.Failed("Call not found")))
                }
                activeCall = null
                return Result.failure(Throwable("Call not found"))
            }

            when (call.direction) {
                CallDirection.Outgoing -> {
                    return try {
                        call.setCallListener(callListener)
                        call.start()
                        Result.success(call.asCallData())
                    } catch (exception: CallException) {
                        Logger.error("CallDataSource::startCall: failed to start the call: ${exception.message}", exception)
                        coroutineScope.launch {
                            _callApiDataFlow.emit(_callApiDataFlow.value?.copy(state = CallState.Failed(exception.toString())))
                        }
                        activeCall = null
                        Result.failure(exception)
                    }
                }

                CallDirection.Incoming -> {
                    call.setCallListener(callListener)
                    try {
                        call.answer(CallSettings().apply {
                            localVideoStream = stream
                            receiveVideo = stream != null
                        })
                    } catch (exception: CallException) {
                        Logger.error("CallDataSource::startCall: failed to answer the call: ${exception.message}", exception)
                        if (activeCall?.state == com.voximplant.android.sdk.calls.CallState.Reconnecting) {
                            suspendedAction = SuspendedAction.Answer
                        }
                    }
                    return Result.success(call.asCallData())
                }
            }
        } ?: run {
            coroutineScope.launch {
                _callApiDataFlow.emit(_callApiDataFlow.value?.copy(state = CallState.Failed("Call not found")))
            }
            activeCall = null
            return Result.failure(Throwable("Call not found"))
        }
    }

    fun toggleMute() {
        activeCall?.muteAudio(!_isMuted.value)
        _isMuted.value = !_isMuted.value
    }

    fun startSendingLocalVideo(videoStream: LocalVideoStream, callBack: (Boolean) -> Unit) {
        activeCall?.startSendingVideo(videoStream, object : CallCallback {
            override fun onFailure(exception: CallException) {
                Logger.error("CallDataSource::startSendingLocalVideo failed")
                callBack(false)
            }

            override fun onSuccess() {
                callBack(true)
            }
        })
    }

    fun stopSendingLocalVideo(callBack: (Boolean) -> Unit) {
        activeCall?.stopSendingVideo(object : CallCallback {
            override fun onFailure(exception: CallException) {
                Logger.error("CallDataSource::stopSendingLocalVideo failed")
                callBack(false)
            }

            override fun onSuccess() {
                callBack(true)
            }
        })
    }

    fun hold(value: Boolean) {
        activeCall?.hold(value, object : CallCallback {
            override fun onFailure(exception: CallException) {
                Logger.error("CallDataSource::hold failed")
            }

            override fun onSuccess() {
                _isOnHold.value = value
            }

        })
    }

    fun hangUp() {
        _remoteVideoStreamFlow.value = null
        coroutineScope.launch {
            _callApiDataFlow.emit(_callApiDataFlow.value?.copy(state = CallState.Disconnecting))
        }
        activeCall?.hangup(null)
    }

    fun reject() {
        coroutineScope.launch {
            _callApiDataFlow.emit(_callApiDataFlow.value?.copy(state = CallState.Disconnecting))
        }
        try {
            activeCall?.reject(RejectMode.Decline, null)
        } catch (exception: CallException) {
            Logger.error("CallDataSource::reject: failure: ${exception.message}", exception)
            if (activeCall?.state == com.voximplant.android.sdk.calls.CallState.Reconnecting) {
                suspendedAction = SuspendedAction.Reject
            }
        }
    }

    fun sendDtmf(value: String) {
        activeCall?.sendDTMF(tone = value)
    }

    private fun startCallTimer(call: Call) {
        callTimer = Timer("callTimer").apply {
            scheduleAtFixedRate(delay = TIMER_DELAY_MS, TIMER_DELAY_MS) {
                _duration.value = call.duration
            }
        }
    }
}

private const val TIMER_DELAY_MS = 1000L

private sealed interface SuspendedAction {
    data object Answer : SuspendedAction
    data object Reject : SuspendedAction
}
