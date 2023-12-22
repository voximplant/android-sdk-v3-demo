/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.domain

import com.voximplant.demos.sdk.core.data.repository.AudioDeviceRepository
import com.voximplant.demos.sdk.core.model.data.AudioDevice
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAudioDeviceUseCase @Inject constructor(
    private val audioDeviceRepository: AudioDeviceRepository,
) {
    operator fun invoke(): Flow<AudioDevice?> = audioDeviceRepository.selectedAudioDevice
}
