package com.voximplant.sdk3demo.core.foundation

import android.content.Context
import android.util.Log
import com.voximplant.core.AuthParams
import com.voximplant.core.Client
import com.voximplant.core.ClientSessionListener
import com.voximplant.core.ClientState.CONNECTED
import com.voximplant.core.ClientState.DISCONNECTED
import com.voximplant.core.ConnectOptions
import com.voximplant.core.ConnectionCallback
import com.voximplant.core.ConnectionError
import com.voximplant.core.DisconnectReason
import com.voximplant.core.LoginCallback
import com.voximplant.core.Node
import com.voximplant.core.PushConfig
import com.voximplant.core.PushTokenError
import com.voximplant.core.RegisterPushTokenCallback
import com.voximplant.sdk3demo.core.foundation.model.NetworkUser
import com.voximplant.sdk3demo.core.foundation.model.NetworkUserData
import com.voximplant.sdk3demo.core.model.data.LoginState
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
            DISCONNECTED -> {
                when (val connectionResult = connect(node)) {
                    is ConnectionResult.Success -> {
                        logIn(username, password, node).let { networkUserResult ->
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
                            Log.e("DemoV3", "AuthDataSource::logIn:onFailure: $loginError")

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
                Log.w("DemoV3", "AuthDataSource::logIn: client is in ${client.clientState}")
                _loginState.emit(LoginState.Failed(com.voximplant.sdk3demo.core.model.data.LoginError.InternalError))
                return Result.failure(com.voximplant.sdk3demo.core.model.data.LoginError.InternalError)
            }
        }
    }

    suspend fun logInWithToken(username: String, accessToken: String, node: Node): Result<NetworkUserData> {
        _loginState.emit(LoginState.LoggingIn)
        when (client.clientState) {
            DISCONNECTED -> {
                when (val connectionResult = connect(node)) {
                    is ConnectionResult.Success -> {
                        logInWithToken(username, accessToken, node).let { networkUserResult ->
                            return networkUserResult
                        }
                    }

                    is ConnectionResult.Failure -> {
                        Log.e("DemoV3", "AuthDataSource::logIn: failed to connect to the cloud.")
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
                            Log.e("DemoV3", "AuthDataSource::logIn:onFailure: $loginError")
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
                Log.w("DemoV3", "AuthDataSource::logIn: client is in ${client.clientState}")
                return Result.failure(com.voximplant.sdk3demo.core.model.data.LoginError.InternalError)
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
    data class Failure(val error: com.voximplant.core.LoginError) : LoginResult
}
