package com.voximplant.sdk3demo.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.sdk3demo.core.domain.GetNodeUseCase
import com.voximplant.sdk3demo.core.domain.LogInUseCase
import com.voximplant.sdk3demo.core.domain.SelectNodeUseCase
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
    getNode: GetNodeUseCase,
    private val selectNodeUseCase: SelectNodeUseCase,
) : ViewModel() {

    private val loginState: MutableStateFlow<LoginState> = MutableStateFlow(LoginState.LoggedOut)

    val loginUiState: StateFlow<LoginUiState> = catalogUiState(
        loginStateFlow = loginState,
        nodeFlow = getNode(),
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LoginUiState(),
    )

    fun logIn(username: String, password: String) {
        loginState.value = LoginState.Loading

        viewModelScope.launch {
            loginUseCase(username, password)
                .onSuccess { user ->
                    loginState.value = LoginState.Success(user)
                }
                .onFailure { throwable ->
                    loginState.value = LoginState.Failure(throwable as LoginError)
                }
        }
    }

    fun selectNode(node: Node) {
        viewModelScope.launch {
            selectNodeUseCase(node)
        }
    }
}

private fun catalogUiState(
    loginStateFlow: Flow<LoginState>,
    nodeFlow: Flow<Node?>,
): Flow<LoginUiState> = combine(loginStateFlow, nodeFlow) { loginState, node ->
    LoginUiState(
        loginState = loginState,
        node = node,
    )
}

data class LoginUiState(
    val loginState: LoginState = LoginState.LoggedOut,
    val node: Node? = null,
)

sealed interface LoginState {
    data object LoggedOut : LoginState
    data object Loading : LoginState
    data class Success(val user: User) : LoginState
    data class Failure(val error: LoginError) : LoginState
}
