/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.domain

import com.voximplant.demos.sdk.core.data.repository.AuthDataRepository
import com.voximplant.demos.sdk.core.model.data.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val authDataRepository: AuthDataRepository,
) {
    operator fun invoke(): Flow<User?> = authDataRepository.user
}
