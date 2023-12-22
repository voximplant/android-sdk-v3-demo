/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.data.repository

import com.voximplant.demos.sdk.core.foundation.AudioDeviceDataSource
import com.voximplant.demos.sdk.core.model.data.AudioDevice
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
