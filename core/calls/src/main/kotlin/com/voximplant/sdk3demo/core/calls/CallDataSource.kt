package com.voximplant.sdk3demo.core.calls

import android.util.Log
import com.voximplant.calls.Call
import com.voximplant.calls.CallCallback
import com.voximplant.calls.CallDirection
import com.voximplant.calls.CallException
import com.voximplant.calls.CallListener
import com.voximplant.calls.CallManager
import com.voximplant.calls.CallSettings
import com.voximplant.calls.CallState
import com.voximplant.calls.IncomingCallListener
import com.voximplant.calls.RejectMode
import com.voximplant.sdk3demo.core.calls.model.CallApiData
import com.voximplant.sdk3demo.core.common.Dispatcher
import com.voximplant.sdk3demo.core.common.VoxDispatchers.Default
import com.voximplant.sdk3demo.core.model.data.CallApiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class CallDataSource @Inject constructor(
    private val callManager: CallManager,
    @Dispatcher(Default) private val defaultDispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
) {
    private var activeCall: Call? = null

    private val callListener = object : CallListener {

        override fun onCallConnected(call: Call, headers: Map<String, String>?) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData())
                _callState.emit(call.state)
            }
        }

        override fun onCallDisconnected(call: Call, headers: Map<String, String>?, answeredElsewhere: Boolean) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData())
                _callState.emit(call.state)
                activeCall?.setCallListener(null)
                activeCall = null
            }
        }

        override fun onCallFailed(call: Call, code: Int, description: String, headers: Map<String, String>?) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData())
                _callState.emit(call.state)
                activeCall?.setCallListener(null)
                activeCall = null
            }
        }

        override fun onCallRinging(call: Call, headers: Map<String, String>?) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData())
                _callState.emit(call.state)
            }
        }
    }

    private val incomingCallListener = object : IncomingCallListener {
        override fun onIncomingCall(call: Call, hasIncomingVideo: Boolean, headers: Map<String, String>?) {
            if (activeCall != null) {
                call.reject(RejectMode.BUSY, null)
                return
            }

            call.setCallListener(callListener)
            activeCall = call
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData())
                _callState.emit(call.state)
            }
        }
    }

    private val _callApiDataFlow: MutableStateFlow<CallApiData?> = MutableStateFlow(null)
    val callApiDataFlow: Flow<CallApiData?> = _callApiDataFlow.asStateFlow()

    private val _callState: MutableStateFlow<CallState?> = MutableStateFlow(null)
    val callStateFlow: Flow<CallApiState?> = _callState.asStateFlow().map { callState ->
        callState?.asExternalModel
    }.flowOn(defaultDispatcher)

    private val _isMuted: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isMuted: Flow<Boolean> = _isMuted.asStateFlow()

    private val _isOnHold: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isOnHold: Flow<Boolean> = _isOnHold.asStateFlow()

    fun createCall(username: String): Result<CallApiData> {
        coroutineScope.launch {
            _callState.emit(CallState.CONNECTING)
        }
        callManager.call(username, CallSettings()).let { call ->
            if (call != null) {
                coroutineScope.launch {
                    _callApiDataFlow.emit(call.asCallData())
                }
                activeCall = call
                return Result.success(call.asCallData())
            } else {
                return Result.failure(IllegalStateException())
            }
        }
    }

    fun startListeningIncomingCalls() {
        callManager.setIncomingCallListener(incomingCallListener)
    }

    fun stopListeningIncomingCalls() {
        callManager.setIncomingCallListener(null)
    }

    fun startCall(id: String): Result<CallApiData> {
        Log.d("DemoV3", "startCall: $activeCall")

        activeCall?.let { call ->
            if (call.id != id) return Result.failure(Throwable("Call not found"))

            when (call.callDirection) {
                CallDirection.OUTGOING -> {
                    return try {
                        call.setCallListener(callListener)
                        call.start()
                        Result.success(call.asCallData())
                    } catch (exception: CallException) {
                        Result.failure(exception)
                    }
                }

                CallDirection.INCOMING -> {
                    call.let {
                        it.setCallListener(callListener)
                        it.answer(CallSettings())
                        return Result.success(it.asCallData())
                    }
                }
            }
        } ?: return Result.failure(Throwable("Call not found"))
        // TODO (Oleg): Start service
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    ServiceCompat.startForeground(AudioCallService(), 0, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
//                } else {
//                    ServiceCompat.startForeground(AudioCallService(), 0, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
//                }
    }

    fun mute(value: Boolean) {
        _isMuted.value = value
        activeCall?.sendAudio(!value)
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
            _callState.emit(CallState.DISCONNECTING)
        }
        activeCall?.hangup(null)
    }

    fun reject() {
        activeCall?.reject(RejectMode.DECLINE, null)
    }
}
