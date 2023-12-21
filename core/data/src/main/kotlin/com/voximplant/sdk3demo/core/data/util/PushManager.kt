package com.voximplant.sdk3demo.core.data.util

interface PushManager {
    suspend fun onMessageReceived(push: MutableMap<String, String>)
    suspend fun onTokenUpdated(token: String)
}
