/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.data.util

interface PushTokenProvider {
    suspend fun getToken(): String
}
