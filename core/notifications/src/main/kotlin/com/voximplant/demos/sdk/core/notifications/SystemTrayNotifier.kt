/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.notifications

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.TaskStackBuilder
import com.voximplant.demos.sdk.core.common.VoxBroadcastReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val ONGOING_CALL_NOTIFICATION_REQUEST_CODE = 0
private const val INCOMING_CALL_NOTIFICATION_REQUEST_CODE = 1
private const val CALL_NOTIFICATION_ID = 1
private const val ONGOING_CALL_NOTIFICATION_CHANNEL_ID = "ONGOING_CALL_NOTIFICATIONS"
private const val INCOMING_CALL_NOTIFICATION_CHANNEL_ID = "INCOMING_CALL_NOTIFICATIONS"

@Singleton
class SystemTrayNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) : Notifier {

    override fun createOngoingCallNotification(id: String, displayName: String?): Notification? = with(context) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return null

        return@with createOngoingCallNotification(id, displayName)
    }

    override fun createIncomingCallNotification(id: String, displayName: String?): Notification? = with(context) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return null

        return@with createIncomingCallNotification(id, displayName)
    }

    override fun cancelCallNotification() = with(context) {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.cancel(CALL_NOTIFICATION_ID)
    }
}

private fun Context.createIncomingCallNotification(id: String, displayName: String?): Notification {
    createIncomingCallNotificationChannel()

    val incomingCallPendingIntent = PendingIntent.getActivity(
        this,
        INCOMING_CALL_NOTIFICATION_REQUEST_CODE,
        packageManager.getLaunchIntentForPackage(packageName)?.apply {
            putExtra("id", id)
            putExtra("displayName", displayName)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val rejectIntent = Intent(VoxBroadcastReceiver.ACTION_REJECT_CALL)

    val rejectPendingIntent = PendingIntent.getBroadcast(
        this,
        INCOMING_CALL_NOTIFICATION_REQUEST_CODE,
        rejectIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val answerIntent = Intent(Intent.ACTION_ANSWER).apply {
        putExtra("id", id)
        putExtra("displayName", displayName)
    }

    val answerPendingIntent: PendingIntent? = if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
        PendingIntent.getBroadcast(
            this,
            INCOMING_CALL_NOTIFICATION_REQUEST_CODE,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    } else {
        TaskStackBuilder.create(this).run {
            addNextIntent(packageManager.getLaunchIntentForPackage(packageName)?.apply {
                action = Intent.ACTION_ANSWER
                putExtra("id", id)
                putExtra("displayName", displayName)
            } ?: return@run null)
            getPendingIntent(
                INCOMING_CALL_NOTIFICATION_REQUEST_CODE,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    val caller = Person.Builder().setName(displayName ?: getString(com.voximplant.demos.sdk.core.resources.R.string.unknown_user)).setImportant(false).build()

    return NotificationCompat.Builder(this, INCOMING_CALL_NOTIFICATION_CHANNEL_ID).apply {
        setFullScreenIntent(incomingCallPendingIntent, true)
        setContentIntent(incomingCallPendingIntent)
        setSmallIcon(com.voximplant.demos.sdk.core.common.R.drawable.ic_notification)
        priority = NotificationCompat.PRIORITY_HIGH
        setCategory(NotificationCompat.CATEGORY_CALL)
        setShowWhen(false)
        setStyle(NotificationCompat.CallStyle.forIncomingCall(caller, rejectPendingIntent, answerPendingIntent ?: incomingCallPendingIntent))
    }.build()
}

private fun Context.createOngoingCallNotification(id: String, displayName: String?): Notification {
    createOngoingCallNotificationChannel()

    val ongoingCallIntent = PendingIntent.getActivity(
        this,
        ONGOING_CALL_NOTIFICATION_REQUEST_CODE,
        packageManager.getLaunchIntentForPackage(packageName)?.apply {
            putExtra("id", id)
            putExtra("displayName", displayName)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val hangUpIntent = Intent(VoxBroadcastReceiver.ACTION_HANG_UP_CALL)

    val hangUpPendingIntent = PendingIntent.getBroadcast(
        this,
        INCOMING_CALL_NOTIFICATION_REQUEST_CODE,
        hangUpIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val callee = Person.Builder().setName(displayName ?: getString(com.voximplant.demos.sdk.core.resources.R.string.unknown_user)).setImportant(true).build()

    return NotificationCompat.Builder(this, ONGOING_CALL_NOTIFICATION_CHANNEL_ID).apply {
        setOngoing(true)
        setUsesChronometer(true)
        setFullScreenIntent(ongoingCallIntent, false)
        setContentIntent(ongoingCallIntent)
        setShowWhen(false)
        setSmallIcon(com.voximplant.demos.sdk.core.common.R.drawable.ic_notification)
        setStyle(NotificationCompat.CallStyle.forOngoingCall(callee, hangUpPendingIntent))
    }.build()
}

private fun Context.createIncomingCallNotificationChannel() {
    val channel = NotificationChannelCompat.Builder(
        INCOMING_CALL_NOTIFICATION_CHANNEL_ID,
        NotificationManagerCompat.IMPORTANCE_HIGH,
    ).apply {
        setName(getString(R.string.incoming_call_notification_channel_name))
        setVibrationEnabled(true)
        setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setLegacyStreamType(AudioManager.STREAM_RING).setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build(),
        )
    }.build()
    NotificationManagerCompat.from(this).createNotificationChannel(channel)
}

private fun Context.createOngoingCallNotificationChannel() {
    val channel = NotificationChannelCompat.Builder(
        ONGOING_CALL_NOTIFICATION_CHANNEL_ID,
        NotificationManagerCompat.IMPORTANCE_DEFAULT,
    ).apply {
        setName(getString(R.string.ongoing_call_notification_channel_name))
        setVibrationEnabled(false)
        setSound(null, null)
    }.build()
    NotificationManagerCompat.from(this).createNotificationChannel(channel)
}
