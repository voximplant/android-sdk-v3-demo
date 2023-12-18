package com.voximplant.core.notifications

interface Notifier {
    fun postOngoingCallNotification(id: String, displayName: String?)
    fun postIncomingCallNotification(id: String, displayName: String?)
    fun cancelCallNotification()
}
