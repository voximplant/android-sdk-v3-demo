package com.voximplant.core.notifications

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
import com.voximplant.sdk3demo.core.common.VoxBroadcastReceiver
import com.voximplant.sdk3demo.core.notifications.R
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

    override fun postIncomingCallNotification(id: String, displayName: String?) = with(context) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return

        val notificationManager = NotificationManagerCompat.from(this)
        val incomingCallNotification = createIncomingCallNotification(id, displayName)

        notificationManager.notify(CALL_NOTIFICATION_ID, incomingCallNotification)
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

    val answerIntent = Intent(VoxBroadcastReceiver.ACTION_ANSWER_CALL).apply {
        putExtra("id", id)
        putExtra("displayName", displayName)
    }

    val answerPendingIntent = PendingIntent.getBroadcast(
        this,
        INCOMING_CALL_NOTIFICATION_REQUEST_CODE,
        answerIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val caller = Person.Builder().setName(displayName ?: getString(com.voximplant.sdk3demo.core.resource.R.string.unknown_user)).setImportant(false).build()

    return NotificationCompat.Builder(this, INCOMING_CALL_NOTIFICATION_CHANNEL_ID).apply {
        setFullScreenIntent(incomingCallPendingIntent, true)
        setContentIntent(incomingCallPendingIntent)
        setSmallIcon(com.voximplant.sdk3demo.core.common.R.drawable.ic_notification)
        priority = NotificationCompat.PRIORITY_MAX
        setCategory(NotificationCompat.CATEGORY_CALL)
        setStyle(NotificationCompat.CallStyle.forIncomingCall(caller, rejectPendingIntent, answerPendingIntent))
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

    val callee = Person.Builder().setName(displayName ?: getString(com.voximplant.sdk3demo.core.resource.R.string.unknown_user)).setImportant(true).build()

    return NotificationCompat.Builder(this, ONGOING_CALL_NOTIFICATION_CHANNEL_ID).apply {
        setOngoing(true)
        setUsesChronometer(true)
        setFullScreenIntent(ongoingCallIntent, false)
        setContentIntent(ongoingCallIntent)
        setSmallIcon(com.voximplant.sdk3demo.core.common.R.drawable.ic_notification)
        setStyle(NotificationCompat.CallStyle.forOngoingCall(callee, hangUpPendingIntent))
    }.build()
}

private fun Context.createIncomingCallNotificationChannel() {
    val channel = NotificationChannelCompat.Builder(
        INCOMING_CALL_NOTIFICATION_CHANNEL_ID,
        NotificationManagerCompat.IMPORTANCE_MAX,
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
