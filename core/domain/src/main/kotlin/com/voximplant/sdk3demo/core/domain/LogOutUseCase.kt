package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.AuthDataRepository
import javax.inject.Inject

class LogOutUseCase @Inject constructor(
    private val authDataRepository: AuthDataRepository,
) {
    suspend operator fun invoke() = authDataRepository.logOut()
}
