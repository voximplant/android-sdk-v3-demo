/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.push.util

import com.google.firebase.messaging.FirebaseMessaging
import com.voximplant.demos.sdk.core.data.util.PushTokenProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePushTokenProvider @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
) : PushTokenProvider {
    override suspend fun getToken(): String = firebaseMessaging.token.await()
}
