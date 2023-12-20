package com.voximplant.sdk3demo.core.data.repository

import android.util.Log
import com.voximplant.sdk3demo.core.data.util.PushTokenProvider
import com.voximplant.sdk3demo.core.datastore.UserPreferencesDataSource
import com.voximplant.sdk3demo.core.foundation.AuthDataSource
import com.voximplant.sdk3demo.core.foundation.model.NetworkUserData
import com.voximplant.sdk3demo.core.foundation.model.asUserData
import com.voximplant.sdk3demo.core.model.data.LoginError
import com.voximplant.sdk3demo.core.model.data.LoginState
import com.voximplant.sdk3demo.core.model.data.User
import com.voximplant.sdk3demo.core.model.data.UserData
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

    suspend fun logIn(username: String, password: String): Result<UserData> {
        val modifiedUsername = if (username.endsWith(domain)) username else username.plus(domain)

        authDataSource.logIn(modifiedUsername, password).let { result: Result<NetworkUserData> ->
            result.fold(
                onSuccess = { networkUser ->
                    authDataSource.registerPushToken(pushTokenProvider.getToken())
                    userPreferencesDataSource.updateUser(networkUser.asUserData())
                    return Result.success(networkUser.asUserData())
                },
                onFailure = { throwable ->
                    return Result.failure(throwable)
                },
            )
        }
    }

    suspend fun logInWithToken(): Result<UserData> {
        userPreferencesDataSource.userData.firstOrNull().let { userData ->
            if (userData == null) {
                return Result.failure(LoginError.InternalError)
            }
            authDataSource.logInWithToken(userData.user.username, userData.accessToken).let { result: Result<NetworkUserData> ->
                result.fold(
                    onSuccess = { networkUser ->
                        authDataSource.registerPushToken(pushTokenProvider.getToken())
                        userPreferencesDataSource.updateUser(networkUser.asUserData())
                        return Result.success(networkUser.asUserData())
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
            logInWithToken().fold(
                onSuccess = {
                    handlePush(push)
                },
                onFailure = { throwable ->
                    Log.e("DemoV3", "AuthDataRepository::handlePush: failure: $throwable")
                }
            )
        }
    }

    suspend fun updatePushToken(token: String) {
        if (loginState.first() is LoginState.LoggedIn) {
            authDataSource.registerPushToken(token)
        } else {
            logInWithToken().fold(
                onSuccess = {
                    updatePushToken(token)
                },
                onFailure = { throwable ->
                    Log.e("DemoV3", "AuthDataRepository::updatePushToken: failure: $throwable")
                }
            )
        }
    }

    companion object {
        const val domain = ".voximplant.com"
    }
}
