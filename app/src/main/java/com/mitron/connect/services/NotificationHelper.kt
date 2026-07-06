package com.mitron.connect.services

import android.app.NotificationManager
import android.content.Context

object NotificationHelper {

    private const val GROUP_CONNECTIONS = "mitron_connections"
    private const val GROUP_MESSAGES = "mitron_messages"
    private const val GROUP_EVENTS = "mitron_events"
    const val TAG_PREFIX_CHAT = "chat_"
    const val TAG_CONNECTIONS = "connection_request"
    const val TAG_EVENTS = "event_notif"

    fun cancelAllNotifications(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancelAll()
    }

    fun cancelChatNotifications(context: Context, chatId: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(TAG_PREFIX_CHAT + chatId, 0)
    }

    fun cancelConnectionNotifications(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(TAG_CONNECTIONS, 0)
    }
}
