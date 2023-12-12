package com.voximplant.sdk3demo.core.model.data

data class AudioDevice(
    val hasMic: Boolean,
    val id: Int?,
    val name: String,
    val type: Type,
) {
    enum class Type { UNKNOWN, EARPIECE, SPEAKER, WIRED_HEADSET, BLUETOOTH, USB }
}
