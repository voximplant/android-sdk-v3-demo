package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.AuthDataRepository
import javax.inject.Inject

class GetNodeUseCase @Inject constructor(
    private val authDataRepository: AuthDataRepository,
) {

    operator fun invoke() = authDataRepository.node
}
