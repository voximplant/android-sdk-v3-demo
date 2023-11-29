package com.voximplant.sdk3demo.core.data

import com.voximplant.sdk3demo.core.model.data.UserData
import com.voximplant.sdk3demo.core.network.UserDataSource
import com.voximplant.sdk3demo.core.network.model.NetworkUserData
import com.voximplant.sdk3demo.core.network.model.asUserData
import javax.inject.Inject

class UserDataRepository @Inject constructor(
    private val datasource: UserDataSource,
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
