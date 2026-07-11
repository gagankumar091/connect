package com.mitron.connect.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class NotificationType {
    @SerialName("CONNECTION_REQUEST") CONNECTION_REQUEST,
    @SerialName("NEW_MESSAGE") NEW_MESSAGE,
    @SerialName("NEW_EVENT") NEW_EVENT,
    @SerialName("REMINDER") REMINDER
}

@Serializable
data class AppNotification(
    val id: String,
    val user_id: String,
    val title: String,
    val description: String? = null,
    val type: NotificationType = NotificationType.NEW_MESSAGE,
    val action_id: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("due_date")
    val dueDate: String? = null,
    @SerialName("is_past_due")
    val isPastDue: Boolean? = false,
    val is_read: Int = 0
) {
    val isRead: Boolean get() = is_read == 1
}
