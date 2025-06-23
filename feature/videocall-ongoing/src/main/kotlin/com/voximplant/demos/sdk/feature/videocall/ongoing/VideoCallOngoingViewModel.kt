/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.videocall.ongoing

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.voximplant.android.sdk.calls.LocalVideoStream
import com.voximplant.android.sdk.calls.RemoteVideoStream
import com.voximplant.demos.sdk.core.data.repository.AudioDeviceRepository
import com.voximplant.demos.sdk.core.data.repository.VideoCallRepository
import com.voximplant.demos.sdk.core.domain.GetLoginStateUseCase
import com.voximplant.demos.sdk.core.domain.SilentLogInUseCase
import com.voximplant.demos.sdk.core.model.data.AudioDevice
import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.CallState
import com.voximplant.demos.sdk.core.model.data.LoginError
import com.voximplant.demos.sdk.core.model.data.LoginState
import com.voximplant.demos.sdk.feature.videocall.ongoing.navigation.OngoingVideoCallArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoCallOngoingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    silentLogIn: SilentLogInUseCase,
    getLoginState: GetLoginStateUseCase,
    private val audioDeviceRepository: AudioDeviceRepository,
    private val videoCallRepository: VideoCallRepository,
) : ViewModel() {
    private val ongoingVideoCallArgs: OngoingVideoCallArgs = OngoingVideoCallArgs(savedStateHandle)
    private val displayName = ongoingVideoCallArgs.displayName

    private val loginState: MutableStateFlow<LoginState> = MutableStateFlow(LoginState.LoggedOut)

    val videoCallOngoingUiState: StateFlow<VideoCallOngoingUiState> = videoCallUiState(
        displayName = displayName,
        loginStateFlow = loginState,
        callFlow = videoCallRepository.callFlow,
        isMicrophoneMutedFlow = videoCallRepository.isMicrophoneMuted,
        cameraEnabledFlow = videoCallRepository.isCameraEnabled,
        audioDevicesFlow = audioDeviceRepository.audioDevices,
        audioDeviceFlow = audioDeviceRepository.selectedAudioDevice,
        localVideoStreamFlow = videoCallRepository.localVideoStream,
        remoteVideoStreamFlow = videoCallRepository.remoteVideoStream,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = VideoCallOngoingUiState.Inactive(
            displayName = displayName,
            call = videoCallRepository.callFlow.stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = null).value,
            isMicrophoneMuted = videoCallRepository.isMicrophoneMuted.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = false).value,
            cameraEnabled = videoCallRepository.isCameraEnabled.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = false).value,
            audioDevices = audioDeviceRepository.audioDevices.stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = emptyList()).value,
            audioDevice = audioDeviceRepository.selectedAudioDevice.stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = null).value,
            localVideoStream = videoCallRepository.localVideoStream.stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = null).value,
            remoteVideoStream = videoCallRepository.remoteVideoStream.stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = null).value
        )
    )

    init {
        viewModelScope.launch {
            if (videoCallRepository.callFlow.firstOrNull()?.state !in listOf(CallState.Created, CallState.Reconnecting)) return@launch

            getLoginState().collect { loginState ->
                when (loginState) {
                    is LoginState.LoggedIn -> {
                        this@VideoCallOngoingViewModel.loginState.value = LoginState.LoggedIn
                        videoCallRepository.startCall(ongoingVideoCallArgs.id, displayName ?: "unknown user")
                        cancel()
                    }

                    is LoginState.LoggedOut -> {
                        this@VideoCallOngoingViewModel.loginState.value = LoginState.LoggingIn
                        silentLogIn().onFailure { throwable ->
                            if (throwable is LoginError) {
                                this@VideoCallOngoingViewModel.loginState.value = LoginState.Failed(throwable)
                                cancel()
                            }
                        }
                    }

                    is LoginState.Failed -> {
                        this@VideoCallOngoingViewModel.loginState.value = LoginState.LoggingIn
                        silentLogIn().onFailure { throwable ->
                            if (throwable is LoginError) {
                                this@VideoCallOngoingViewModel.loginState.value = LoginState.Failed(throwable)
                                cancel()
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun stopSendingLocalVideo() {
        videoCallRepository.stopSendingLocalVideo()
    }

    fun startSendingLocalVideo() {
        viewModelScope.launch {
            videoCallRepository.startSendingLocalVideo()
        }
    }

    fun toggleMute() {
        videoCallRepository.toggleMute()
    }

    fun toggleCameraEnabled() {
        videoCallRepository.toggleCameraEnabled()
    }

    fun switchCamera() {
        videoCallRepository.switchCameraDevice()
    }

    fun selectAudioDevice(audioDevice: AudioDevice) {
        audioDeviceRepository.selectAudioDevice(audioDevice)
    }

    fun hangUpCall() {
        viewModelScope.launch {
            videoCallRepository.hangUp()
        }
    }

    override fun onCleared() {
        super.onCleared()
        videoCallOngoingUiState.value.call?.let { call ->
            if (call.state is CallState.Created || call.state is CallState.Disconnected || call.state is CallState.Failed) {
                videoCallRepository.clearCall(call)
            }
        }
    }
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8) -> R,
): Flow<R> = combine(
    combine(flow, flow2, flow3, ::Triple),
    combine(flow4, flow5, flow6, ::Triple),
    flow7, flow8,
) { t1, t2, t3, t4 ->
    transform(
        t1.first,
        t1.second,
        t1.third,
        t2.first,
        t2.second,
        t2.third,
        t3,
        t4,
    )
}

private fun videoCallUiState(
    displayName: String?,
    loginStateFlow: Flow<LoginState>,
    callFlow: Flow<Call?>,
    localVideoStreamFlow: Flow<LocalVideoStream?>,
    isMicrophoneMutedFlow: Flow<Boolean>,
    cameraEnabledFlow: Flow<Boolean>,
    audioDevicesFlow: Flow<List<AudioDevice>>,
    audioDeviceFlow: Flow<AudioDevice?>,
    remoteVideoStreamFlow: Flow<RemoteVideoStream?>,
): Flow<VideoCallOngoingUiState> = combine(
    loginStateFlow,
    callFlow,
    isMicrophoneMutedFlow,
    cameraEnabledFlow,
    audioDevicesFlow,
    audioDeviceFlow,
    localVideoStreamFlow,
    remoteVideoStreamFlow,
) { loginState, call, isMicrophoneMuted, cameraEnabled, audioDevices, audioDevice, localVideoStream, remoteVideoStream ->
    if (call == null) {
        VideoCallOngoingUiState.Inactive(displayName = displayName, isMicrophoneMuted = isMicrophoneMuted, cameraEnabled = cameraEnabled, audioDevices = audioDevices, audioDevice = audioDevice, call = null, localVideoStream = localVideoStream, remoteVideoStream = remoteVideoStream)
    } else if (loginState is LoginState.LoggingIn) {
        VideoCallOngoingUiState.Connecting(displayName = displayName, isMicrophoneMuted = isMicrophoneMuted, cameraEnabled = cameraEnabled, audioDevices = audioDevices, audioDevice = audioDevice, call = call, localVideoStream = localVideoStream, remoteVideoStream = remoteVideoStream)
    } else if (loginState is LoginState.Failed) {
        VideoCallOngoingUiState.Failed(reason = loginState.error.toString(), displayName = displayName, isMicrophoneMuted = isMicrophoneMuted, cameraEnabled = cameraEnabled, audioDevices = audioDevices, audioDevice = audioDevice, call = null, localVideoStream = localVideoStream, remoteVideoStream = remoteVideoStream)
    } else {
        when (call.state) {
            CallState.Created -> VideoCallOngoingUiState.Inactive(displayName = displayName, isMicrophoneMuted = isMicrophoneMuted, cameraEnabled = cameraEnabled, audioDevices = audioDevices, audioDevice = audioDevice, call = null, localVideoStream = localVideoStream, remoteVideoStream = remoteVideoStream)
            CallState.Connecting -> VideoCallOngoingUiState.Connecting(displayName = displayName, isMicrophoneMuted = isMicrophoneMuted, cameraEnabled = cameraEnabled, audioDevices = audioDevices, audioDevice = audioDevice, call = call, localVideoStream = localVideoStream, remoteVideoStream = remoteVideoStream)
            CallState.Connected -> VideoCallOngoingUiState.Active(displayName = displayName, isMicrophoneMuted = isMicrophoneMuted, cameraEnabled = cameraEnabled, audioDevices = audioDevices, audioDevice = audioDevice, call = call, localVideoStream = localVideoStream, remoteVideoStream = remoteVideoStream)
            CallState.Reconnecting -> VideoCallOngoingUiState.Connecting(displayName = displayName, isMicrophoneMuted = isMicrophoneMuted, cameraEnabled = cameraEnabled, audioDevices = audioDevices, audioDevice = audioDevice, call = call, localVideoStream = localVideoStream, remoteVideoStream = remoteVideoStream)
            CallState.Disconnecting -> VideoCallOngoingUiState.Inactive(displayName = displayName, isMicrophoneMuted = isMicrophoneMuted, cameraEnabled = cameraEnabled, audioDevices = audioDevices, audioDevice = audioDevice, call = call, localVideoStream = localVideoStream, remoteVideoStream = remoteVideoStream)
            CallState.Disconnected -> VideoCallOngoingUiState.Inactive(displayName = displayName, isMicrophoneMuted = isMicrophoneMuted, cameraEnabled = cameraEnabled, audioDevices = audioDevices, audioDevice = audioDevice, call = call, localVideoStream = localVideoStream,remoteVideoStream = remoteVideoStream)
            is CallState.Failed -> VideoCallOngoingUiState.Failed(reason = (call.state as CallState.Failed).description ?: "Call failed", displayName = displayName, isMicrophoneMuted = isMicrophoneMuted, cameraEnabled = cameraEnabled, audioDevices = audioDevices, audioDevice = audioDevice, call = call, localVideoStream = localVideoStream, remoteVideoStream = remoteVideoStream)
        }
    }
}

sealed class VideoCallOngoingUiState(
    open val displayName: String?,
    open val isMicrophoneMuted: Boolean,
    open val cameraEnabled: Boolean,
    open val audioDevices: List<AudioDevice>,
    open val audioDevice: AudioDevice?,
    open val call: Call? = null,
    open val localVideoStream: LocalVideoStream?,
    open val remoteVideoStream: RemoteVideoStream?,
) {
    data class Inactive(
        override val displayName: String?,
        override val isMicrophoneMuted: Boolean,
        override val cameraEnabled: Boolean,
        override val audioDevices: List<AudioDevice>,
        override val audioDevice: AudioDevice?,
        override val call: Call?,
        override val localVideoStream: LocalVideoStream?,
        override val remoteVideoStream: RemoteVideoStream?,
    ) : VideoCallOngoingUiState(displayName, isMicrophoneMuted, cameraEnabled, audioDevices, audioDevice, localVideoStream = localVideoStream, remoteVideoStream = remoteVideoStream)

    data class Active(
        override val displayName: String?,
        override val isMicrophoneMuted: Boolean,
        override val cameraEnabled: Boolean,
        override val audioDevices: List<AudioDevice>,
        override val audioDevice: AudioDevice?,
        override val call: Call,
        override val localVideoStream: LocalVideoStream?,
        override val remoteVideoStream: RemoteVideoStream?,
    ) : VideoCallOngoingUiState(displayName, isMicrophoneMuted, cameraEnabled, audioDevices, audioDevice, call, localVideoStream, remoteVideoStream)

    data class Connecting(
        override val displayName: String?,
        override val isMicrophoneMuted: Boolean,
        override val cameraEnabled: Boolean,
        override val audioDevices: List<AudioDevice>,
        override val audioDevice: AudioDevice?,
        override val call: Call,
        override val localVideoStream: LocalVideoStream?,
        override val remoteVideoStream: RemoteVideoStream?,
    ) : VideoCallOngoingUiState(displayName, isMicrophoneMuted, cameraEnabled, audioDevices, audioDevice, call, localVideoStream, remoteVideoStream)

    data class Failed(
        val reason: String,
        override val displayName: String?,
        override val isMicrophoneMuted: Boolean,
        override val cameraEnabled: Boolean,
        override val audioDevices: List<AudioDevice>,
        override val audioDevice: AudioDevice?,
        override val call: Call?,
        override val localVideoStream: LocalVideoStream?,
        override val remoteVideoStream: RemoteVideoStream?,
    ) : VideoCallOngoingUiState(displayName, isMicrophoneMuted, cameraEnabled, audioDevices, audioDevice, call, localVideoStream, remoteVideoStream)
}
