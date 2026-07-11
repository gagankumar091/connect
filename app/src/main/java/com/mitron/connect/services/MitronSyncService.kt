package com.mitron.connect.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mitron.connect.MainActivity
import com.mitron.connect.R
import com.mitron.connect.data.SessionManager
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

class MitronSyncService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var eventSource: EventSource? = null

    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userId = SessionManager.getUserId()
        if (userId != null) {
            connectToStream(userId)
        } else {
            stopSelf()
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "mitron_sync_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Mitron Background Sync",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Mitron")
            .setContentText("Connected and listening for messages")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    private fun connectToStream(userId: String) {
        // Start polling loop
        scope.launch {
            while (isActive) {
                try {
                    val notifications = com.mitron.connect.data.RetrofitClient.apiService.getNotifications(userId)
                    val unread = notifications.filter { !it.isRead }
                    
                    for (notif in unread) {
                        sendLocalNotification(notif.title, notif.description.orEmpty(), notif.type.name, notif.action_id.orEmpty(), notif.action_id.orEmpty())
                        com.mitron.connect.data.RetrofitClient.apiService.markNotificationRead(notif.id)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(5000)
            }
        }

        // Connect to SSE stream for real-time events (chats, calls)
        val request = Request.Builder()
            .url("${com.mitron.connect.BuildConfig.BASE_URL}api/users/$userId/stream")
            .header("Authorization", "Bearer MITRON_SECURE_KEY_2026")
            .build()

        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                super.onEvent(eventSource, id, type, data)
                if (type == "incoming_call") {
                    try {
                        val json = Json { ignoreUnknownKeys = true }.parseToJsonElement(data).jsonObject
                        val channelName = json["channelName"]?.jsonPrimitive?.content ?: return
                        val callerId = json["callerId"]?.jsonPrimitive?.content ?: return
                        val callerName = json["callerName"]?.jsonPrimitive?.content ?: "Someone"
                        val callerAvatar = json["callerAvatar"]?.jsonPrimitive?.content
                        val isVideo = json["isVideo"]?.jsonPrimitive?.content.toBoolean()

                        CallManager.setIncomingCall(IncomingCallData(channelName, callerId, callerName, callerAvatar, isVideo))
                        
                        sendLocalNotification(
                            if (isVideo) "Incoming Video Call" else "Incoming Audio Call",
                            "$callerName is calling you",
                            "call",
                            callerId,
                            callerId
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            override fun onClosed(eventSource: EventSource) {
                super.onClosed(eventSource)
                reconnectToStream(userId)
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                super.onFailure(eventSource, t, response)
                reconnectToStream(userId)
            }
        }
        eventSource = EventSources.createFactory(client).newEventSource(request, listener)
    }

    private fun reconnectToStream(userId: String) {
        scope.launch {
            delay(5000) // 5s backoff before reconnect
            connectToStream(userId)
        }
    }

    private fun sendLocalNotification(title: String, messageBody: String, type: String, chatId: String, actionId: String) {
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
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Mitron Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val (tag, notifId) = when (type) {
            "chat" -> NotificationHelper.TAG_PREFIX_CHAT + chatId to chatId.hashCode()
            "connection" -> NotificationHelper.TAG_CONNECTIONS to 1001
            "event" -> NotificationHelper.TAG_EVENTS to 1002
            "call" -> "call_alerts" to 1003
            else -> null to System.currentTimeMillis().toInt()
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))

        if (tag != null) {
            builder.setGroup(tag)
            notificationManager.notify(tag, notifId, builder.build())
        } else {
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        eventSource?.cancel()
        job.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
