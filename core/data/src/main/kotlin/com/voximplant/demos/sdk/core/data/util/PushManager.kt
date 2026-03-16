/*
 * Copyright (c) 2011 - 2026, Voximplant, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.data.util

interface PushManager {
    suspend fun onMessageReceived(push: Map<String, String>, highPriority: Boolean)
    suspend fun onTokenUpdated(token: String)
}
