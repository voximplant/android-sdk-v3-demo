/*
 * Copyright (c) 2011 - 2025, Voximplant, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.foundation

import com.voximplant.android.sdk.core.audio.AudioDevice
import com.voximplant.android.sdk.core.audio.AudioDeviceType
import com.voximplant.android.sdk.core.audio.AudioDeviceType.Bluetooth
import com.voximplant.android.sdk.core.audio.AudioDeviceType.Earpiece
import com.voximplant.android.sdk.core.audio.AudioDeviceType.Speaker
import com.voximplant.android.sdk.core.audio.AudioDeviceType.Usb
import com.voximplant.android.sdk.core.audio.AudioDeviceType.WiredHeadset
import com.voximplant.demos.sdk.core.model.data.AudioDevice.Type

fun AudioDevice.asExternalModel() = com.voximplant.demos.sdk.core.model.data.AudioDevice(
    hasMic = hasMic,
    id = id,
    name = getAudioDeviceName(type) ?: name,
    type = type.asExternalModel,
)

val AudioDeviceType.asExternalModel
    get() = when (this) {
        Earpiece -> Type.EARPIECE
        Speaker -> Type.SPEAKER
        WiredHeadset -> Type.WIRED_HEADSET
        Bluetooth -> Type.BLUETOOTH
        Usb -> Type.USB
    }

private fun getAudioDeviceName(audioDeviceType: AudioDeviceType): String? {
    return when (audioDeviceType) {
        Earpiece -> "Earpiece"
        Speaker -> "Speaker"
        WiredHeadset -> "WiredHeadset"
        Bluetooth, Usb -> null
    }

}
