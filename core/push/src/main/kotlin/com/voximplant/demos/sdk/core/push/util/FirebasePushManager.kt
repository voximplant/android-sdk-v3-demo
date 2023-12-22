/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.push.util

import com.voximplant.demos.sdk.core.data.repository.AudioCallRepository
import com.voximplant.demos.sdk.core.data.repository.AuthDataRepository
import com.voximplant.demos.sdk.core.data.util.PushManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePushManager @Inject constructor(
    private val authDataRepository: AuthDataRepository,
    private val callRepository: AudioCallRepository,
) : PushManager {

    override suspend fun onMessageReceived(push: MutableMap<String, String>) {
        authDataRepository.handlePush(push)
        callRepository.startListeningForIncomingCalls()
    }

    override suspend fun onTokenUpdated(token: String) {
        authDataRepository.updatePushToken(token)
    }
}
