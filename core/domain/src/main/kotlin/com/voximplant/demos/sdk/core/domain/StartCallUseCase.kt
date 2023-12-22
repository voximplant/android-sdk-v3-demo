/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.domain

import com.voximplant.demos.sdk.core.data.repository.AudioCallRepository
import com.voximplant.demos.sdk.core.model.data.Call
import javax.inject.Inject

class StartCallUseCase @Inject constructor(
    private val audioCallRepository: AudioCallRepository,
) {

    operator fun invoke(id: String): Result<Call> {
        return audioCallRepository.startCall(id)
    }
}
