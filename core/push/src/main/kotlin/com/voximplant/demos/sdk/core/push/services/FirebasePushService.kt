/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.push.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.voximplant.demos.sdk.core.common.di.ApplicationScope
import com.voximplant.demos.sdk.core.data.util.PushManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@AndroidEntryPoint
class FirebasePushService : FirebaseMessagingService() {

    @Inject
    lateinit var pushManager: PushManager

    @Inject
    @ApplicationScope
    lateinit var coroutineScope: CoroutineScope

    override fun onMessageReceived(message: RemoteMessage) {
        val push = message.data
        if (push.containsKey("voximplant")) {
            coroutineScope.launch {
                pushManager.onMessageReceived(push)
            }
        }
    }

    override fun onNewToken(token: String) {
        coroutineScope.launch {
            pushManager.onTokenUpdated(token)
        }
    }
}
