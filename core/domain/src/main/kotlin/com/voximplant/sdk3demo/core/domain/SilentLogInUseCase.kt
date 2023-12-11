package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.UserDataRepository
import com.voximplant.sdk3demo.core.datastore.UserPreferencesDataSource
import com.voximplant.sdk3demo.core.model.data.AuthError
import com.voximplant.sdk3demo.core.model.data.User
import com.voximplant.sdk3demo.core.model.data.UserCredentials
import com.voximplant.sdk3demo.core.model.data.UserData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class SilentLogInUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) {
    private var attempt = 0

    suspend operator fun invoke(): Result<User> {
        delay(attempt * attemptDelay)
        attempt++

        userPreferencesDataSource.userData.firstOrNull().let { userData ->
            if (userData == null) {
                attempt = 0
                return Result.failure(AuthError.NoInfo)
            }
            userDataRepository.logInWithToken(userData.user.username, userData.accessToken).let { userDataResult ->
                userDataResult.fold(
                    onSuccess = { userData ->
                        userPreferencesDataSource.updateUser(userData)
                        attempt = 0
                        return Result.success(userData.user)
                    },
                    onFailure = { throwable ->
                        if (throwable in listOf(AuthError.TimeOut, AuthError.NetworkIssue) && attempt < maxAttempts) {
                            return invoke()
                        } else if (throwable is AuthError.TokenExpired) {
                            refreshToken(userData).let { refreshResult ->
                                refreshResult.fold(
                                    onSuccess = { return invoke() },
                                    onFailure = { return Result.failure(throwable) },
                                )
                            }
                        } else {
                            attempt = 0
                            return Result.failure(throwable)
                        }
                    },
                )
            }
        }
    }

    private suspend fun refreshToken(userData: UserData): Result<UserCredentials> {
        userDataRepository.refreshToken(userData.user.username, userData.refreshToken).let { authParamsResult ->
            authParamsResult.fold(
                onSuccess = { authParams ->
                    userPreferencesDataSource.updateTokens(authParams.accessToken, authParams.refreshToken)
                    return Result.success(authParams)
                },
                onFailure = { throwable ->
                    if (throwable in listOf(AuthError.TimeOut, AuthError.NetworkIssue) && attempt < maxAttempts) {
                        return refreshToken(userData)
                    } else {
                        attempt = 0
                        return Result.failure(throwable)
                    }
                },
            )
        }
    }

    companion object {
        const val maxAttempts: Int = 3
        const val attemptDelay: Long = 500
    }
}
