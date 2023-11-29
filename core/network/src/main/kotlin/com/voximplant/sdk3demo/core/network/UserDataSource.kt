package com.voximplant.sdk3demo.core.network

import android.content.Context
import android.util.Log
import com.voximplant.core.AuthParams
import com.voximplant.core.Client
import com.voximplant.core.ClientState.CONNECTED
import com.voximplant.core.ClientState.DISCONNECTED
import com.voximplant.core.ConnectionCallback
import com.voximplant.core.ConnectionError
import com.voximplant.core.LoginCallback
import com.voximplant.core.LoginError
import com.voximplant.sdk3demo.core.model.data.AuthError
import com.voximplant.sdk3demo.core.network.model.NetworkUser
import com.voximplant.sdk3demo.core.network.model.NetworkUserData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserDataSource(@ApplicationContext context: Context) {

    private val client: Client = Client.getInstance(context)

    suspend fun logIn(username: String, password: String): Result<NetworkUserData> {
        when (client.clientState) {
            DISCONNECTED -> {
                when (val connectionResult = connect()) {
                    is ConnectionResult.Success -> {
                        logIn(username, password).let { networkUserResult ->
                            return networkUserResult
                        }
                    }

                    is ConnectionResult.Failure -> {
                        Log.e("DemoV3", "UserDataSource::logIn: failed to connect to the cloud.")
                        val authError = when (connectionResult.error) {
                            ConnectionError.INTERNAL_ERROR -> AuthError.InternalError
                            ConnectionError.INTERRUPTED -> AuthError.Interrupted
                            ConnectionError.INVALID_STATE -> AuthError.InvalidState
                            ConnectionError.NETWORK_ISSUE -> AuthError.NetworkIssue
                            ConnectionError.TIMEOUT -> AuthError.TimeOut
                        }
                        return Result.failure(authError)
                    }
                }
            }

            CONNECTED -> {
                val loginResult: LoginResult = suspendCoroutine { continuation ->
                    client.login(username, password, object : LoginCallback {
                        override fun onFailure(loginError: LoginError) {
                            Log.e("DemoV3", "UserDataSource::logIn:onFailure: $loginError")
                            continuation.resume(LoginResult.Failure(loginError))
                        }

                        override fun onSuccess(displayName: String, authParams: AuthParams?) {
                            if (authParams == null) {
                                continuation.resume(LoginResult.Failure(LoginError.INTERNAL_ERROR))
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
                        Result.success(loginResult.networkUserData)
                    }

                    is LoginResult.Failure -> {
                        val authError = when (loginResult.error) {
                            LoginError.INVALID_PASSWORD -> AuthError.InvalidPassword
                            LoginError.INVALID_USERNAME -> AuthError.InvalidUsername
                            LoginError.ACCOUNT_FROZEN -> AuthError.AccountFrozen
                            LoginError.INTERNAL_ERROR -> AuthError.InternalError
                            LoginError.INVALID_STATE -> AuthError.InvalidState
                            LoginError.INTERRUPTED -> AuthError.Interrupted
                            LoginError.MAU_ACCESS_DENIED -> AuthError.MauAccessDenied
                            LoginError.NETWORK_ISSUES -> AuthError.NetworkIssue
                            LoginError.TOKEN_EXPIRED -> AuthError.TokenExpired
                            LoginError.TIMEOUT -> AuthError.TimeOut
                        }
                        return Result.failure(authError)
                    }
                }
            }

            else -> {
                Log.w("DemoV3", "UserDataSource::logIn: client is in ${client.clientState}")
                return Result.failure(AuthError.InternalError)
            }
        }
    }

    suspend fun logInWithToken(username: String, accessToken: String): Result<NetworkUserData> {
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
                        val authError = when (connectionResult.error) {
                            ConnectionError.INTERNAL_ERROR -> AuthError.InternalError
                            ConnectionError.INTERRUPTED -> AuthError.Interrupted
                            ConnectionError.INVALID_STATE -> AuthError.InvalidState
                            ConnectionError.NETWORK_ISSUE -> AuthError.NetworkIssue
                            ConnectionError.TIMEOUT -> AuthError.TimeOut
                        }
                        return Result.failure(authError)
                    }
                }
            }

            CONNECTED -> {
                val loginResult: LoginResult = suspendCoroutine { continuation ->
                    client.loginWithAccessToken(username, accessToken, object : LoginCallback {
                        override fun onFailure(loginError: LoginError) {
                            Log.e("DemoV3", "UserDataSource::logIn:onFailure: $loginError")
                            continuation.resume(LoginResult.Failure(loginError))
                        }

                        override fun onSuccess(displayName: String, authParams: AuthParams?) {
                            if (authParams == null) {
                                continuation.resume(LoginResult.Failure(LoginError.INTERNAL_ERROR))
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
                        Result.success(loginResult.networkUserData)
                    }

                    is LoginResult.Failure -> {
                        val authError = when (loginResult.error) {
                            LoginError.INVALID_PASSWORD -> AuthError.InvalidPassword
                            LoginError.INVALID_USERNAME -> AuthError.InvalidUsername
                            LoginError.ACCOUNT_FROZEN -> AuthError.AccountFrozen
                            LoginError.INTERNAL_ERROR -> AuthError.InternalError
                            LoginError.INVALID_STATE -> AuthError.InvalidState
                            LoginError.INTERRUPTED -> AuthError.Interrupted
                            LoginError.MAU_ACCESS_DENIED -> AuthError.MauAccessDenied
                            LoginError.NETWORK_ISSUES -> AuthError.NetworkIssue
                            LoginError.TOKEN_EXPIRED -> AuthError.TokenExpired
                            LoginError.TIMEOUT -> AuthError.TimeOut
                        }
                        return Result.failure(authError)
                    }
                }
            }

            else -> {
                Log.w("DemoV3", "UserDataSource::logIn: client is in ${client.clientState}")
                return Result.failure(AuthError.InternalError)
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

    fun disconnect() = client.disconnect()
}

private sealed interface ConnectionResult {
    data object Success : ConnectionResult
    data class Failure(val error: ConnectionError) : ConnectionResult
}

private sealed interface LoginResult {
    data class Success(val networkUserData: NetworkUserData) : LoginResult
    data class Failure(val error: LoginError) : LoginResult
}
