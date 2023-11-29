package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.datastore.UserPreferencesDataSource
import com.voximplant.sdk3demo.core.model.data.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
) {
    operator fun invoke(): Flow<User?> = userPreferencesDataSource.userData.map { userData -> userData?.user }
}
