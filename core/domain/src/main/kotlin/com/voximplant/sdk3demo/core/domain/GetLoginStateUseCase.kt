package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.AuthDataRepository
import com.voximplant.sdk3demo.core.model.data.LoginState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLoginStateUseCase @Inject constructor(
    private val authDataRepository: AuthDataRepository,
) {
    operator fun invoke(): Flow<LoginState> = authDataRepository.loginState
}
