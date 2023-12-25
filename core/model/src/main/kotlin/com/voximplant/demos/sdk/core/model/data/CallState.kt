/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.model.data

sealed interface CallState {
    data object Created : CallState
    data object Connecting : CallState
    data object Connected : CallState
    data object Reconnecting : CallState
    data object Disconnecting : CallState
    data object Disconnected : CallState
    data class Failed(val description: String?) : CallState
}
