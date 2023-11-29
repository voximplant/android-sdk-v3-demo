package com.voximplant.sdk3demo.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.sdk3demo.core.domain.LogInUseCase
import com.voximplant.sdk3demo.core.model.data.AuthError
import com.voximplant.sdk3demo.core.model.data.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LogInUseCase,
) : ViewModel() {

    private val _loginUiState: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState.Init)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    fun logIn(username: String, password: String) {
        _loginUiState.value = LoginUiState.Loading

        viewModelScope.launch {
            loginUseCase(username, password)
                .onSuccess { user ->
                    _loginUiState.value = LoginUiState.Success(user)
                }
                .onFailure { throwable ->
                    _loginUiState.value = LoginUiState.Failure(throwable as AuthError)
                }
        }
    }
}

sealed interface LoginUiState {
    data object Init : LoginUiState
    data object Loading : LoginUiState
    data class Success(val user: User) : LoginUiState
    data class Failure(val error: AuthError) : LoginUiState
}
