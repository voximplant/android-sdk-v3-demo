/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.notifications

import android.app.Notification

interface Notifier {
    fun createOngoingCallNotification(id: String, displayName: String?, isOngoing: Boolean): Notification?
    fun createIncomingAudioCallNotification(id: String, displayName: String?): Notification?
    fun createIncomingVideoCallNotification(id: String, displayName: String?): Notification?
    fun postIncomingAudioCallNotification(id: String, displayName: String?)
    fun postIncomingVideoCallNotification(id: String, displayName: String?)
    fun cancelCallNotification()
}
