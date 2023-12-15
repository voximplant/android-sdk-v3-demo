package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.AuthDataRepository
import com.voximplant.sdk3demo.core.datastore.UserPreferencesDataSource
import javax.inject.Inject

class LogOutUseCase @Inject constructor(
    private val authDataRepository: AuthDataRepository,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) {
    suspend operator fun invoke() {
        authDataRepository.logOut()
        userPreferencesDataSource.clearUser()
    }
}
