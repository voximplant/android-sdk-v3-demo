package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.UserDataRepository
import com.voximplant.sdk3demo.core.datastore.UserPreferencesDataSource
import com.voximplant.sdk3demo.core.model.data.AuthError
import com.voximplant.sdk3demo.core.model.data.User
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class SilentLogInUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) {
    suspend operator fun invoke(): Result<User> {
        userPreferencesDataSource.userData.firstOrNull().let { userData ->
            if (userData == null) {
                return Result.failure(AuthError.InternalError)
            }
            userDataRepository.logInWithToken(userData.user.username, userData.accessToken).let { userDataResult ->
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
}
