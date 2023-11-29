package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.UserDataRepository
import com.voximplant.sdk3demo.core.datastore.UserPreferencesDataSource
import com.voximplant.sdk3demo.core.model.data.User
import javax.inject.Inject

class LogInUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        userDataRepository.logIn(username, password).let { userDataResult ->
            userDataResult.fold(
                onSuccess = { userData ->
                    userPreferencesDataSource.updateUser(userData)
                    return Result.success(userData.user)
                },
                onFailure = { throwable ->
                    return Result.failure(throwable)
                },
            )
        }
    }
}
