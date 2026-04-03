/*
 * Copyright (c) 2011 - 2026, Voximplant, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.push.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.voximplant.demos.sdk.core.calls.CallDataSource
import com.voximplant.demos.sdk.core.data.repository.AudioCallRepository
import com.voximplant.demos.sdk.core.data.repository.AuthDataRepository
import com.voximplant.demos.sdk.core.data.repository.VideoCallRepository
import com.voximplant.demos.sdk.core.data.services.BackgroundPushService
import com.voximplant.demos.sdk.core.data.util.PushManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePushManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authDataRepository: AuthDataRepository,
    private val callDataSource: CallDataSource,
    private val audioCallDataRepository: AudioCallRepository,
    private val videoCallDataRepository: VideoCallRepository,
) : PushManager {

    override suspend fun onMessageReceived(push: Map<String, String>, highPriority: Boolean) {
        if (audioCallDataRepository.isIncomingCallServiceStart || videoCallDataRepository.isIncomingVideoCallServiceStart) {
            authDataRepository.handlePush(push)
        } else {
            val intent = Intent(context, BackgroundPushService::class.java).apply {
                putExtra("push", Bundle().apply {
                    for ((key, value) in push) {
                        putString(key, value)
                    }
                })
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        callDataSource.startListeningForIncomingCalls()
    }

    override suspend fun onTokenUpdated(token: String) {
        authDataRepository.updatePushToken(token)
    }
}
