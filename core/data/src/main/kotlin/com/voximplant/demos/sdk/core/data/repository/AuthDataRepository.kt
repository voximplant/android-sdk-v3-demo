/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.data.repository

import com.voximplant.demos.sdk.core.common.di.ApplicationScope
import com.voximplant.demos.sdk.core.data.model.asExternal
import com.voximplant.demos.sdk.core.data.util.PushTokenProvider
import com.voximplant.demos.sdk.core.datastore.UserPreferencesDataSource
import com.voximplant.demos.sdk.core.foundation.AuthDataSource
import com.voximplant.demos.sdk.core.foundation.model.NetworkUserData
import com.voximplant.demos.sdk.core.foundation.model.asUserData
import com.voximplant.demos.sdk.core.logger.Logger
import com.voximplant.demos.sdk.core.model.data.LoginError
import com.voximplant.demos.sdk.core.model.data.LoginState
import com.voximplant.demos.sdk.core.model.data.Node
import com.voximplant.demos.sdk.core.model.data.User
import com.voximplant.demos.sdk.core.model.data.UserCredentials
import com.voximplant.demos.sdk.core.model.data.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

class AuthDataRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val authDataSource: AuthDataSource,
    private val pushTokenProvider: PushTokenProvider,
    @ApplicationScope private val scope: CoroutineScope,
) {
    val loginState: Flow<LoginState>
        get() = authDataSource.loginState

    suspend fun logIn(username: String, password: String, node: Node): Result<User> {
        val modifiedUsername = if (username.endsWith(domain)) username else username.plus(domain)

        authDataSource.logIn(modifiedUsername, password, node.asExternal()).let { result: Result<NetworkUserData> ->
            result.fold(
                onSuccess = { networkUser ->
                    try {
                        authDataSource.registerPushToken(pushTokenProvider.getToken())
                    } catch (exception: IOException) {
                        Logger.error("AuthDataRepository::logIn: failed to get pushToken", exception)
                    }
                    userPreferencesDataSource.updateUser(networkUser.asUserData())
                    userPreferencesDataSource.updateNode(node)
                    return Result.success(networkUser.asUserData().user)
                },
                onFailure = { throwable ->
                    return Result.failure(throwable)
                },
            )
        }
    }

    suspend fun silentLogIn(): Result<User> {
        val node = getNode().getOrNull() ?: return Result.failure(LoginError.InternalError)
        return logInWithToken(node).await().fold(
            onSuccess = { userData ->
                try {
                    authDataSource.registerPushToken(pushTokenProvider.getToken())
                } catch (exception: IOException) {
                    Logger.error("AuthDataRepository::silentLogIn: failed to get pushToken", exception)
                }
                userPreferencesDataSource.updateUser(userData)
                userPreferencesDataSource.updateNode(node)
                return@fold Result.success(userData.user)
            },
            onFailure = { throwable ->
                return@fold Result.failure(throwable)
            },
        )
    }

    private suspend fun logInWithToken(node: Node): Deferred<Result<UserData>> = coroutineScope {
        userPreferencesDataSource.userData.first().let { userData ->
            return@let scope.async {
                authDataSource.logInWithToken(userData.user.username, userData.accessToken, node.asExternal()).fold(
                    onSuccess = { networkUserData ->
                        return@fold Result.success(networkUserData.asUserData())
                    },
                    onFailure = { throwable ->
                        return@fold Result.failure(throwable)
                    },
                )
            }
        }
    }

    private suspend fun getNode(): Result<Node> {
        userPreferencesDataSource.userData.first().let { userData ->
            val node = userData.node
            if (node == null) {
                Logger.error("AuthDataRepository::getNode: node is null")
                return Result.failure(LoginError.InternalError)
            } else {
                return Result.success(node)
            }
        }
    }

    suspend fun refreshToken(): Result<UserCredentials> {
        userPreferencesDataSource.userData.first().let { userData ->
            authDataSource.refreshToken(userData.user.username, userData.refreshToken).let { authParamsResult ->
                authParamsResult.fold(
                    onSuccess = { authParams ->
                        userPreferencesDataSource.updateTokens(authParams.accessToken, authParams.refreshToken)
                        return Result.success(authParams)
                    },
                    onFailure = { throwable ->
                        return Result.failure(throwable)
                    },
                )
            }
        }
    }

    suspend fun logOut() = coroutineScope {
        if (loginState.first() is LoginState.LoggedIn) {
            userPreferencesDataSource.clearUserData()

            try {
                authDataSource.unregisterPush(pushTokenProvider.getToken())
            } catch (exception: IOException) {
                Logger.error("AuthDataRepository::logOut: failed to get pushToken", exception)
            }

            authDataSource.disconnect()
        } else {
            val node = getNode().getOrNull() ?: return@coroutineScope
            val loginJob = logInWithToken(node)
            userPreferencesDataSource.clearUserData()
            loginJob.await().let { loginResult ->
                if (loginResult.isSuccess) {
                    try {
                        authDataSource.unregisterPush(pushTokenProvider.getToken())
                    } catch (exception: IOException) {
                        Logger.error("AuthDataRepository::logOut: failed to get pushToken", exception)
                    }
                }
                authDataSource.disconnect()
            }
        }
    }

    suspend fun handlePush(push: MutableMap<String, String>) {
        if (loginState.first() is LoginState.LoggedIn) {
            authDataSource.handlePush(push)
        } else {
            silentLogIn().fold(
                onSuccess = {
                    handlePush(push)
                },
                onFailure = { throwable ->
                    Logger.error("AuthDataRepository::handlePush: failure", throwable)
                },
            )
        }
    }

    suspend fun updatePushToken(token: String) {
        if (loginState.first() is LoginState.LoggedIn) {
            authDataSource.registerPushToken(token)
        } else {
            silentLogIn().fold(
                onSuccess = {
                    updatePushToken(token)
                },
                onFailure = { throwable ->
                    Logger.error("AuthDataRepository::updatePushToken: failure", throwable)
                },
            )
        }
    }

    companion object {
        const val domain = ".voximplant.com"
    }
}
