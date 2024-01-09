/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.notifications

import android.app.Notification

interface Notifier {
    fun createOngoingCallNotification(id: String, displayName: String?): Notification?
    fun createIncomingCallNotification(id: String, displayName: String?): Notification?
    fun cancelCallNotification()
}
