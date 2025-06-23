/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.common

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class VoxBroadcastReceiver(
    val onHangUpReceived: () -> Unit,
    val onRejectReceived: () -> Unit,
    val onAnswerReceived: () -> Unit,
    val onToggleMuteReceived: () -> Unit,
    val onToggleCameraReceived: () -> Unit,
) : BroadcastReceiver() {
    private var registered = false

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_HANG_UP_CALL -> {
                onHangUpReceived()
            }

            ACTION_REJECT_CALL -> {
                onRejectReceived()
            }

            ACTION_TOGGLE_MUTE -> {
                onToggleMuteReceived()
            }

            ACTION_TOGGLE_CAMERA -> {
                onToggleCameraReceived()
            }

            Intent.ACTION_ANSWER -> {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    onAnswerReceived()
                } else {
                    // Start an activity to request permissions
                    context.startActivity(intent)
                }
            }
        }
    }

    fun register(context: Context) {
        if (!registered) {
            val filter = IntentFilter().apply {
                addAction(ACTION_HANG_UP_CALL)
                addAction(ACTION_REJECT_CALL)
                addAction(ACTION_TOGGLE_MUTE)
                addAction(ACTION_TOGGLE_CAMERA)
                addAction(Intent.ACTION_ANSWER)
            }
            ContextCompat.registerReceiver(context, this, filter, ContextCompat.RECEIVER_EXPORTED)
            registered = true
        }
    }

    fun unregister(context: Context) {
        if (registered) {
            context.unregisterReceiver(this)
            registered = false
        }
    }

    companion object {
        const val ACTION_HANG_UP_CALL = "com.voximplant.demos.sdk.ACTION_HANG_UP_CALL"
        const val ACTION_REJECT_CALL = "com.voximplant.demos.sdk.ACTION_REJECT_CALL"
        const val ACTION_TOGGLE_MUTE = "com.voximplant.demos.sdk.ACTION_TOGGLE_MUTE"
        const val ACTION_TOGGLE_CAMERA = "com.voximplant.demos.sdk.ACTION_TOGGLE_CAMERA"
    }
}
