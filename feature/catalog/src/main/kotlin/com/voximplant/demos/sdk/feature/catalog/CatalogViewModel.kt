/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.demos.sdk.core.domain.GetCallUseCase
import com.voximplant.demos.sdk.core.domain.GetLoginStateUseCase
import com.voximplant.demos.sdk.core.domain.GetUserUseCase
import com.voximplant.demos.sdk.core.domain.LogOutUseCase
import com.voximplant.demos.sdk.core.domain.SilentLogInUseCase
import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.LoginError
import com.voximplant.demos.sdk.core.model.data.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    getLoginState: GetLoginStateUseCase,
    getUserUseCase: GetUserUseCase,
    getCall: GetCallUseCase,
    private val silentLogInUseCase: SilentLogInUseCase,
    private val logOutUseCase: LogOutUseCase,
) : ViewModel() {
    val user = getUserUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    private val _loginState: MutableStateFlow<LoginState> = MutableStateFlow(LoginState.LoggedOut)
    private val loginState: Flow<LoginState> = _loginState.asStateFlow()

    val catalogUiState: StateFlow<CatalogUiState> = catalogUiState(
        loginStateFlow = loginState,
        callFlow = getCall(),
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CatalogUiState(),
    )

    init {
        viewModelScope.launch {
            if (getLoginState().first() == LoginState.LoggedIn) {
                _loginState.value = LoginState.LoggedIn
                return@launch
            }

            _loginState.value = LoginState.LoggingIn

            silentLogInUseCase()
                .onSuccess {
                    _loginState.value = LoginState.LoggedIn
                }
                .onFailure { throwable ->
                    _loginState.value = LoginState.Failed(throwable as LoginError)
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logOutUseCase()
        }
    }
}

private fun catalogUiState(
    loginStateFlow: Flow<LoginState>,
    callFlow: Flow<Call?>,
): Flow<CatalogUiState> = combine(loginStateFlow, callFlow) { loginState, call ->
    CatalogUiState(
        loginState = loginState,
        call = call,
    )
}

data class CatalogUiState(
    val loginState: LoginState = LoginState.LoggedOut,
    val call: Call? = null,
)
