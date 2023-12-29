/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.demos.sdk.core.data.repository.UserDataRepository
import com.voximplant.demos.sdk.core.domain.GetCallUseCase
import com.voximplant.demos.sdk.core.domain.GetLoginStateUseCase
import com.voximplant.demos.sdk.core.domain.GetUserUseCase
import com.voximplant.demos.sdk.core.domain.LogOutUseCase
import com.voximplant.demos.sdk.core.domain.SilentLogInUseCase
import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.LoginError
import com.voximplant.demos.sdk.core.model.data.LoginState
import com.voximplant.demos.sdk.core.model.data.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    private val userDataRepository: UserDataRepository,
) : ViewModel() {
    private val _loginState: MutableStateFlow<LoginState> = MutableStateFlow(LoginState.LoggedOut)
    private val loginState: Flow<LoginState> = _loginState.asStateFlow()

    private val shouldShowNotificationPermissionRequest: Flow<Boolean> = userDataRepository.userData.map { !it.shouldHideNotificationPermissionRequest }

    val catalogUiState: StateFlow<CatalogUiState> = catalogUiState(
        userFlow = getUserUseCase(),
        loginStateFlow = loginState,
        callFlow = getCall(),
        shouldShowNotificationPermissionRequestFlow = shouldShowNotificationPermissionRequest,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CatalogUiState(
            user = User("", ""),
            shouldShowNotificationPermissionRequest = false,
        ),
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

    fun dismissNotificationPermissionRequest() {
        viewModelScope.launch {
            userDataRepository.setShouldHideNotificationPermissionRequest(true)
        }
    }
}

private fun catalogUiState(
    userFlow: Flow<User>,
    loginStateFlow: Flow<LoginState>,
    callFlow: Flow<Call?>,
    shouldShowNotificationPermissionRequestFlow: Flow<Boolean>,
): Flow<CatalogUiState> = combine(
    userFlow,
    loginStateFlow,
    callFlow,
    shouldShowNotificationPermissionRequestFlow,
) { user, loginState, call, shouldShowNotificationPermissionRequest ->
    CatalogUiState(
        user = user,
        loginState = loginState,
        call = call,
        shouldShowNotificationPermissionRequest = shouldShowNotificationPermissionRequest,
    )
}

data class CatalogUiState(
    val user: User,
    val loginState: LoginState = LoginState.LoggedOut,
    val call: Call? = null,
    val shouldShowNotificationPermissionRequest: Boolean,
)
