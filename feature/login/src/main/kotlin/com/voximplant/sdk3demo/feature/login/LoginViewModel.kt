package com.voximplant.sdk3demo.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.sdk3demo.core.domain.LogInUseCase
import com.voximplant.sdk3demo.core.model.data.LoginError
import com.voximplant.sdk3demo.core.model.data.Node
import com.voximplant.sdk3demo.core.model.data.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LogInUseCase,
) : ViewModel() {

    private val loginState: MutableStateFlow<LoginState> = MutableStateFlow(LoginState.LoggedOut)

    val loginUiState: StateFlow<LoginUiState> = catalogUiState(
        loginStateFlow = loginState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LoginUiState(),
    )

    fun logIn(username: String, password: String, node: Node) {
        loginState.value = LoginState.Loading

        viewModelScope.launch {
            loginUseCase(username, password, node)
                .onSuccess { user ->
                    loginState.value = LoginState.Success(user)
                }
                .onFailure { throwable ->
                    loginState.value = LoginState.Failure(throwable as LoginError)
                }
        }
    }

}

private fun catalogUiState(
    loginStateFlow: Flow<LoginState>,
): Flow<LoginUiState> = combine(loginStateFlow) {
    LoginUiState(
        loginState = it.first(),
    )
}

data class LoginUiState(
    val loginState: LoginState = LoginState.LoggedOut,
)

sealed interface LoginState {
    data object LoggedOut : LoginState
    data object Loading : LoginState
    data class Success(val user: User) : LoginState
    data class Failure(val error: LoginError) : LoginState
}
