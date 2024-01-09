/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.notifications

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat

class AudioCallIncomingService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val id = intent?.getStringExtra("id")
        val displayName = intent?.getStringExtra("displayName")

        if (id == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = SystemTrayNotifier(applicationContext).createIncomingCallNotification(id, displayName)
        if (notification == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            ServiceCompat.startForeground(
                this,
                1,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
                } else {
                    0
                },
            )
        } catch (exception: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && exception is ForegroundServiceStartNotAllowedException) {
                Log.e("DemoV3", "AudioCallIncomingService::exception: $exception")
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
