package com.voximplant.core.notifications

import android.app.Notification

interface Notifier {
    fun createOngoingCallNotification(id: String, displayName: String?): Notification?
    fun postIncomingCallNotification(id: String, displayName: String?)
    fun cancelCallNotification()
}
