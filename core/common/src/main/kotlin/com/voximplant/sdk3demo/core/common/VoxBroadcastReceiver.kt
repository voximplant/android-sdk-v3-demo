package com.voximplant.sdk3demo.core.common

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

            ACTION_ANSWER_CALL -> {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    onAnswerReceived()
                } else {
                    // Start an activity to request permissions
                    val answerIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                        action = ACTION_ANSWER_CALL
                        putExtras(intent)
                    }
                    context.startActivity(answerIntent)
                }
            }
        }
    }

    fun register(context: Context) {
        if (!registered) {
            val filter = IntentFilter().apply {
                addAction(ACTION_HANG_UP_CALL)
                addAction(ACTION_REJECT_CALL)
                addAction(ACTION_ANSWER_CALL)
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
        const val ACTION_HANG_UP_CALL = "com.voximplant.sdk3demo.ACTION_HANG_UP_CALL"
        const val ACTION_REJECT_CALL = "com.voximplant.sdk3demo.ACTION_REJECT_CALL"
        const val ACTION_ANSWER_CALL = "com.voximplant.sdk3demo.ACTION_ANSWER_CALL"
    }
}
