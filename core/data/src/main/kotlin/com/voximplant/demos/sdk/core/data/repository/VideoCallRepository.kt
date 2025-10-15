/*
 * Copyright (c) 2011 - 2025, Voximplant, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import com.voximplant.android.sdk.calls.LocalVideoStream
import com.voximplant.android.sdk.calls.RemoteVideoStream
import com.voximplant.android.sdk.core.audio.AudioDeviceType
import com.voximplant.demos.sdk.core.calls.CallDataSource
import com.voximplant.demos.sdk.core.calls.model.CallTypeApi
import com.voximplant.demos.sdk.core.camera.manager.CameraDeviceManager
import com.voximplant.demos.sdk.core.video.manager.LocalVideoManager
import com.voximplant.demos.sdk.core.calls.model.asCall
import com.voximplant.demos.sdk.core.calls.model.callTypeMap
import com.voximplant.demos.sdk.core.common.VoxBroadcastReceiver
import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.CallDirection
import com.voximplant.demos.sdk.core.model.data.CallState
import com.voximplant.demos.sdk.core.data.services.VideoCallIncomingService
import com.voximplant.demos.sdk.core.notifications.Notifier
import com.voximplant.demos.sdk.core.data.services.VideoCallOngoingService
import com.voximplant.demos.sdk.core.model.data.CallType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class VideoCallRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localVideoManager: LocalVideoManager,
    private val cameraDeviceManager: CameraDeviceManager,
    private val notifier: Notifier,
    private val callDataSource: CallDataSource,
    private val coroutineScope: CoroutineScope,
    private val audioDeviceRepository: AudioDeviceRepository,
) {
    val localVideoStream: StateFlow<LocalVideoStream?> = localVideoManager.localVideoStream
    val remoteVideoStream: StateFlow<RemoteVideoStream?> = callDataSource.remoteVideoStreamFlow
    val isCameraEnabled: StateFlow<Boolean> = localVideoStream
        .map { it != null }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = true
        )

    val callFlow: Flow<Call?>
        get() = combine(
            callDataSource.callApiDataFlow,
            callDataSource.duration
        ) { callApiData, duration ->
            if (callApiData == null) return@combine null
            if (callApiData.type != CallTypeApi.VideoCall && callApiData.state != CallState.Disconnected) return@combine null
            Call(
                id = callApiData.id,
                state = callApiData.state,
                direction = callApiData.direction,
                duration = duration,
                remoteDisplayName = callApiData.remoteDisplayName,
                remoteSipUri = callApiData.remoteSipUri,
                type = callTypeMap(callApiData.type)
            )
        }

    val isMicrophoneMuted: Flow<Boolean>
        get() = callDataSource.isMuted

    private val br = VoxBroadcastReceiver(
        onHangUpReceived = {
            hangUp()
        },
        onRejectReceived = {
            reject()
        },
        onToggleMuteReceived = {
            toggleMute()
        },
        onToggleCameraReceived = {
            toggleCameraEnabled()
        },
        onAnswerReceived = {
            coroutineScope.launch {
                callFlow.firstOrNull()?.let { call ->
                    startCall(call.id, call.remoteDisplayName ?: "unknown user")
                }
            }
        },
    )

    private val videoCallIncomingService = Intent(context, VideoCallIncomingService::class.java)
    private val videoCallOngoingService = Intent(context, VideoCallOngoingService::class.java)

    private var pushHandled: Boolean = false

    private fun startIncomingCallService(call: Call) {
        pushHandled = false
        videoCallIncomingService.apply {
            putExtra("id", call.id)
            putExtra("displayName", call.remoteDisplayName)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(videoCallIncomingService)
        } else {
            context.startService(videoCallIncomingService)
        }
    }

    init {
        coroutineScope.launch {
            callFlow.collect { call ->
                when (call?.state) {
                    is CallState.Created -> {
                        if (call.direction == CallDirection.INCOMING) {
                            br.register(context)
                            if (pushHandled) {
                                startIncomingCallService(call)
                            } else {
                                notifier.postIncomingVideoCallNotification(
                                    call.id,
                                    call.remoteDisplayName
                                )
                            }
                        }
                    }

                    is CallState.Connected -> {
                        if (call.duration != 0L) return@collect

                        br.register(context)
                        videoCallOngoingService.apply {
                            putExtra("id", call.id)
                            putExtra("displayName", call.remoteDisplayName)
                            putExtra("isOngoing", true)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(videoCallOngoingService)
                        } else {
                            context.startService(videoCallOngoingService)
                        }
                    }

                    is CallState.Disconnected,
                    is CallState.Failed,
                    null,
                        -> {
                        coroutineScope.launch {
                            releaseLocalVideo()
                        }
                        br.unregister(context)
                        notifier.cancelCallNotification()
                        context.stopService(videoCallIncomingService)
                        context.stopService(videoCallOngoingService)
                        pushHandled = false
                    }

                    else -> {}
                }
            }
        }
    }

    suspend fun createCall(username: String): Result<Call> {
        createLocalVideoStream()
        callDataSource.createCall(username, localVideoStream.value).let { callDataResult ->
            callDataResult.fold(
                onSuccess = { callData ->
                    return Result.success(callData.asCall())
                },
                onFailure = { throwable ->
                    releaseLocalVideo()
                    return Result.failure(throwable)
                },
            )
        }
    }

    suspend fun startCall(id: String, displayName: String): Result<Call> {
        audioDeviceRepository.setDefaultAudioDeviceType(AudioDeviceType.Speaker)
        notifier.cancelCallNotification()
        context.stopService(videoCallIncomingService)
        pushHandled = false
        br.register(context)
        videoCallOngoingService.apply {
            putExtra("id", id)
            putExtra("displayName", displayName)
            putExtra("isOngoing", false)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(videoCallOngoingService)
        } else {
            context.startService(videoCallOngoingService)
        }
        createLocalVideoStream()
        callDataSource.startCall(id, localVideoStream.value).let { callDataResult ->
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

    fun handlePush() {
        pushHandled = true

        coroutineScope.launch {
            callFlow.firstOrNull()?.let { call ->
                if (call.type == CallType.VideoCall && call.state is CallState.Created && call.direction == CallDirection.INCOMING) {
                    startIncomingCallService(call)
                }
            }
        }
    }

    fun clearCall(call: Call) {
        callDataSource.clearCall(call)
        coroutineScope.launch {
            releaseLocalVideo()
        }
    }

    fun hangUp() {
        context.stopService(videoCallOngoingService)
        callDataSource.hangUp()
    }

    fun reject() {
        notifier.cancelCallNotification()
        context.stopService(videoCallIncomingService)
        callDataSource.reject()
        pushHandled = false
    }

    fun toggleMute() {
        callDataSource.toggleMute()
    }

    private var toggleCameraAvailable = true

    fun toggleCameraEnabled() {
        if (toggleCameraAvailable) {
            toggleCameraAvailable = false
            coroutineScope.launch {
                if (localVideoStream.value != null) {
                    stopSendingLocalVideo()
                } else {
                    startSendingLocalVideo()
                }
                toggleCameraAvailable = true
            }
        }
    }

    private var stopSendingLocalVideoAvailable = true

    fun stopSendingLocalVideo() {
        if (stopSendingLocalVideoAvailable) {
            callDataSource.stopSendingLocalVideo { isStopSendingLocalVideo ->
                if (isStopSendingLocalVideo) {
                    coroutineScope.launch {
                        releaseLocalVideo()
                        stopSendingLocalVideoAvailable = true
                    }
                }
            }
        }
    }

    suspend fun startSendingLocalVideo() {
        createLocalVideoStream()
        localVideoStream.value?.let {
            callDataSource.startSendingLocalVideo(it) { isStartSendingLocalVideo ->
                if (!isStartSendingLocalVideo) {
                    coroutineScope.launch {
                        releaseLocalVideo()
                    }
                }
            }
        }
    }

    private suspend fun createLocalVideoStream() {
        if (localVideoStream.value == null) {
            localVideoManager.createLocalVideoStream(cameraDeviceManager.cameraVideoSource)
        }
    }

    private suspend fun releaseLocalVideo() {
        localVideoManager.releaseLocalVideoStream()
    }

    fun switchCameraDevice() {
        cameraDeviceManager.switchCameraDevice()
    }
}