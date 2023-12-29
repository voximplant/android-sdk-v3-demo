/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.audiocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.demos.sdk.core.data.repository.UserDataRepository
import com.voximplant.demos.sdk.core.domain.CreateCallUseCase
import com.voximplant.demos.sdk.core.domain.GetCallUseCase
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
class AudioCallViewModel @Inject constructor(
    getUserUseCase: GetUserUseCase,
    private val getLoginState: GetLoginStateUseCase,
    private val silentLogIn: SilentLogInUseCase,
    private val createCallUseCase: CreateCallUseCase,
    getCall: GetCallUseCase,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {
    private val shouldShowNotificationPermissionRequest: Flow<Boolean> = userDataRepository.userData.map { !it.shouldHideNotificationPermissionRequest }
    private val shouldShowMicrophonePermissionRequest: Flow<Boolean> = userDataRepository.userData.map { !it.shouldHideMicrophonePermissionRequest }

    val audioCallUiState: StateFlow<AudioCallUiState> = audioCallUiState(
        userFlow = getUserUseCase(),
        callFlow = getCall(),
        shouldShowNotificationPermissionRequest,
        shouldShowMicrophonePermissionRequest,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AudioCallUiState.Inactive(
            user = User("", ""),
            shouldShowNotificationPermissionRequest = false,
            shouldShowMicrophonePermissionRequest = false,
        ),
    )

    suspend fun createCall(username: String): Result<Call> {
        return suspendCoroutine { continuation ->
            viewModelScope.launch {
                getLoginState().collect { loginState ->
                    when (loginState) {
                        LoginState.LoggedIn -> {
                            continuation.resume(createCallUseCase(username))
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
}

private fun audioCallUiState(
    userFlow: Flow<User>,
    callFlow: Flow<Call?>,
    shouldShowNotificationPermissionRequestFlow: Flow<Boolean>,
    shouldShowMicrophonePermissionRequestFlow: Flow<Boolean>,
): Flow<AudioCallUiState> = combine(
    userFlow,
    callFlow,
    shouldShowNotificationPermissionRequestFlow,
    shouldShowMicrophonePermissionRequestFlow,
) { user, call, shouldShowNotificationPermissionRequest, shouldShowMicrophonePermissionRequest ->
    if (call == null) {
        AudioCallUiState.Inactive(
            user = user,
            shouldShowNotificationPermissionRequest = shouldShowNotificationPermissionRequest,
            shouldShowMicrophonePermissionRequest = shouldShowMicrophonePermissionRequest,
        )
    } else {
        AudioCallUiState.Active(user = user, call = call)
    }
}

sealed class AudioCallUiState(
    open val user: User,
) {
    data class Active(override val user: User, val call: Call) : AudioCallUiState(user)
    data class Inactive(
        override val user: User,
        val shouldShowNotificationPermissionRequest: Boolean,
        val shouldShowMicrophonePermissionRequest: Boolean,
    ) : AudioCallUiState(user)
}
