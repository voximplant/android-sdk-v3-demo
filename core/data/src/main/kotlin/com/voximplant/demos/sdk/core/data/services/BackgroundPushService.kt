/*
 * Copyright (c) 2011 - 2026, Voximplant, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.data.services

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.voximplant.demos.sdk.core.common.di.ApplicationScope
import com.voximplant.demos.sdk.core.data.repository.AuthDataRepository
import com.voximplant.demos.sdk.core.logger.Logger
import com.voximplant.demos.sdk.core.notifications.SystemTrayNotifier
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class BackgroundPushService : Service() {

    @Inject
    lateinit var authDataRepository: AuthDataRepository

    @Inject
    @ApplicationScope
    lateinit var scope: CoroutineScope

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notification = SystemTrayNotifier(applicationContext).createBackgroundPushNotification()

        val restoredMap = mutableMapOf<String, String>()
        val data = intent?.getBundleExtra("push")

        if (data != null) {
            val keys = data.keySet()

            for (key in keys) {
                val value = data.getString(key)
                if (value != null) {
                    restoredMap[key] = value
                }
            }
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
                Logger.error("BackgroundPushService::exception: $exception")
            }
        }

        runBlocking {
            authDataRepository.handlePush(restoredMap.toMap())
        }

        return START_NOT_STICKY
    }

    override fun onTimeout(startId: Int) {
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
