package com.voximplant.sdk3demo.core.data.repository

import com.voximplant.sdk3demo.core.calls.CallDataSource
import com.voximplant.sdk3demo.core.calls.model.asCall
import com.voximplant.sdk3demo.core.model.data.Call
import com.voximplant.sdk3demo.core.model.data.CallApiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AudioCallRepository @Inject constructor(
    private val callDataSource: CallDataSource,
) {
    val call: Flow<Call>
        get() = callDataSource.callApiDataFlow.map { callApiData -> callApiData.asCall() }

    val state: Flow<CallApiState>
        get() = callDataSource.callStateFlow

    val isMuted: Flow<Boolean>
        get() = callDataSource.isMuted

    val isOnHold: Flow<Boolean>
        get() = callDataSource.isOnHold

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

    fun makeCall(id: String): Result<Call> {
        callDataSource.makeCall(id).let { callDataResult ->
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
}
