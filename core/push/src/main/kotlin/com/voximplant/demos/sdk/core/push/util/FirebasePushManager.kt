/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.push.util

import com.voximplant.demos.sdk.core.calls.CallDataSource
import com.voximplant.demos.sdk.core.data.repository.AudioCallRepository
import com.voximplant.demos.sdk.core.data.repository.AuthDataRepository
import com.voximplant.demos.sdk.core.data.repository.VideoCallRepository
import com.voximplant.demos.sdk.core.data.util.PushManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePushManager @Inject constructor(
    private val authDataRepository: AuthDataRepository,
    private val callDataSource: CallDataSource,
    private val audioCallDataRepository: AudioCallRepository,
    private val videoCallDataRepository: VideoCallRepository,
) : PushManager {

    override suspend fun onMessageReceived(push: MutableMap<String, String>, highPriority: Boolean) {
        authDataRepository.handlePush(push)
        if (highPriority) {
            audioCallDataRepository.handlePush()
            videoCallDataRepository.handlePush()
        }
        callDataSource.startListeningForIncomingCalls()
    }

    override suspend fun onTokenUpdated(token: String) {
        authDataRepository.updatePushToken(token)
    }
}
