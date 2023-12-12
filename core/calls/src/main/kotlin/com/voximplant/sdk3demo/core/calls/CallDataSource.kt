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
import com.voximplant.sdk3demo.core.calls.model.CallApiData
import com.voximplant.sdk3demo.core.common.Dispatcher
import com.voximplant.sdk3demo.core.common.VoxDispatchers.Default
import com.voximplant.sdk3demo.core.model.data.CallApiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private var call: Call? = null

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
            }
        }

        override fun onCallFailed(call: Call, code: Int, description: String, headers: Map<String, String>?) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData())
                _callState.emit(call.state)
            }
        }

        override fun onCallRinging(call: Call, headers: Map<String, String>?) {
            coroutineScope.launch {
                _callApiDataFlow.emit(call.asCallData())
                _callState.emit(call.state)
            }
        }
    }

    private val _callApiDataFlow: MutableSharedFlow<CallApiData> = MutableSharedFlow()
    val callApiDataFlow: Flow<CallApiData> = _callApiDataFlow.asSharedFlow()

    private val _callState: MutableSharedFlow<CallState> = MutableSharedFlow()
    val callStateFlow: Flow<CallApiState> = _callState.asSharedFlow().map { callState ->
        callState.asExternalModel
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
                this.call = call
                return Result.success(call.asCallData())
            } else {
                return Result.failure(IllegalStateException())
            }
        }
    }

    fun startCall(id: String): Result<CallApiData> {
        Log.d("DemoV3", "startCall: $call")
        if (call?.id != id) return Result.failure(Throwable("Call not found"))

        if (call?.callDirection == CallDirection.OUTGOING) {
            return try {
                call?.let {
                    it.setCallListener(callListener)
                    it.start()
                    Result.success(it.asCallData())
                } ?: return Result.failure(Throwable("Failed to start call"))
            } catch (exception: CallException) {
                Result.failure(exception)
            }
        } else {
            return Result.failure(Throwable("Incoming call not implemented yet"))
            // TODO (Oleg): Incoming call
        }
        // TODO (Oleg): Start service
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    ServiceCompat.startForeground(AudioCallService(), 0, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
//                } else {
//                    ServiceCompat.startForeground(AudioCallService(), 0, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
//                }
    }

    fun mute(value: Boolean) {
        _isMuted.value = value
        call?.sendAudio(!value)
    }

    fun hold(value: Boolean) {
        call?.hold(value, object : CallCallback {
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
        call?.hangup(null)
    }
}
