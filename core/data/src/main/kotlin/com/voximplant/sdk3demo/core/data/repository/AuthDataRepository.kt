package com.voximplant.sdk3demo.core.data.repository

import com.voximplant.sdk3demo.core.foundation.AuthDataSource
import com.voximplant.sdk3demo.core.foundation.model.NetworkUserData
import com.voximplant.sdk3demo.core.foundation.model.asUserData
import com.voximplant.sdk3demo.core.model.data.UserData
import javax.inject.Inject

class AuthDataRepository @Inject constructor(
    private val datasource: AuthDataSource,
) {
    val loginState
        get() = datasource.loginState

    suspend fun logIn(username: String, password: String): Result<UserData> {
        val modifiedUsername = if (username.endsWith(domain)) username else username.plus(domain)

        datasource.logIn(modifiedUsername, password).let { result: Result<NetworkUserData> ->
            result.fold(
                onSuccess = { networkUser ->
                    return Result.success(networkUser.asUserData())
                },
                onFailure = { throwable ->
                    return Result.failure(throwable)
                },
            )
        }
    }

    suspend fun logInWithToken(username: String, accessToken: String): Result<UserData> {
        datasource.logInWithToken(username, accessToken).let { result: Result<NetworkUserData> ->
            result.fold(
                onSuccess = { networkUser ->
                    return Result.success(networkUser.asUserData())
                },
                onFailure = { throwable ->
                    return Result.failure(throwable)
                },
            )
        }
    }

    suspend fun logOut() {
        datasource.disconnect()
    }

    companion object {
        const val domain = ".voximplant.com"
    }
}
