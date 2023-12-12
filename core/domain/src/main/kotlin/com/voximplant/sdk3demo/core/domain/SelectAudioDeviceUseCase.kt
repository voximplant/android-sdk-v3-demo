package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.AudioDeviceRepository
import com.voximplant.sdk3demo.core.model.data.AudioDevice
import javax.inject.Inject

class SelectAudioDeviceUseCase @Inject constructor(
    private val audioDeviceRepository: AudioDeviceRepository,
) {
    operator fun invoke(audioDevice: AudioDevice) = audioDeviceRepository.selectAudioDevice(audioDevice)
}
