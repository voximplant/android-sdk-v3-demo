/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.data.services

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
import com.voximplant.demos.sdk.core.notifications.SystemTrayNotifier

class VideoCallOngoingService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Before starting the service as foreground check that the app has the
        // appropriate runtime permissions. In this case, verify that the user has
        // granted the RECORD_AUDIO and CAMERA permission.
        val recordAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (recordAudioPermission == PackageManager.PERMISSION_DENIED && cameraPermission == PackageManager.PERMISSION_DENIED) {
            // Without microphone and camera permissions the service cannot run in the foreground
            // Consider informing user or updating your app UI if visible.
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }

        val id = intent?.getStringExtra("id")
        val displayName = intent?.getStringExtra("displayName")
        var isOngoing = false
        intent?.getBooleanExtra("isOngoing", false)?.let { isOngoing = it }

        if (id == null) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }

        val notification = SystemTrayNotifier(applicationContext).createOngoingCallNotification(id, displayName, isOngoing)

        try {
            ServiceCompat.startForeground(
                this,
                1,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                } else {
                    0
                },
            )

        } catch (exception: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && exception is ForegroundServiceStartNotAllowedException) {
                Logger.error("VideoCallOngoingService::exception", exception)
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}