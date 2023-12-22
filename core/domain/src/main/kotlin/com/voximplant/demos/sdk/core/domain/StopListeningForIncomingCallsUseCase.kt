/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.domain

import com.voximplant.demos.sdk.core.data.repository.AudioCallRepository
import javax.inject.Inject

class StopListeningForIncomingCallsUseCase @Inject constructor(
    private val audioCallRepository: AudioCallRepository,
) {

    operator fun invoke() {
        return audioCallRepository.stopListeningForIncomingCalls()
    }
}
