package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.UserDataRepository
import com.voximplant.sdk3demo.core.datastore.UserPreferencesDataSource
import javax.inject.Inject

class LogOutUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) {
    suspend operator fun invoke() {
        userDataRepository.logOut()
        userPreferencesDataSource.clearUser()
    }
}
