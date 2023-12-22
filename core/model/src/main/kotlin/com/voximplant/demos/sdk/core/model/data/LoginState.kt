/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.model.data

sealed interface LoginState {
    data object LoggedOut : LoginState
    data object LoggingIn : LoginState
    data object LoggedIn : LoginState
    data class Failed(val error: LoginError) : LoginState
}
