package com.mitron.connect.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class NotificationType { CONNECTION_REQUEST, NEW_MESSAGE, NEW_EVENT }

@Serializable
data class AppNotification(
    val id: String,
    val user_id: String,
    val title: String,
    val description: String,
    val type: NotificationType,
    val action_id: String? = null,
    val is_read: Int = 0
) {
    val isRead: Boolean get() = is_read == 1
}
