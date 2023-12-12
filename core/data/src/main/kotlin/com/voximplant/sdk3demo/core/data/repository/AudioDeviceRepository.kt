package com.voximplant.sdk3demo.core.data.repository

import com.voximplant.sdk3demo.core.foundation.AudioDeviceDataSource
import com.voximplant.sdk3demo.core.model.data.AudioDevice
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AudioDeviceRepository @Inject constructor(
    private val audioDeviceDataSource: AudioDeviceDataSource,
) {
    val audioDevices: Flow<List<AudioDevice>>
        get() = audioDeviceDataSource.audioDevices

    val selectedAudioDevice: Flow<AudioDevice?>
        get() = audioDeviceDataSource.selectedAudioDevice

    fun selectAudioDevice(value: AudioDevice) = audioDeviceDataSource.selectAudioDevice(value)
}
