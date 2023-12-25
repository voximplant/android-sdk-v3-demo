/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.audiocall.ongoing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.demos.sdk.core.domain.GetAudioDeviceUseCase
import com.voximplant.demos.sdk.core.domain.GetAudioDevicesUseCase
import com.voximplant.demos.sdk.core.domain.GetCallStateUseCase
import com.voximplant.demos.sdk.core.domain.GetCallUseCase
import com.voximplant.demos.sdk.core.domain.GetHoldStateUseCase
import com.voximplant.demos.sdk.core.domain.GetLoginStateUseCase
import com.voximplant.demos.sdk.core.domain.GetMuteStateUseCase
import com.voximplant.demos.sdk.core.domain.HangUpCallUseCase
import com.voximplant.demos.sdk.core.domain.HoldCallUseCase
import com.voximplant.demos.sdk.core.domain.MuteCallUseCase
import com.voximplant.demos.sdk.core.domain.SelectAudioDeviceUseCase
import com.voximplant.demos.sdk.core.domain.SilentLogInUseCase
import com.voximplant.demos.sdk.core.domain.StartCallUseCase
import com.voximplant.demos.sdk.core.model.data.AudioDevice
import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.CallState
import com.voximplant.demos.sdk.core.model.data.LoginError
import com.voximplant.demos.sdk.core.model.data.LoginState
import com.voximplant.demos.sdk.feature.audiocall.ongoing.navigation.OngoingCallArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioCallOngoingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    silentLogIn: SilentLogInUseCase,
    getLoginState: GetLoginStateUseCase,
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

    private val displayName = ongoingCallArgs.displayName

    private val loginState: MutableStateFlow<LoginState> = MutableStateFlow(LoginState.LoggedOut)

    val callOngoingUiState: StateFlow<CallOngoingUiState> = callUiState(
        displayName = displayName,
        loginStateFlow = loginState,
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
            displayName = displayName,
            call = getCall().stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null).value,
            state = CallState.Connecting,
            isMuted = getMuteState().stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = false).value,
            audioDevices = getAudioDevices().stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = emptyList()).value,
            audioDevice = getAudioDevice().stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = null).value,
        ),
    )

    init {
        viewModelScope.launch {
            if (getCallState().first() != CallState.Created) return@launch
            getLoginState().collect { loginState ->
                when (loginState) {
                    is LoginState.LoggedIn -> {
                        startCall(ongoingCallArgs.id)
                        cancel()
                    }

                    is LoginState.LoggedOut -> {
                        silentLogIn().onFailure { throwable ->
                            if (throwable is LoginError) {
                                this@AudioCallOngoingViewModel.loginState.value = LoginState.Failed(throwable)
                            }
                        }
                    }

                    is LoginState.Failed -> {
                        silentLogIn().onFailure { throwable ->
                            if (throwable is LoginError) {
                                viewModelScope.launch {
                                    this@AudioCallOngoingViewModel.loginState.value = LoginState.Failed(throwable)
                                    cancel()
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
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

fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7) -> R,
): Flow<R> = combine(
    combine(flow, flow2, flow3, ::Triple),
    combine(flow4, flow5, flow6, ::Triple),
    flow7,
) { t1, t2, t3 ->
    transform(
        t1.first,
        t1.second,
        t1.third,
        t2.first,
        t2.second,
        t2.third,
        t3,
    )
}


private fun callUiState(
    displayName: String?,
    loginStateFlow: Flow<LoginState>,
    callFlow: Flow<Call?>,
    stateFlow: Flow<CallState?>,
    isMutedFlow: Flow<Boolean>,
    isOnHoldFlow: Flow<Boolean>,
    audioDevicesFlow: Flow<List<AudioDevice>>,
    audioDeviceFlow: Flow<AudioDevice?>,
): Flow<CallOngoingUiState> = combine(
    loginStateFlow,
    callFlow,
    stateFlow,
    isMutedFlow,
    isOnHoldFlow,
    audioDevicesFlow,
    audioDeviceFlow,
) { loginState, call, state, isMuted, isOnHold, audioDevices, audioDevice ->
    if (call == null || state == null) {
        CallOngoingUiState.Inactive(state = CallState.Failed("Call not found"), displayName = displayName, isMuted = isMuted, audioDevices = audioDevices, audioDevice = audioDevice, call = null)
    } else if (loginState is LoginState.Failed) {
        CallOngoingUiState.Inactive(state = CallState.Failed(loginState.error.toString()), displayName = displayName, isMuted = isMuted, audioDevices = audioDevices, audioDevice = audioDevice, call = null)
    } else {
        when (state) {
            CallState.Created -> CallOngoingUiState.Inactive(state = CallState.Connecting, displayName = displayName, isMuted = isMuted, audioDevices = audioDevices, audioDevice = audioDevice, call = null)
            CallState.Connecting -> CallOngoingUiState.Paused(state = CallState.Connecting, displayName = displayName, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
            CallState.Connected -> CallOngoingUiState.Active(state = CallState.Connected, displayName = displayName, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
            CallState.Reconnecting -> CallOngoingUiState.Paused(state = CallState.Reconnecting, displayName = displayName, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
            CallState.Disconnecting -> CallOngoingUiState.Inactive(state = CallState.Disconnecting, displayName = displayName, isMuted = isMuted, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
            CallState.Disconnected -> CallOngoingUiState.Inactive(state = CallState.Disconnected, displayName = displayName, isMuted = isMuted, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
            is CallState.Failed -> CallOngoingUiState.Inactive(state = state, displayName = displayName, isMuted = isMuted, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
        }
    }
}

sealed class CallOngoingUiState(
    open val state: CallState,
    open val displayName: String?,
    open val isMuted: Boolean,
    open val isOnHold: Boolean,
    open val audioDevices: List<AudioDevice>,
    open val audioDevice: AudioDevice?,
    open val call: Call? = null,
) {
    data class Inactive(
        override val state: CallState,
        override val displayName: String?,
        override val isMuted: Boolean,
        override val audioDevices: List<AudioDevice>,
        override val audioDevice: AudioDevice?,
        override val call: Call?,
    ) : CallOngoingUiState(state, displayName, isMuted, false, audioDevices, audioDevice)

    data class Active(
        override val state: CallState,
        override val displayName: String?,
        override val isMuted: Boolean,
        override val isOnHold: Boolean,
        override val audioDevices: List<AudioDevice>,
        override val audioDevice: AudioDevice?,
        override val call: Call,
    ) : CallOngoingUiState(state, displayName, isMuted, isOnHold, audioDevices, audioDevice, call)

    data class Paused(
        override val state: CallState,
        override val displayName: String?,
        override val isMuted: Boolean,
        override val isOnHold: Boolean,
        override val audioDevices: List<AudioDevice>,
        override val audioDevice: AudioDevice?,
        override val call: Call,
    ) : CallOngoingUiState(state, displayName, isMuted, isOnHold, audioDevices, audioDevice, call)
}
