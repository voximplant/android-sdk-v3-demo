/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.domain

import com.voximplant.demos.sdk.core.data.repository.AudioCallRepository
import com.voximplant.demos.sdk.core.model.data.Call
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCallUseCase @Inject constructor(
    private val callRepository: AudioCallRepository,
) {
    operator fun invoke(): Flow<Call?> = callRepository.callFlow
}
