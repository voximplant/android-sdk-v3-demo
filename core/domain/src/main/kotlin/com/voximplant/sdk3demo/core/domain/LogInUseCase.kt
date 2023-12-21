package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.AuthDataRepository
import com.voximplant.sdk3demo.core.model.data.LoginError
import com.voximplant.sdk3demo.core.model.data.Node
import com.voximplant.sdk3demo.core.model.data.User
import kotlinx.coroutines.delay
import javax.inject.Inject

class LogInUseCase @Inject constructor(
    private val authDataRepository: AuthDataRepository,
) {
    private var attempt = 0

    suspend operator fun invoke(username: String, password: String, node: Node): Result<User> {
        delay(attempt * attemptDelay)
        attempt++

        authDataRepository.logIn(username, password, node).let { userResult ->
            userResult.fold(
                onSuccess = { user ->
                    attempt = 0
                    return Result.success(user)
                },
                onFailure = { throwable ->
                    if (throwable in listOf(LoginError.TimeOut, LoginError.NetworkIssue, LoginError.InternalError) && attempt < maxAttempts) {
                        return invoke(username, password, node)
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
