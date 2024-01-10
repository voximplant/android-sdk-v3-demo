/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.domain

import com.voximplant.demos.sdk.core.data.repository.AudioCallRepository
import javax.inject.Inject

class SendDtmfUseCase @Inject constructor(
    private val audioCallRepository: AudioCallRepository,
) {
    operator fun invoke(value: String) = audioCallRepository.sendDtmf(value)
}
