package com.voximplant.sdk3demo.feature.audiocall.ongoing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.sdk3demo.core.domain.GetAudioDeviceUseCase
import com.voximplant.sdk3demo.core.domain.GetAudioDevicesUseCase
import com.voximplant.sdk3demo.core.domain.GetCallStateUseCase
import com.voximplant.sdk3demo.core.domain.GetCallUseCase
import com.voximplant.sdk3demo.core.domain.GetHoldStateUseCase
import com.voximplant.sdk3demo.core.domain.GetMuteStateUseCase
import com.voximplant.sdk3demo.core.domain.HangUpCallUseCase
import com.voximplant.sdk3demo.core.domain.HoldCallUseCase
import com.voximplant.sdk3demo.core.domain.MuteCallUseCase
import com.voximplant.sdk3demo.core.domain.SelectAudioDeviceUseCase
import com.voximplant.sdk3demo.core.domain.StartCallUseCase
import com.voximplant.sdk3demo.core.model.data.AudioDevice
import com.voximplant.sdk3demo.core.model.data.Call
import com.voximplant.sdk3demo.core.model.data.CallApiState
import com.voximplant.sdk3demo.feature.audiocall.ongoing.navigation.OngoingCallArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioCallOngoingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getCall: GetCallUseCase,
    getCallState: GetCallStateUseCase,
    getMuteState: GetMuteStateUseCase,
    getHoldState: GetHoldStateUseCase,
    getAudioDevices: GetAudioDevicesUseCase,
    getAudioDevice: GetAudioDeviceUseCase,
    private val selectAudioDeviceUseCase: SelectAudioDeviceUseCase,
    startCall: StartCallUseCase,
    private val muteCall: MuteCallUseCase,
    private val holdCall: HoldCallUseCase,
    private val hangUpCall: HangUpCallUseCase,
) : ViewModel() {
    private val ongoingCallArgs: OngoingCallArgs = OngoingCallArgs(savedStateHandle)

    private val username = ongoingCallArgs.username

    val callOngoingUiState: StateFlow<CallOngoingUiState> = callUiState(
        username = username,
        callFlow = getCall(),
        stateFlow = getCallState(),
        isMutedFlow = getMuteState(),
        isOnHoldFlow = getHoldState(),
        audioDevicesFlow = getAudioDevices(),
        audioDeviceFlow = getAudioDevice(),
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CallOngoingUiState.Inactive(
            state = CallState.Connecting,
            username = username,
            isMuted = getMuteState().stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = false).value,
            audioDevices = getAudioDevices().stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = emptyList()).value,
            audioDevice = getAudioDevice().stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = null).value,
            call = null,
        ),
    )

    init {
        startCall(ongoingCallArgs.id)
    }

    fun toggleMute() {
        viewModelScope.launch {
            muteCall(!callOngoingUiState.value.isMuted)
        }
    }

    fun toggleHold() {
        viewModelScope.launch {
            holdCall(!callOngoingUiState.value.isOnHold)
        }
    }

    fun selectAudioDevice(audioDevice: AudioDevice) {
        viewModelScope.launch {
            selectAudioDeviceUseCase(audioDevice)
        }
    }

    fun hangUp() {
        viewModelScope.launch {
            hangUpCall()
        }
    }

}

private fun callUiState(
    username: String,
    callFlow: Flow<Call>,
    stateFlow: Flow<CallApiState>,
    isMutedFlow: Flow<Boolean>,
    isOnHoldFlow: Flow<Boolean>,
    audioDevicesFlow: Flow<List<AudioDevice>>,
    audioDeviceFlow: Flow<AudioDevice?>,
): Flow<CallOngoingUiState> = combine(callFlow, stateFlow, isMutedFlow, isOnHoldFlow, audioDevicesFlow, audioDeviceFlow) {
    val call = it[0] as Call
    val state = it[1] as CallApiState
    val isMuted = it[2] as Boolean
    val isOnHold = it[3] as Boolean
    val audioDevices = it[4] as List<AudioDevice>
    val audioDevice = it[5] as AudioDevice

    when (state) {
        CallApiState.CREATED -> CallOngoingUiState.Inactive(state = CallState.Connecting, username = username, isMuted = isMuted, audioDevices = audioDevices, audioDevice = audioDevice, call = null)
        CallApiState.CONNECTING -> CallOngoingUiState.Paused(state = CallState.Connecting, username = username, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
        CallApiState.CONNECTED -> CallOngoingUiState.Active(state = CallState.Connected, username = username, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
        CallApiState.RECONNECTING -> CallOngoingUiState.Paused(state = CallState.Reconnecting, username = username, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
        CallApiState.DISCONNECTING -> CallOngoingUiState.Inactive(state = CallState.Disconnecting, username = username, isMuted = isMuted, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
        CallApiState.DISCONNECTED -> CallOngoingUiState.Inactive(state = CallState.Disconnected, username = username, isMuted = isMuted, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
        CallApiState.FAILED -> CallOngoingUiState.Inactive(state = CallState.Failed(error = "callUiState error"), username = username, isMuted = isMuted, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
    }
}

sealed class CallOngoingUiState(
    open val state: CallState,
    open val username: String,
    open val isMuted: Boolean,
    open val isOnHold: Boolean,
    open val audioDevices: List<AudioDevice>,
    open val audioDevice: AudioDevice?,
    open val call: Call? = null,
) {
    data class Inactive(
        override val state: CallState,
        override val username: String,
        override val isMuted: Boolean,
        override val audioDevices: List<AudioDevice>,
        override val audioDevice: AudioDevice?,
        override val call: Call?,
    ) : CallOngoingUiState(state, username, isMuted, false, audioDevices, audioDevice)

    data class Active(
        override val state: CallState,
        override val username: String,
        override val isMuted: Boolean,
        override val isOnHold: Boolean,
        override val audioDevices: List<AudioDevice>,
        override val audioDevice: AudioDevice?,
        override val call: Call,
    ) : CallOngoingUiState(state, username, isMuted, isOnHold, audioDevices, audioDevice, call)

    data class Paused(
        override val state: CallState,
        override val username: String,
        override val isMuted: Boolean,
        override val isOnHold: Boolean,
        override val audioDevices: List<AudioDevice>,
        override val audioDevice: AudioDevice?,
        override val call: Call,
    ) : CallOngoingUiState(state, username, isMuted, isOnHold, audioDevices, audioDevice, call)
}

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
