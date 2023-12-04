package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.UserDataRepository
import com.voximplant.sdk3demo.core.datastore.UserPreferencesDataSource
import javax.inject.Inject

class MakeCallUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) {
    private var attempt = 0

    suspend operator fun invoke(username: String) {

    }

    companion object
}
