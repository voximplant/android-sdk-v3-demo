/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.foundation

import android.content.Context
import android.util.Log
import com.voximplant.android.sdk.core.AuthParams
import com.voximplant.android.sdk.core.Client
import com.voximplant.android.sdk.core.ClientSessionListener
import com.voximplant.android.sdk.core.ClientState.Connected
import com.voximplant.android.sdk.core.ClientState.Disconnected
import com.voximplant.android.sdk.core.ConnectOptions
import com.voximplant.android.sdk.core.ConnectionCallback
import com.voximplant.android.sdk.core.ConnectionError
import com.voximplant.android.sdk.core.DisconnectReason
import com.voximplant.android.sdk.core.LoginCallback
import com.voximplant.android.sdk.core.LoginError
import com.voximplant.android.sdk.core.Node
import com.voximplant.android.sdk.core.PushConfig
import com.voximplant.android.sdk.core.PushTokenError
import com.voximplant.android.sdk.core.RefreshTokenCallback
import com.voximplant.android.sdk.core.RegisterPushTokenCallback
import com.voximplant.demos.sdk.core.foundation.model.NetworkUser
import com.voximplant.demos.sdk.core.foundation.model.NetworkUserData
import com.voximplant.demos.sdk.core.model.data.LoginState
import com.voximplant.demos.sdk.core.model.data.UserCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthDataSource(
    @ApplicationContext private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val client: Client,
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

    suspend fun logIn(username: String, password: String, node: Node): Result<NetworkUserData> {
        _loginState.emit(LoginState.LoggingIn)
        when (client.clientState) {
            Disconnected -> {
                when (val connectionResult = connect(node)) {
                    is ConnectionResult.Success -> {
                        logIn(username, password, node).let { networkUserResult ->
                            return networkUserResult
                        }
                    }

                    is ConnectionResult.Failure -> {
                        val loginError = when (connectionResult.error) {
                            ConnectionError.InternalError -> com.voximplant.demos.sdk.core.model.data.LoginError.InternalError
                            ConnectionError.Interrupted -> com.voximplant.demos.sdk.core.model.data.LoginError.Interrupted
                            ConnectionError.InvalidState -> com.voximplant.demos.sdk.core.model.data.LoginError.InvalidState
                            ConnectionError.NetworkIssues -> com.voximplant.demos.sdk.core.model.data.LoginError.NetworkIssue
                            ConnectionError.Timeout -> com.voximplant.demos.sdk.core.model.data.LoginError.TimeOut
                        }
                        _loginState.emit(LoginState.Failed(loginError))
                        return Result.failure(loginError)
                    }
                }
            }

            Connected -> {
                val loginResult: LoginResult = suspendCoroutine { continuation ->
                    client.login(username, password, object : LoginCallback {
                        override fun onFailure(loginError: LoginError) {
                            Log.e("DemoV3", "AuthDataSource::logIn:onFailure: $loginError")

                            continuation.resume(LoginResult.Failure(loginError))
                        }

                        override fun onSuccess(displayName: String, authParams: AuthParams?) {
                            if (authParams == null) {
                                continuation.resume(LoginResult.Failure(LoginError.InternalError))
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
                            LoginError.InvalidPassword -> com.voximplant.demos.sdk.core.model.data.LoginError.InvalidPassword
                            LoginError.InvalidUsername -> com.voximplant.demos.sdk.core.model.data.LoginError.InvalidUsername
                            LoginError.AccountFrozen -> com.voximplant.demos.sdk.core.model.data.LoginError.AccountFrozen
                            LoginError.InternalError -> com.voximplant.demos.sdk.core.model.data.LoginError.InternalError
                            LoginError.InvalidState -> com.voximplant.demos.sdk.core.model.data.LoginError.InvalidState
                            LoginError.Interrupted -> com.voximplant.demos.sdk.core.model.data.LoginError.Interrupted
                            LoginError.MauAccessDenied -> com.voximplant.demos.sdk.core.model.data.LoginError.MauAccessDenied
                            LoginError.NetworkIssues -> com.voximplant.demos.sdk.core.model.data.LoginError.NetworkIssue
                            LoginError.TokenExpired -> com.voximplant.demos.sdk.core.model.data.LoginError.TokenExpired
                            LoginError.Timeout -> com.voximplant.demos.sdk.core.model.data.LoginError.TimeOut
                        }
                        _loginState.emit(LoginState.Failed(loginError))
                        return Result.failure(loginError)
                    }
                }
            }

            else -> {
                Log.w("DemoV3", "AuthDataSource::logIn: client is in ${client.clientState}")
                _loginState.emit(LoginState.Failed(com.voximplant.demos.sdk.core.model.data.LoginError.InternalError))
                return Result.failure(com.voximplant.demos.sdk.core.model.data.LoginError.InternalError)
            }
        }
    }

    suspend fun logInWithToken(username: String, accessToken: String, node: Node): Result<NetworkUserData> {
        _loginState.emit(LoginState.LoggingIn)
        when (client.clientState) {
            Disconnected -> {
                when (val connectionResult = connect(node)) {
                    is ConnectionResult.Success -> {
                        logInWithToken(username, accessToken, node).let { networkUserResult ->
                            return networkUserResult
                        }
                    }

                    is ConnectionResult.Failure -> {
                        Log.e("DemoV3", "AuthDataSource::logIn: failed to connect to the cloud.")
                        val loginError = when (connectionResult.error) {
                            ConnectionError.InternalError -> com.voximplant.demos.sdk.core.model.data.LoginError.InternalError
                            ConnectionError.Interrupted -> com.voximplant.demos.sdk.core.model.data.LoginError.Interrupted
                            ConnectionError.InvalidState -> com.voximplant.demos.sdk.core.model.data.LoginError.InvalidState
                            ConnectionError.NetworkIssues -> com.voximplant.demos.sdk.core.model.data.LoginError.NetworkIssue
                            ConnectionError.Timeout -> com.voximplant.demos.sdk.core.model.data.LoginError.TimeOut
                        }
                        _loginState.emit(LoginState.Failed(loginError))
                        return Result.failure(loginError)
                    }
                }
            }

            Connected -> {
                val loginResult: LoginResult = suspendCoroutine { continuation ->
                    client.loginWithAccessToken(username, accessToken, object : LoginCallback {
                        override fun onFailure(loginError: LoginError) {
                            Log.e("DemoV3", "AuthDataSource::logIn:onFailure: $loginError")
                            continuation.resume(LoginResult.Failure(loginError))
                        }

                        override fun onSuccess(displayName: String, authParams: AuthParams?) {
                            if (authParams == null) {
                                continuation.resume(LoginResult.Failure(LoginError.InternalError))
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
                            LoginError.InvalidPassword -> com.voximplant.demos.sdk.core.model.data.LoginError.InvalidPassword
                            LoginError.InvalidUsername -> com.voximplant.demos.sdk.core.model.data.LoginError.InvalidUsername
                            LoginError.AccountFrozen -> com.voximplant.demos.sdk.core.model.data.LoginError.AccountFrozen
                            LoginError.InternalError -> com.voximplant.demos.sdk.core.model.data.LoginError.InternalError
                            LoginError.InvalidState -> com.voximplant.demos.sdk.core.model.data.LoginError.InvalidState
                            LoginError.Interrupted -> com.voximplant.demos.sdk.core.model.data.LoginError.Interrupted
                            LoginError.MauAccessDenied -> com.voximplant.demos.sdk.core.model.data.LoginError.MauAccessDenied
                            LoginError.NetworkIssues -> com.voximplant.demos.sdk.core.model.data.LoginError.NetworkIssue
                            LoginError.TokenExpired -> com.voximplant.demos.sdk.core.model.data.LoginError.TokenExpired
                            LoginError.Timeout -> com.voximplant.demos.sdk.core.model.data.LoginError.TimeOut
                        }
                        _loginState.emit(LoginState.Failed(loginError))
                        return Result.failure(loginError)
                    }
                }
            }

            else -> {
                Log.w("DemoV3", "AuthDataSource::logIn: client is in ${client.clientState}")
                return Result.failure(com.voximplant.demos.sdk.core.model.data.LoginError.InternalError)
            }
        }
    }

    suspend fun refreshToken(username: String, refreshToken: String): Result<UserCredentials> {
        val refreshResult: RefreshResult = suspendCoroutine { continuation ->
            client.refreshToken(
                username,
                refreshToken,
                object : RefreshTokenCallback {
                    override fun onFailure(error: LoginError) {
                        continuation.resume(RefreshResult.Failure(error))
                    }

                    override fun onSuccess(authParams: AuthParams) {
                        continuation.resume(RefreshResult.Success(authParams))
                    }
                },
            )
        }
        return when (refreshResult) {
            is RefreshResult.Success -> {
                Result.success(UserCredentials(refreshResult.authParams.accessToken, refreshResult.authParams.refreshToken))
            }

            is RefreshResult.Failure -> {
                val loginError = when (refreshResult.error) {
                    LoginError.InvalidPassword -> com.voximplant.demos.sdk.core.model.data.LoginError.InvalidPassword
                    LoginError.InvalidUsername -> com.voximplant.demos.sdk.core.model.data.LoginError.InvalidUsername
                    LoginError.AccountFrozen -> com.voximplant.demos.sdk.core.model.data.LoginError.AccountFrozen
                    LoginError.InternalError -> com.voximplant.demos.sdk.core.model.data.LoginError.InternalError
                    LoginError.InvalidState -> com.voximplant.demos.sdk.core.model.data.LoginError.InvalidState
                    LoginError.Interrupted -> com.voximplant.demos.sdk.core.model.data.LoginError.Interrupted
                    LoginError.MauAccessDenied -> com.voximplant.demos.sdk.core.model.data.LoginError.MauAccessDenied
                    LoginError.NetworkIssues -> com.voximplant.demos.sdk.core.model.data.LoginError.NetworkIssue
                    LoginError.TokenExpired -> com.voximplant.demos.sdk.core.model.data.LoginError.TokenExpired
                    LoginError.Timeout -> com.voximplant.demos.sdk.core.model.data.LoginError.TimeOut
                }
                return Result.failure(loginError)
            }
        }
    }

    suspend fun registerPushToken(token: String) = suspendCoroutine {
        client.registerForPushNotifications(
            pushConfig = PushConfig(token, context.packageName),
            callback = object : RegisterPushTokenCallback {
                override fun onFailure(error: PushTokenError) {
                    Log.e("DemoV3", "AuthDataSource::registerPushToken failure: $error")
                    it.resumeWithException(Exception(error.toString()))
                }

                override fun onSuccess() {
                    it.resume(Unit)
                }

            },
        )
    }

    suspend fun unregisterPush(token: String) = suspendCoroutine {
        client.unregisterFromPushNotifications(
            pushConfig = PushConfig(token, context.packageName),
            callback = object : RegisterPushTokenCallback {
                override fun onFailure(error: PushTokenError) {
                    Log.e("DemoV3", "AuthDataSource::unregisterPush failure: $error")
                    it.resumeWithException(Exception(error.toString()))
                }

                override fun onSuccess() {
                    it.resume(Unit)
                }

            },
        )
    }

    fun handlePush(push: MutableMap<String, String>) {
        client.handlePushNotification(push)
    }

    private suspend fun connect(node: Node) = suspendCoroutine { continuation ->
        client.connect(
            options = ConnectOptions(node),
            callback = object : ConnectionCallback {
                override fun onFailure(error: ConnectionError) {
                    continuation.resume(ConnectionResult.Failure(error))
                }

                override fun onSuccess() {
                    continuation.resume(ConnectionResult.Success)
                }
            },
        )
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
    data class Failure(val error: LoginError) : LoginResult
}

private sealed interface RefreshResult {
    data class Success(val authParams: AuthParams) : RefreshResult
    data class Failure(val error: LoginError) : RefreshResult
}

