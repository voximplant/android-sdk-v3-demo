/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.model.data

data class AudioDevice(
    val hasMic: Boolean,
    val id: Int?,
    val name: String,
    val type: Type,
) {
    enum class Type { UNKNOWN, EARPIECE, SPEAKER, WIRED_HEADSET, BLUETOOTH, USB }
}
