package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.AudioDeviceRepository
import com.voximplant.sdk3demo.core.model.data.AudioDevice
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAudioDevicesUseCase @Inject constructor(
    private val audioDeviceRepository: AudioDeviceRepository,
) {
    operator fun invoke(): Flow<List<AudioDevice>> = audioDeviceRepository.audioDevices
}
