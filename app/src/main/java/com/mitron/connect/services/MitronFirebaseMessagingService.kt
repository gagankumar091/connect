package com.mitron.connect.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mitron.connect.MainActivity
import com.mitron.connect.data.SessionManager
import com.mitron.connect.data.VercelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MitronFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        SessionManager.init(this)
        val userId = SessionManager.getUserId()
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    VercelRepository().updateFcmToken(token)
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "New Notification"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        val type = remoteMessage.data["type"] ?: ""
        val chatId = remoteMessage.data["chatId"] ?: ""
        val actionId = remoteMessage.data["action_id"] ?: ""

        sendNotification(title, body, type, chatId, actionId)
    }

    private fun sendNotification(title: String, messageBody: String, type: String, chatId: String, actionId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("type", type)
            putExtra("chatId", chatId)
            putExtra("action_id", actionId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "mitron_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val (tag, notifId) = when (type) {
            "chat" -> NotificationHelper.TAG_PREFIX_CHAT + chatId to chatId.hashCode()
            "connection" -> NotificationHelper.TAG_CONNECTIONS to 1001
            "event" -> NotificationHelper.TAG_EVENTS to 1002
            else -> null to System.currentTimeMillis().toInt()
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.mitron.connect.R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))

        if (tag != null) {
            notificationBuilder.setGroup(tag)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Mitron Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        if (tag != null) {
            notificationManager.notify(tag, notifId, notificationBuilder.build())
        } else {
            notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }
    }
}
