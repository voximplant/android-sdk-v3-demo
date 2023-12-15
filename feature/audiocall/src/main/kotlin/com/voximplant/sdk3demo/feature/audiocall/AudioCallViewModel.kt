package com.voximplant.sdk3demo.feature.audiocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.sdk3demo.core.domain.CreateCallUseCase
import com.voximplant.sdk3demo.core.domain.GetCallStateUseCase
import com.voximplant.sdk3demo.core.domain.GetCallUseCase
import com.voximplant.sdk3demo.core.domain.GetUserUseCase
import com.voximplant.sdk3demo.core.domain.StartListeningForIncomingCallsUseCase
import com.voximplant.sdk3demo.core.domain.StopListeningForIncomingCallsUseCase
import com.voximplant.sdk3demo.core.model.data.Call
import com.voximplant.sdk3demo.core.model.data.CallApiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AudioCallViewModel @Inject constructor(
    getUserUseCase: GetUserUseCase,
    private val createCallUseCase: CreateCallUseCase,
    getCall: GetCallUseCase,
    getCallState: GetCallStateUseCase,
    startListeningForIncomingCallsUseCase: StartListeningForIncomingCallsUseCase,
    private val stopListeningForIncomingCallsUseCase: StopListeningForIncomingCallsUseCase,
) : ViewModel() {
    val user = getUserUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    val audioCallUiState: StateFlow<AudioCallUiState> = audioCallUiState(
        callFlow = getCall(),
        callStateFlow = getCallState(),
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AudioCallUiState.Inactive,
    )

    init {
        startListeningForIncomingCallsUseCase()
    }

    suspend fun createCall(username: String): Call? = viewModelScope.async {
        createCallUseCase(username)
    }.await().fold(
        onSuccess = { return it },
        onFailure = { return null },
    )

    override fun onCleared() {
        super.onCleared()
        stopListeningForIncomingCallsUseCase()
    }
}

private fun audioCallUiState(
    callFlow: Flow<Call?>,
    callStateFlow: Flow<CallApiState?>,
): Flow<AudioCallUiState> = combine(callFlow, callStateFlow) { call, state ->
    if (call == null || state == null) {
        AudioCallUiState.Inactive
    } else {
        AudioCallUiState.Active(
            call = call,
            state = when (state) {
                CallApiState.CREATED -> CallState.Created
                CallApiState.CONNECTING -> CallState.Connecting
                CallApiState.CONNECTED -> CallState.Connected
                CallApiState.RECONNECTING -> CallState.Reconnecting
                CallApiState.DISCONNECTING -> CallState.Disconnecting
                CallApiState.DISCONNECTED -> CallState.Disconnected
                CallApiState.FAILED -> CallState.Failed("audioCallUiState error")
            },
        )
    }
}

sealed interface AudioCallUiState {
    data class Active(val call: Call, val state: CallState) : AudioCallUiState
    data object Inactive : AudioCallUiState
}

sealed class CallState {
    data object Created : CallState()
    data object Connecting : CallState()
    data object Connected : CallState()
    data object Disconnected : CallState()
    data object Reconnecting : CallState()
    data object Disconnecting : CallState()
    data class Failed(
        val error: String,
    ) : CallState()
}
