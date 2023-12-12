package com.voximplant.sdk3demo.core.data.repository

import com.voximplant.sdk3demo.core.foundation.AuthDataSource
import com.voximplant.sdk3demo.core.foundation.model.NetworkUserData
import com.voximplant.sdk3demo.core.foundation.model.asUserData
import com.voximplant.sdk3demo.core.model.data.UserData
import javax.inject.Inject

class UserDataRepository @Inject constructor(
    private val datasource: AuthDataSource,
) {
    suspend fun logIn(username: String, password: String): Result<UserData> {
        datasource.logIn(username, password).let { result: Result<NetworkUserData> ->
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

    fun logOut() {
        datasource.disconnect()
    }
}
