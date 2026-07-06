package com.mitron.connect.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class ChatPreview(
    val id: String,
    val contactId: String,
    val initials: String,
    val name: String,
    val company: String,
    val lastMessage: String,
    @SerialName("lastMessageTime")
    val lastMessageTime: String = "",
    val unreadCount: Int = 0,
    val scoreLabel: String,
    val color: AccentColor,
    val overdue: Boolean,
) {
    val formattedTime: String
        get() {
            if (lastMessageTime.isEmpty()) return ""
            return try {
                val date = try {
                    java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }.parse(lastMessageTime)
                } catch (e: Exception) {
                    java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }.parse(lastMessageTime)
                }
                if (date == null) return ""
                val now = Date()
                val diff = now.time - date.time
                val minutes = diff / 60000
                val hours = minutes / 60
                when {
                    minutes < 1 -> "now"
                    minutes < 60 -> "${minutes}m"
                    hours < 24 -> "${hours}h"
                    else -> java.text.SimpleDateFormat("dd MMM", java.util.Locale.US).format(date)
                }
            } catch (e: Exception) {
                ""
            }
        }
}
