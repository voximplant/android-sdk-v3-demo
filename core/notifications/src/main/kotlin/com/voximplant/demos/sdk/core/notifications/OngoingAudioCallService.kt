/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.notifications

import android.Manifest
import android.app.ForegroundServiceStartNotAllowedException
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.voximplant.demos.sdk.core.logger.Logger

class OngoingAudioCallService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Before starting the service as foreground check that the app has the
        // appropriate runtime permissions. In this case, verify that the user has
        // granted the RECORD_AUDIO permission.
        val recordAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        if (recordAudioPermission == PackageManager.PERMISSION_DENIED) {
            // Without microphone permissions the service cannot run in the foreground
            // Consider informing user or updating your app UI if visible.
            stopSelf()
            return START_NOT_STICKY
        }

        val id = intent?.getStringExtra("id")
        val displayName = intent?.getStringExtra("displayName")

        if (id == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = SystemTrayNotifier(applicationContext).createOngoingCallNotification(id, displayName)
        if (notification == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            ServiceCompat.startForeground(
                this,
                1,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                } else {
                    0
                },
            )

        } catch (exception: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && exception is ForegroundServiceStartNotAllowedException) {
                Logger.error("OngoingAudioCallService::exception", exception)
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
