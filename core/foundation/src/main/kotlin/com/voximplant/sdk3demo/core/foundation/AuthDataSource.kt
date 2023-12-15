package com.voximplant.sdk3demo.core.foundation

import android.util.Log
import com.voximplant.core.AuthParams
import com.voximplant.core.Client
import com.voximplant.core.ClientSessionListener
import com.voximplant.core.ClientState.CONNECTED
import com.voximplant.core.ClientState.DISCONNECTED
import com.voximplant.core.ConnectionCallback
import com.voximplant.core.ConnectionError
import com.voximplant.core.DisconnectReason
import com.voximplant.core.LoginCallback
import com.voximplant.sdk3demo.core.foundation.model.NetworkUser
import com.voximplant.sdk3demo.core.foundation.model.NetworkUserData
import com.voximplant.sdk3demo.core.model.data.LoginState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthDataSource(
    private val client: Client,
    private val coroutineScope: CoroutineScope,
) {

    private val _loginState: MutableStateFlow<LoginState> = MutableStateFlow(LoginState.LoggedOut)
    val loginState: Flow<LoginState> = _loginState.asStateFlow()

    private val clientSessionListener = object : ClientSessionListener {
        override fun onConnectionClosed(reason: DisconnectReason) {
            coroutineScope.launch {
                _loginState.emit(LoginState.LoggedOut)
            }
        }
    }

    init {
        client.setClientSessionListener(clientSessionListener)
    }

    suspend fun logIn(username: String, password: String): Result<NetworkUserData> {
        _loginState.emit(LoginState.LoggingIn)
        when (client.clientState) {
            DISCONNECTED -> {
                when (val connectionResult = connect()) {
                    is ConnectionResult.Success -> {
                        logIn(username, password).let { networkUserResult ->
                            return networkUserResult
                        }
                    }

                    is ConnectionResult.Failure -> {
                        val loginError = when (connectionResult.error) {
                            ConnectionError.INTERNAL_ERROR -> com.voximplant.sdk3demo.core.model.data.LoginError.InternalError
                            ConnectionError.INTERRUPTED -> com.voximplant.sdk3demo.core.model.data.LoginError.Interrupted
                            ConnectionError.INVALID_STATE -> com.voximplant.sdk3demo.core.model.data.LoginError.InvalidState
                            ConnectionError.NETWORK_ISSUE -> com.voximplant.sdk3demo.core.model.data.LoginError.NetworkIssue
                            ConnectionError.TIMEOUT -> com.voximplant.sdk3demo.core.model.data.LoginError.TimeOut
                        }
                        _loginState.emit(LoginState.Failed(loginError))
                        return Result.failure(loginError)
                    }
                }
            }

            CONNECTED -> {
                val loginResult: LoginResult = suspendCoroutine { continuation ->
                    client.login(username, password, object : LoginCallback {
                        override fun onFailure(loginError: com.voximplant.core.LoginError) {
                            Log.e("DemoV3", "UserDataSource::logIn:onFailure: $loginError")

                            continuation.resume(LoginResult.Failure(loginError))
                        }

                        override fun onSuccess(displayName: String, authParams: AuthParams?) {
                            if (authParams == null) {
                                continuation.resume(LoginResult.Failure(com.voximplant.core.LoginError.INTERNAL_ERROR))
                                return
                            }
                            continuation.resume(
                                LoginResult.Success(
                                    NetworkUserData(
                                        user = NetworkUser(username, displayName),
                                        accessToken = authParams.accessToken,
                                        refreshToken = authParams.refreshToken,
                                    )
                                )
                            )
                        }
                    })
                }
                return when (loginResult) {
                    is LoginResult.Success -> {
                        _loginState.emit(LoginState.LoggedIn)
                        Result.success(loginResult.networkUserData)
                    }

                    is LoginResult.Failure -> {
                        val loginError = when (loginResult.error) {
                            com.voximplant.core.LoginError.INVALID_PASSWORD -> com.voximplant.sdk3demo.core.model.data.LoginError.InvalidPassword
                            com.voximplant.core.LoginError.INVALID_USERNAME -> com.voximplant.sdk3demo.core.model.data.LoginError.InvalidUsername
                            com.voximplant.core.LoginError.ACCOUNT_FROZEN -> com.voximplant.sdk3demo.core.model.data.LoginError.AccountFrozen
                            com.voximplant.core.LoginError.INTERNAL_ERROR -> com.voximplant.sdk3demo.core.model.data.LoginError.InternalError
                            com.voximplant.core.LoginError.INVALID_STATE -> com.voximplant.sdk3demo.core.model.data.LoginError.InvalidState
                            com.voximplant.core.LoginError.INTERRUPTED -> com.voximplant.sdk3demo.core.model.data.LoginError.Interrupted
                            com.voximplant.core.LoginError.MAU_ACCESS_DENIED -> com.voximplant.sdk3demo.core.model.data.LoginError.MauAccessDenied
                            com.voximplant.core.LoginError.NETWORK_ISSUES -> com.voximplant.sdk3demo.core.model.data.LoginError.NetworkIssue
                            com.voximplant.core.LoginError.TOKEN_EXPIRED -> com.voximplant.sdk3demo.core.model.data.LoginError.TokenExpired
                            com.voximplant.core.LoginError.TIMEOUT -> com.voximplant.sdk3demo.core.model.data.LoginError.TimeOut
                        }
                        _loginState.emit(LoginState.Failed(loginError))
                        return Result.failure(loginError)
                    }
                }
            }

            else -> {
                Log.w("DemoV3", "UserDataSource::logIn: client is in ${client.clientState}")
                _loginState.emit(LoginState.Failed(com.voximplant.sdk3demo.core.model.data.LoginError.InternalError))
                return Result.failure(com.voximplant.sdk3demo.core.model.data.LoginError.InternalError)
            }
        }
    }

    suspend fun logInWithToken(username: String, accessToken: String): Result<NetworkUserData> {
        _loginState.emit(LoginState.LoggingIn)
        when (client.clientState) {
            DISCONNECTED -> {
                when (val connectionResult = connect()) {
                    is ConnectionResult.Success -> {
                        logInWithToken(username, accessToken).let { networkUserResult ->
                            return networkUserResult
                        }
                    }

                    is ConnectionResult.Failure -> {
                        Log.e("DemoV3", "UserDataSource::logIn: failed to connect to the cloud.")
                        val loginError = when (connectionResult.error) {
                            ConnectionError.INTERNAL_ERROR -> com.voximplant.sdk3demo.core.model.data.LoginError.InternalError
                            ConnectionError.INTERRUPTED -> com.voximplant.sdk3demo.core.model.data.LoginError.Interrupted
                            ConnectionError.INVALID_STATE -> com.voximplant.sdk3demo.core.model.data.LoginError.InvalidState
                            ConnectionError.NETWORK_ISSUE -> com.voximplant.sdk3demo.core.model.data.LoginError.NetworkIssue
                            ConnectionError.TIMEOUT -> com.voximplant.sdk3demo.core.model.data.LoginError.TimeOut
                        }
                        _loginState.emit(LoginState.Failed(loginError))
                        return Result.failure(loginError)
                    }
                }
            }

            CONNECTED -> {
                val loginResult: LoginResult = suspendCoroutine { continuation ->
                    client.loginWithAccessToken(username, accessToken, object : LoginCallback {
                        override fun onFailure(loginError: com.voximplant.core.LoginError) {
                            Log.e("DemoV3", "UserDataSource::logIn:onFailure: $loginError")
                            continuation.resume(LoginResult.Failure(loginError))
                        }

                        override fun onSuccess(displayName: String, authParams: AuthParams?) {
                            if (authParams == null) {
                                continuation.resume(LoginResult.Failure(com.voximplant.core.LoginError.INTERNAL_ERROR))
                                return
                            }
                            continuation.resume(
                                LoginResult.Success(
                                    NetworkUserData(
                                        user = NetworkUser(username, displayName),
                                        accessToken = authParams.accessToken,
                                        refreshToken = authParams.refreshToken,
                                    )
                                )
                            )
                        }
                    })
                }
                return when (loginResult) {
                    is LoginResult.Success -> {
                        _loginState.emit(LoginState.LoggedIn)
                        Result.success(loginResult.networkUserData)
                    }

                    is LoginResult.Failure -> {
                        val loginError = when (loginResult.error) {
                            com.voximplant.core.LoginError.INVALID_PASSWORD -> com.voximplant.sdk3demo.core.model.data.LoginError.InvalidPassword
                            com.voximplant.core.LoginError.INVALID_USERNAME -> com.voximplant.sdk3demo.core.model.data.LoginError.InvalidUsername
                            com.voximplant.core.LoginError.ACCOUNT_FROZEN -> com.voximplant.sdk3demo.core.model.data.LoginError.AccountFrozen
                            com.voximplant.core.LoginError.INTERNAL_ERROR -> com.voximplant.sdk3demo.core.model.data.LoginError.InternalError
                            com.voximplant.core.LoginError.INVALID_STATE -> com.voximplant.sdk3demo.core.model.data.LoginError.InvalidState
                            com.voximplant.core.LoginError.INTERRUPTED -> com.voximplant.sdk3demo.core.model.data.LoginError.Interrupted
                            com.voximplant.core.LoginError.MAU_ACCESS_DENIED -> com.voximplant.sdk3demo.core.model.data.LoginError.MauAccessDenied
                            com.voximplant.core.LoginError.NETWORK_ISSUES -> com.voximplant.sdk3demo.core.model.data.LoginError.NetworkIssue
                            com.voximplant.core.LoginError.TOKEN_EXPIRED -> com.voximplant.sdk3demo.core.model.data.LoginError.TokenExpired
                            com.voximplant.core.LoginError.TIMEOUT -> com.voximplant.sdk3demo.core.model.data.LoginError.TimeOut
                        }
                        _loginState.emit(LoginState.Failed(loginError))
                        return Result.failure(loginError)
                    }
                }
            }

            else -> {
                Log.w("DemoV3", "UserDataSource::logIn: client is in ${client.clientState}")
                return Result.failure(com.voximplant.sdk3demo.core.model.data.LoginError.InternalError)
            }
        }
    }

    private suspend fun connect() = suspendCoroutine { continuation ->
        client.connect(object : ConnectionCallback {
            override fun onFailure(error: ConnectionError) {
                continuation.resume(ConnectionResult.Failure(error))
            }

            override fun onSuccess() {
                continuation.resume(ConnectionResult.Success)
            }
        })
    }

    suspend fun disconnect() {
        _loginState.emit(LoginState.LoggedOut)
        client.disconnect()
    }
}

private sealed interface ConnectionResult {
    data object Success : ConnectionResult
    data class Failure(val error: ConnectionError) : ConnectionResult
}

private sealed interface LoginResult {
    data class Success(val networkUserData: NetworkUserData) : LoginResult
    data class Failure(val error: com.voximplant.core.LoginError) : LoginResult
}