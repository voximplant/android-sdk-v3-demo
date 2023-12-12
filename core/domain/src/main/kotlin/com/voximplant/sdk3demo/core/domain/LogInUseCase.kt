package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.UserDataRepository
import com.voximplant.sdk3demo.core.datastore.UserPreferencesDataSource
import com.voximplant.sdk3demo.core.model.data.AuthError
import com.voximplant.sdk3demo.core.model.data.User
import kotlinx.coroutines.delay
import javax.inject.Inject

class LogInUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) {
    private var attempt = 0

    suspend operator fun invoke(username: String, password: String): Result<User> {
        delay(attempt * attemptDelay)
        attempt++

        userDataRepository.logIn(username, password).let { userDataResult ->
            userDataResult.fold(
                onSuccess = { userData ->
                    userPreferencesDataSource.updateUser(userData)
                    attempt = 0
                    return Result.success(userData.user)
                },
                onFailure = { throwable ->
                    if (throwable in listOf(AuthError.TimeOut, AuthError.NetworkIssue) && attempt < maxAttempts) {
                        return invoke(username, password)
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
