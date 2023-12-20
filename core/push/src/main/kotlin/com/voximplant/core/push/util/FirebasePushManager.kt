package com.voximplant.core.push.util

import com.voximplant.sdk3demo.core.data.repository.AudioCallRepository
import com.voximplant.sdk3demo.core.data.repository.AuthDataRepository
import com.voximplant.sdk3demo.core.data.util.PushManager
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
        authDataRepository.updateAccessToken(token)
    }
}
