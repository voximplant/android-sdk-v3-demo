/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.ui.util

fun formatDuration(durationInMillis: Long): String {
    val minutes = (durationInMillis / 1000) / 60
    val seconds = (durationInMillis / 1000) % 60
    return String.format("%02d:%02d", minutes, seconds)
}