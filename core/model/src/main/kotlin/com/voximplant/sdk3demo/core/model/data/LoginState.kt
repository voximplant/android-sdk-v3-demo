package com.voximplant.sdk3demo.core.model.data

sealed interface LoginState {
    data object LoggedOut : LoginState
    data object LoggingIn : LoginState
    data object LoggedIn : LoginState
    data class Failed(val error: LoginError) : LoginState
}
