/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.domain

import com.voximplant.demos.sdk.core.data.repository.AudioCallRepository
import javax.inject.Inject

class GetHoldStateUseCase @Inject constructor(
    private val callRepository: AudioCallRepository,
) {
    operator fun invoke() = callRepository.isOnHold
}
