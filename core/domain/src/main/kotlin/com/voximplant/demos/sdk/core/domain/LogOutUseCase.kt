/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.domain

import com.voximplant.demos.sdk.core.data.repository.AuthDataRepository
import javax.inject.Inject

class LogOutUseCase @Inject constructor(
    private val authDataRepository: AuthDataRepository,
) {
    suspend operator fun invoke() = authDataRepository.logOut()
}
