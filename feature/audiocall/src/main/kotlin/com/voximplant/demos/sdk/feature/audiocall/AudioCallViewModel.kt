/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.audiocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.demos.sdk.core.domain.CreateCallUseCase
import com.voximplant.demos.sdk.core.domain.GetCallUseCase
import com.voximplant.demos.sdk.core.domain.GetLoginStateUseCase
import com.voximplant.demos.sdk.core.domain.GetUserUseCase
import com.voximplant.demos.sdk.core.domain.SilentLogInUseCase
import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
) : ViewModel() {
    val user = getUserUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    val audioCallUiState: StateFlow<AudioCallUiState> = audioCallUiState(
        callFlow = getCall(),
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AudioCallUiState.Inactive,
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
}

private fun audioCallUiState(
    callFlow: Flow<Call?>,
): Flow<AudioCallUiState> = combine(callFlow) {
    val call = it[0]
    if (call == null) {
        AudioCallUiState.Inactive
    } else {
        AudioCallUiState.Active(call = call)
    }
}

sealed interface AudioCallUiState {
    data class Active(val call: Call) : AudioCallUiState
    data object Inactive : AudioCallUiState
}
