/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.audiocall.ongoing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.demos.sdk.core.domain.GetAudioDeviceUseCase
import com.voximplant.demos.sdk.core.domain.GetAudioDevicesUseCase
import com.voximplant.demos.sdk.core.domain.GetCallUseCase
import com.voximplant.demos.sdk.core.domain.GetHoldStateUseCase
import com.voximplant.demos.sdk.core.domain.GetLoginStateUseCase
import com.voximplant.demos.sdk.core.domain.GetMuteStateUseCase
import com.voximplant.demos.sdk.core.domain.HangUpCallUseCase
import com.voximplant.demos.sdk.core.domain.HoldCallUseCase
import com.voximplant.demos.sdk.core.domain.MuteCallUseCase
import com.voximplant.demos.sdk.core.domain.RefuseCallUseCase
import com.voximplant.demos.sdk.core.domain.SelectAudioDeviceUseCase
import com.voximplant.demos.sdk.core.domain.SendDtmfUseCase
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioCallOngoingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    silentLogIn: SilentLogInUseCase,
    getLoginState: GetLoginStateUseCase,
    getCall: GetCallUseCase,
    private val refuseCall: RefuseCallUseCase,
    getMuteState: GetMuteStateUseCase,
    getHoldState: GetHoldStateUseCase,
    getAudioDevices: GetAudioDevicesUseCase,
    getAudioDevice: GetAudioDeviceUseCase,
    private val selectAudioDeviceUseCase: SelectAudioDeviceUseCase,
    startCall: StartCallUseCase,
    private val muteCall: MuteCallUseCase,
    private val holdCall: HoldCallUseCase,
    private val hangUpCall: HangUpCallUseCase,
    private val sendDtmfUseCase: SendDtmfUseCase,
) : ViewModel() {
    private val ongoingCallArgs: OngoingCallArgs = OngoingCallArgs(savedStateHandle)

    private val displayName = ongoingCallArgs.displayName

    private val loginState: MutableStateFlow<LoginState> = MutableStateFlow(LoginState.LoggedOut)

    val callOngoingUiState: StateFlow<CallOngoingUiState> = callUiState(
        displayName = displayName,
        loginStateFlow = loginState,
        callFlow = getCall(),
        isMutedFlow = getMuteState(),
        isOnHoldFlow = getHoldState(),
        audioDevicesFlow = getAudioDevices(),
        audioDeviceFlow = getAudioDevice(),
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CallOngoingUiState.Inactive(
            displayName = displayName,
            call = getCall().stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null).value,
            isMuted = getMuteState().stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = false).value,
            isOnHold = getHoldState().stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = false).value,
            audioDevices = getAudioDevices().stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = emptyList()).value,
            audioDevice = getAudioDevice().stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = null).value,
        ),
    )

    init {
        viewModelScope.launch {
            if (getCall().firstOrNull()?.state !in listOf(CallState.Created, CallState.Reconnecting)) return@launch

            getLoginState().collect { loginState ->
                when (loginState) {
                    is LoginState.LoggedIn -> {
                        this@AudioCallOngoingViewModel.loginState.value = LoginState.LoggedIn
                        startCall(ongoingCallArgs.id)
                        cancel()
                    }

                    is LoginState.LoggedOut -> {
                        this@AudioCallOngoingViewModel.loginState.value = LoginState.LoggingIn
                        silentLogIn().onFailure { throwable ->
                            if (throwable is LoginError) {
                                this@AudioCallOngoingViewModel.loginState.value = LoginState.Failed(throwable)
                                cancel()
                            }
                        }
                    }

                    is LoginState.Failed -> {
                        this@AudioCallOngoingViewModel.loginState.value = LoginState.LoggingIn
                        silentLogIn().onFailure { throwable ->
                            if (throwable is LoginError) {
                                this@AudioCallOngoingViewModel.loginState.value = LoginState.Failed(throwable)
                                cancel()
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

    fun sendDTMF(value: String) {
        viewModelScope.launch {
            sendDtmfUseCase(value)
        }
    }

    fun hangUp() {
        viewModelScope.launch {
            hangUpCall()
        }
    }

    override fun onCleared() {
        super.onCleared()
        callOngoingUiState.value.call?.let { call ->
            if (call.state is CallState.Created || call.state is CallState.Disconnected || call.state is CallState.Failed) {
                refuseCall(call)
            }
        }
    }

}

fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    transform: suspend (T1, T2, T3, T4, T5, T6) -> R,
): Flow<R> = combine(
    combine(flow, flow2, flow3, ::Triple),
    combine(flow4, flow5, flow6, ::Triple),
) { t1, t2 ->
    transform(
        t1.first,
        t1.second,
        t1.third,
        t2.first,
        t2.second,
        t2.third,
    )
}


private fun callUiState(
    displayName: String?,
    loginStateFlow: Flow<LoginState>,
    callFlow: Flow<Call?>,
    isMutedFlow: Flow<Boolean>,
    isOnHoldFlow: Flow<Boolean>,
    audioDevicesFlow: Flow<List<AudioDevice>>,
    audioDeviceFlow: Flow<AudioDevice?>,
): Flow<CallOngoingUiState> = combine(
    loginStateFlow,
    callFlow,
    isMutedFlow,
    isOnHoldFlow,
    audioDevicesFlow,
    audioDeviceFlow,
) { loginState, call, isMuted, isOnHold, audioDevices, audioDevice ->
    if (call == null) {
        CallOngoingUiState.Failed(reason = "Call not found", displayName = displayName, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = null)
    } else if (loginState is LoginState.LoggingIn) {
        CallOngoingUiState.Connecting(displayName = displayName, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
    } else if (loginState is LoginState.Failed) {
        CallOngoingUiState.Failed(reason = loginState.error.toString(), displayName = displayName, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = null)
    } else {
        when (call.state) {
            CallState.Created -> CallOngoingUiState.Inactive(displayName = displayName, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = null)
            CallState.Connecting -> CallOngoingUiState.Connecting(displayName = displayName, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
            CallState.Connected -> CallOngoingUiState.Active(displayName = displayName, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
            CallState.Reconnecting -> CallOngoingUiState.Connecting(displayName = displayName, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
            CallState.Disconnecting -> CallOngoingUiState.Inactive(displayName = displayName, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
            CallState.Disconnected -> CallOngoingUiState.Inactive(displayName = displayName, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
            is CallState.Failed -> CallOngoingUiState.Failed(reason = (call.state as CallState.Failed).description ?: "Call failed", displayName = displayName, isMuted = isMuted, isOnHold = isOnHold, audioDevices = audioDevices, audioDevice = audioDevice, call = call)
        }
    }
}

sealed class CallOngoingUiState(
    open val displayName: String?,
    open val isMuted: Boolean,
    open val isOnHold: Boolean,
    open val audioDevices: List<AudioDevice>,
    open val audioDevice: AudioDevice?,
    open val call: Call? = null,
) {
    data class Inactive(
        override val displayName: String?,
        override val isMuted: Boolean,
        override val isOnHold: Boolean,
        override val audioDevices: List<AudioDevice>,
        override val audioDevice: AudioDevice?,
        override val call: Call?,
    ) : CallOngoingUiState(displayName, isMuted, false, audioDevices, audioDevice)

    data class Active(
        override val displayName: String?,
        override val isMuted: Boolean,
        override val isOnHold: Boolean,
        override val audioDevices: List<AudioDevice>,
        override val audioDevice: AudioDevice?,
        override val call: Call,
    ) : CallOngoingUiState(displayName, isMuted, isOnHold, audioDevices, audioDevice, call)

    data class Connecting(
        override val displayName: String?,
        override val isMuted: Boolean,
        override val isOnHold: Boolean,
        override val audioDevices: List<AudioDevice>,
        override val audioDevice: AudioDevice?,
        override val call: Call,
    ) : CallOngoingUiState(displayName, isMuted, isOnHold, audioDevices, audioDevice, call)

    data class Failed(
        val reason: String,
        override val displayName: String?,
        override val isMuted: Boolean,
        override val isOnHold: Boolean,
        override val audioDevices: List<AudioDevice>,
        override val audioDevice: AudioDevice?,
        override val call: Call?,
    ) : CallOngoingUiState(displayName, isMuted, isOnHold, audioDevices, audioDevice, call)
}
