package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.AuthDataRepository
import com.voximplant.sdk3demo.core.model.data.LoginError
import com.voximplant.sdk3demo.core.model.data.User
import com.voximplant.sdk3demo.core.model.data.UserCredentials
import kotlinx.coroutines.delay
import javax.inject.Inject

class SilentLogInUseCase @Inject constructor(
    private val authDataRepository: AuthDataRepository,
) {
    private var attempt = 0

    suspend operator fun invoke(): Result<User> {
        delay(attempt * attemptDelay)
        attempt++
        authDataRepository.logInWithToken().let { userResult ->
            userResult.fold(
                onSuccess = { user ->
                    attempt = 0
                    return Result.success(user)
                },
                onFailure = { throwable ->
                    if (throwable in listOf(LoginError.TimeOut, LoginError.NetworkIssue, LoginError.InternalError) && attempt < maxAttempts) {
                        return invoke()
                    } else if (throwable is LoginError.TokenExpired) {
                        refreshToken().let { refreshResult ->
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

    private suspend fun refreshToken(): Result<UserCredentials> {
        authDataRepository.refreshToken().let { authParamsResult ->
            authParamsResult.fold(
                onSuccess = { authParams ->
                    return Result.success(authParams)
                },
                onFailure = { throwable ->
                    if (throwable in listOf(LoginError.TimeOut, LoginError.NetworkIssue, LoginError.InternalError) && attempt < maxAttempts) {
                        return refreshToken()
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
