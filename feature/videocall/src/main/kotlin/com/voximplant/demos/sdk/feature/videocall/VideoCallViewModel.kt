/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.videocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.demos.sdk.core.data.repository.UserDataRepository
import com.voximplant.demos.sdk.core.data.repository.VideoCallRepository
import com.voximplant.demos.sdk.core.domain.GetLoginStateUseCase
import com.voximplant.demos.sdk.core.domain.GetUserUseCase
import com.voximplant.demos.sdk.core.domain.SilentLogInUseCase
import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.LoginState
import com.voximplant.demos.sdk.core.model.data.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class VideoCallViewModel @Inject constructor(
    getUserUseCase: GetUserUseCase,
    private val getLoginState: GetLoginStateUseCase,
    private val silentLogIn: SilentLogInUseCase,
    private val videoCallRepository: VideoCallRepository,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {
    private val shouldShowNotificationPermissionRequest: Flow<Boolean> =
        userDataRepository.userData.map { !it.shouldHideNotificationPermissionRequest }
    private val shouldShowMicrophonePermissionRequest: Flow<Boolean> =
        userDataRepository.userData.map { !it.shouldHideMicrophonePermissionRequest }
    private val shouldShowCameraPermissionRequest: Flow<Boolean> =
        userDataRepository.userData.map { !it.shouldHideCameraPermissionRequest }

    val videoCallUiState: StateFlow<VideoCallUiState> = videoCallUiState(
        userFlow = getUserUseCase(),
        callFlow = videoCallRepository.callFlow,
        shouldShowNotificationPermissionRequest,
        shouldShowMicrophonePermissionRequest,
        shouldShowCameraPermissionRequest
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VideoCallUiState.Inactive(
            user = User("", ""),
            shouldShowNotificationPermissionRequest = false,
            shouldShowMicrophonePermissionRequest = false,
            shouldShowCameraPermissionRequest = false
        ),
    )

    suspend fun createCall(username: String): Result<Call> {
        return suspendCoroutine { continuation ->
            viewModelScope.launch {
                getLoginState().collect { loginState ->
                    when (loginState) {
                        LoginState.LoggedIn -> {
                            continuation.resume(videoCallRepository.createCall(username))
                            cancel()
                        }

                        LoginState.LoggedOut -> {
                            silentLogIn().onFailure { throwable ->
                                continuation.resume(Result.failure(throwable))
                                cancel()
                            }
                        }

                        is LoginState.Failed -> {
                            silentLogIn().onFailure { throwable ->
                                continuation.resume(Result.failure(throwable))
                                cancel()
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun dismissNotificationPermissionRequest() {
        viewModelScope.launch {
            userDataRepository.setShouldHideNotificationPermissionRequest(true)
        }
    }

    fun dismissMicrophonePermissionRequest() {
        viewModelScope.launch {
            userDataRepository.setShouldHideMicrophonePermissionRequest(true)
        }
    }

    fun dismissCameraPermissionRequest() {
        viewModelScope.launch {
            userDataRepository.setShouldHideCameraPermissionRequest(true)
        }
    }
}

private fun videoCallUiState(
    userFlow: Flow<User>,
    callFlow: Flow<Call?>,
    shouldShowNotificationPermissionRequestFlow: Flow<Boolean>,
    shouldShowMicrophonePermissionRequestFlow: Flow<Boolean>,
    shouldShowCameraPermissionRequestFlow: Flow<Boolean>,
): Flow<VideoCallUiState> = combine(
    userFlow,
    callFlow,
    shouldShowNotificationPermissionRequestFlow,
    shouldShowMicrophonePermissionRequestFlow,
    shouldShowCameraPermissionRequestFlow,
) { user, call, shouldShowNotificationPermissionRequest, shouldShowMicrophonePermissionRequest, shouldShowCameraPermissionRequest ->
    if (call == null) {
        VideoCallUiState.Inactive(
            user = user,
            shouldShowNotificationPermissionRequest = shouldShowNotificationPermissionRequest,
            shouldShowMicrophonePermissionRequest = shouldShowMicrophonePermissionRequest,
            shouldShowCameraPermissionRequest = shouldShowCameraPermissionRequest
        )
    } else {
        VideoCallUiState.Active(user = user)
    }
}

sealed class VideoCallUiState(
    open val user: User,
) {
    data class Active(
        override val user: User
    ) : VideoCallUiState(user)

    data class Inactive(
        override val user: User,
        val shouldShowNotificationPermissionRequest: Boolean,
        val shouldShowMicrophonePermissionRequest: Boolean,
        val shouldShowCameraPermissionRequest: Boolean,
    ) : VideoCallUiState(user)
}
