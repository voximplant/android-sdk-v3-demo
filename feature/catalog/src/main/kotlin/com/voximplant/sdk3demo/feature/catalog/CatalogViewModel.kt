package com.voximplant.sdk3demo.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.sdk3demo.core.domain.GetCallStateUseCase
import com.voximplant.sdk3demo.core.domain.GetCallUseCase
import com.voximplant.sdk3demo.core.domain.GetUserUseCase
import com.voximplant.sdk3demo.core.domain.LogOutUseCase
import com.voximplant.sdk3demo.core.domain.SilentLogInUseCase
import com.voximplant.sdk3demo.core.model.data.Call
import com.voximplant.sdk3demo.core.model.data.CallApiState
import com.voximplant.sdk3demo.core.model.data.LoginError
import com.voximplant.sdk3demo.core.model.data.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    getUserUseCase: GetUserUseCase,
    getCall: GetCallUseCase,
    getCallState: GetCallStateUseCase,
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
        callStateFlow = getCallState(),
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CatalogUiState(),
    )

    init {
        viewModelScope.launch {
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
    callStateFlow: Flow<CallApiState?>,
): Flow<CatalogUiState> = combine(loginStateFlow, callFlow, callStateFlow) { loginState, call, callState ->
    CatalogUiState(
        loginState = loginState,
        call = call,
        callState = when (callState) {
            CallApiState.CREATED -> CallState.Created
            CallApiState.CONNECTING -> CallState.Connecting
            CallApiState.CONNECTED -> CallState.Connected
            CallApiState.RECONNECTING -> CallState.Reconnecting
            CallApiState.DISCONNECTING -> CallState.Disconnecting
            CallApiState.DISCONNECTED -> CallState.Disconnected
            CallApiState.FAILED -> CallState.Failed("audioCallUiState error")
            null -> null
        },
    )
}

data class CatalogUiState(
    val loginState: LoginState = LoginState.LoggedOut,
    val call: Call? = null,
    val callState: CallState? = null,
)

sealed class CallState {
    data object Created : CallState()
    data object Connecting : CallState()
    data object Connected : CallState()
    data object Disconnected : CallState()
    data object Reconnecting : CallState()
    data object Disconnecting : CallState()
    data class Failed(
        val error: String,
    ) : CallState()
}
