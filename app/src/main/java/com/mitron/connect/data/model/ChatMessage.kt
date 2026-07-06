package com.mitron.connect.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class ChatMessage(
    var id: String = "",

    @SerialName("chat_id")
    var chatId: String = "",

    @SerialName("content")
    var text: String = "",

    @SerialName("from_user")
    var fromUser: Boolean = true,

    @SerialName("is_ai_labeled")
    var isAiLabeled: Boolean = false,

    @SerialName("created_at")
    var createdAt: String = "",

    @SerialName("is_read")
    var isRead: Int = 0
) {
    val parsedCreatedAt: Date
        get() = try {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.parse(createdAt) ?: Date()
        } catch (e: Exception) {
            try {
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }.parse(createdAt) ?: Date()
            } catch (e2: Exception) {
                Date()
            }
        }

    val formattedTime: String
        get() {
            val date = parsedCreatedAt
            val now = Date()
            val diff = now.time - date.time
            val minutes = diff / 60000
            val hours = minutes / 60
            return when {
                minutes < 1 -> "now"
                minutes < 60 -> "${minutes}m"
                hours < 24 -> "${hours}h"
                else -> java.text.SimpleDateFormat("dd MMM", java.util.Locale.US).format(date)
            }
        }
}
