package com.voximplant.sdk3demo.core.data.util

interface PushTokenProvider {
    suspend fun getToken(): String
}
