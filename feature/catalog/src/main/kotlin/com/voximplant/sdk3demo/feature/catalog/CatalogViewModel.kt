package com.voximplant.sdk3demo.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.sdk3demo.core.domain.GetUserUseCase
import com.voximplant.sdk3demo.core.domain.LogOutUseCase
import com.voximplant.sdk3demo.core.domain.SilentLogInUseCase
import com.voximplant.sdk3demo.core.model.data.LoginError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    getUserUseCase: GetUserUseCase,
    private val silentLogInUseCase: SilentLogInUseCase,
    private val logOutUseCase: LogOutUseCase,
) : ViewModel() {
    val user = getUserUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    private val _loginUiState: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState.Init)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    init {
        viewModelScope.launch {
            silentLogInUseCase()
                .onSuccess {
                    _loginUiState.value = LoginUiState.Success
                }
                .onFailure { throwable ->
                    _loginUiState.value = LoginUiState.Failure(throwable as LoginError)
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logOutUseCase()
        }
    }
}

sealed interface LoginUiState {
    data object Init : LoginUiState
    data object Success : LoginUiState
    data class Failure(val error: LoginError) : LoginUiState
}
