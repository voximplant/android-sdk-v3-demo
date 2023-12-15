package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.AuthDataRepository
import com.voximplant.sdk3demo.core.datastore.UserPreferencesDataSource
import com.voximplant.sdk3demo.core.model.data.LoginError
import com.voximplant.sdk3demo.core.model.data.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class SilentLogInUseCase @Inject constructor(
    private val authDataRepository: AuthDataRepository,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) {
    private var attempt = 0

    suspend operator fun invoke(): Result<User> {
        delay(attempt * attemptDelay)
        attempt++

        userPreferencesDataSource.userData.firstOrNull().let { userData ->
            if (userData == null) {
                attempt = 0
                return Result.failure(LoginError.InternalError)
            }
            authDataRepository.logInWithToken(userData.user.username, userData.accessToken).let { userDataResult ->
                userDataResult.fold(
                    onSuccess = { userData ->
                        userPreferencesDataSource.updateUser(userData)
                        attempt = 0
                        return Result.success(userData.user)
                    },
                    onFailure = { throwable ->
                        if (throwable in listOf(LoginError.TimeOut, LoginError.NetworkIssue, LoginError.InternalError) && attempt < maxAttempts) {
                            return invoke()
                        } else {
                            attempt = 0
                            return Result.failure(throwable)
                        }
                    },
                )
            }
        }
    }

    companion object {
        const val maxAttempts: Int = 3
        const val attemptDelay: Long = 500
    }
}
