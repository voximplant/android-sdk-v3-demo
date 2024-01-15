/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.data.util

interface PushManager {
    suspend fun onMessageReceived(push: MutableMap<String, String>, highPriority: Boolean)
    suspend fun onTokenUpdated(token: String)
}
