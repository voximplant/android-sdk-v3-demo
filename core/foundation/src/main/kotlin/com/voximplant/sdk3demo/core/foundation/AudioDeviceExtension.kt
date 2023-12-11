package com.voximplant.sdk3demo.core.foundation

import com.voximplant.core.audio.AudioDevice
import com.voximplant.core.audio.AudioDeviceType
import com.voximplant.core.audio.AudioDeviceType.BLUETOOTH
import com.voximplant.core.audio.AudioDeviceType.EARPIECE
import com.voximplant.core.audio.AudioDeviceType.SPEAKER
import com.voximplant.core.audio.AudioDeviceType.UNKNOWN
import com.voximplant.core.audio.AudioDeviceType.USB
import com.voximplant.core.audio.AudioDeviceType.WIRED_HEADSET
import com.voximplant.sdk3demo.core.model.data.AudioDevice.Type

fun AudioDevice.asExternalModel() = com.voximplant.sdk3demo.core.model.data.AudioDevice(
    hasMic = hasMic,
    id = id,
    name = name,
    type = type.asExternalModel,
)

val AudioDeviceType.asExternalModel
    get() = when (this) {
        UNKNOWN -> Type.UNKNOWN
        EARPIECE -> Type.EARPIECE
        SPEAKER -> Type.SPEAKER
        WIRED_HEADSET -> Type.WIRED_HEADSET
        BLUETOOTH -> Type.BLUETOOTH
        USB -> Type.USB
    }
