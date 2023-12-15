package com.voximplant.sdk3demo.core.foundation

import com.voximplant.core.audio.AudioDevice
import com.voximplant.core.audio.AudioDeviceEventsListener
import com.voximplant.core.audio.AudioDeviceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class AudioDeviceDataSource(
    private val audioDeviceManager: AudioDeviceManager,
) {
    private val _audioDevices: MutableStateFlow<List<AudioDevice>> = MutableStateFlow(audioDeviceManager.audioDevices)
    val audioDevices: Flow<List<com.voximplant.sdk3demo.core.model.data.AudioDevice>> = _audioDevices.asStateFlow().map { audioDevices ->
        audioDevices.map { audioDevice ->
            audioDevice.asExternalModel()
        }
    }

    private val _selectedAudioDevice: MutableStateFlow<AudioDevice?> = MutableStateFlow(audioDeviceManager.selectedAudioDevice)
    val selectedAudioDevice: Flow<com.voximplant.sdk3demo.core.model.data.AudioDevice?> = _selectedAudioDevice.asStateFlow().map { audioDevice ->
        audioDevice?.asExternalModel()
    }

    init {
        audioDeviceManager.addAudioDeviceEventsListener(
            object : AudioDeviceEventsListener {
                override fun onAudioDeviceChanged(audioDevice: AudioDevice) {
                    _selectedAudioDevice.value = audioDevice
                }

                override fun onAudioDeviceListChanged(audioDevices: List<AudioDevice>) {
                    _audioDevices.value = audioDevices
                }
            },
        )
    }

    fun selectAudioDevice(value: com.voximplant.sdk3demo.core.model.data.AudioDevice) {
        audioDeviceManager.audioDevices.find { audioDevice -> audioDevice.asExternalModel() == value }?.run {
            audioDeviceManager.selectAudioDevice(this)
        }
    }
}