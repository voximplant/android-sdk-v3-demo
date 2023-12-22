/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.data.repository

import android.util.Log
import com.voximplant.demos.sdk.core.data.model.asExternal
import com.voximplant.demos.sdk.core.data.util.PushTokenProvider
import com.voximplant.demos.sdk.core.datastore.UserPreferencesDataSource
import com.voximplant.demos.sdk.core.foundation.AuthDataSource
import com.voximplant.demos.sdk.core.foundation.model.NetworkUserData
import com.voximplant.demos.sdk.core.foundation.model.asUserData
import com.voximplant.demos.sdk.core.model.data.LoginError
import com.voximplant.demos.sdk.core.model.data.LoginState
import com.voximplant.demos.sdk.core.model.data.Node
import com.voximplant.demos.sdk.core.model.data.User
import com.voximplant.demos.sdk.core.model.data.UserCredentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthDataRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val authDataSource: AuthDataSource,
    private val pushTokenProvider: PushTokenProvider,
) {
    val user: Flow<User?> = userPreferencesDataSource.userData.map { userData -> userData?.user }

    val loginState: Flow<LoginState>
        get() = authDataSource.loginState

    suspend fun logIn(username: String, password: String, node: Node): Result<User> {
        val modifiedUsername = if (username.endsWith(domain)) username else username.plus(domain)

        authDataSource.logIn(modifiedUsername, password, node.asExternal()).let { result: Result<NetworkUserData> ->
            result.fold(
                onSuccess = { networkUser ->
                    authDataSource.registerPushToken(pushTokenProvider.getToken())
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

    suspend fun logInWithToken(): Result<User> {
        userPreferencesDataSource.userData.firstOrNull().let { userData ->
            if (userData == null) {
                return Result.failure(LoginError.InternalError)
            }
            val node = userData.node
            if (node == null) {
                Log.e("DemoV3", "AuthDataRepository::logInWithToken: node is null")
                return Result.failure(LoginError.InternalError)
            }

            authDataSource.logInWithToken(userData.user.username, userData.accessToken, node.asExternal()).let { result: Result<NetworkUserData> ->
                result.fold(
                    onSuccess = { networkUser ->
                        authDataSource.registerPushToken(pushTokenProvider.getToken())
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
    }

    suspend fun refreshToken(): Result<UserCredentials> {
        userPreferencesDataSource.userData.firstOrNull().let { userData ->
            if (userData == null) {
                return Result.failure(LoginError.NoInfo)
            }
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

    suspend fun logOut() {
        authDataSource.unregisterPush(pushTokenProvider.getToken())
        authDataSource.disconnect()
        userPreferencesDataSource.clearUser()
    }

    suspend fun handlePush(push: MutableMap<String, String>) {
        if (loginState.first() is LoginState.LoggedIn) {
            authDataSource.handlePush(push)
        } else {
            logInWithToken().fold(onSuccess = {
                handlePush(push)
            }, onFailure = { throwable ->
                Log.e("DemoV3", "AuthDataRepository::handlePush: failure: $throwable")
            })
        }
    }

    suspend fun updatePushToken(token: String) {
        if (loginState.first() is LoginState.LoggedIn) {
            authDataSource.registerPushToken(token)
        } else {
            logInWithToken().fold(onSuccess = {
                updatePushToken(token)
            }, onFailure = { throwable ->
                Log.e("DemoV3", "AuthDataRepository::updatePushToken: failure: $throwable")
            })
        }
    }

    companion object {
        const val domain = ".voximplant.com"
    }
}
