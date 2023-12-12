package com.voximplant.sdk3demo.feature.audiocall.incoming

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.sdk3demo.core.domain.GetCallStateUseCase
import com.voximplant.sdk3demo.core.domain.RejectIncomingCallUseCase
import com.voximplant.sdk3demo.core.model.data.CallApiState
import com.voximplant.sdk3demo.core.model.data.CallApiState.CONNECTED
import com.voximplant.sdk3demo.core.model.data.CallApiState.CONNECTING
import com.voximplant.sdk3demo.core.model.data.CallApiState.CREATED
import com.voximplant.sdk3demo.core.model.data.CallApiState.DISCONNECTED
import com.voximplant.sdk3demo.core.model.data.CallApiState.DISCONNECTING
import com.voximplant.sdk3demo.core.model.data.CallApiState.FAILED
import com.voximplant.sdk3demo.core.model.data.CallApiState.RECONNECTING
import com.voximplant.sdk3demo.feature.audiocall.incoming.navigation.IncomingCallArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioCallIncomingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getCallState: GetCallStateUseCase,
    private val rejectCall: RejectIncomingCallUseCase,
) : ViewModel() {
    private val incomingCallArgs: IncomingCallArgs = IncomingCallArgs(savedStateHandle)

    val id = incomingCallArgs.id
    private val displayName = incomingCallArgs.displayName

    val callIncomingUiState: StateFlow<AudioCallIncomingUiState> = audioCallIncomingUiState(
        displayName = displayName,
        stateFlow = getCallState(),
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AudioCallIncomingUiState(
            displayName = displayName,
            state = CallState.Connecting,
        ),
    )

    fun reject() {
        viewModelScope.launch {
            rejectCall()
        }
    }

}

private fun audioCallIncomingUiState(
    displayName: String?,
    stateFlow: Flow<CallApiState>,
): Flow<AudioCallIncomingUiState> = combine(stateFlow) {
    AudioCallIncomingUiState(
        displayName = displayName,
        state = when (it[0]) {
            CREATED -> CallState.Connecting
            CONNECTING -> CallState.Connecting
            CONNECTED -> CallState.Connected
            RECONNECTING -> CallState.Reconnecting
            DISCONNECTING -> CallState.Disconnecting
            DISCONNECTED -> CallState.Disconnected
            FAILED -> CallState.Failed("audioCallIncomingUiState error")
        },
    )
}

data class AudioCallIncomingUiState(
    val displayName: String?,
    val state: CallState,
)

sealed class CallState {
    data object Connecting : CallState()
    data object Connected : CallState()
    data object Disconnected : CallState()
    data object Reconnecting : CallState()
    data object Disconnecting : CallState()
    data class Failed(
        val error: String,
    ) : CallState()
}
