package com.mitron.connect.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
enum class TimelineIcon { LOCATION, QR, FILE, COFFEE, SEND, CHECK, EMAIL, CALENDAR, NOTE }

@Serializable
data class TimelineEvent(
    var id: String = "",
    
    @SerialName("contact_id")
    var contactId: String = "",
    
    @SerialName("content")
    var label: String = "",
    
    var icon: TimelineIcon = TimelineIcon.LOCATION,
    
    var color: AccentColor = AccentColor.NEUTRAL,
    
    @SerialName("is_meeting")
    var isMeeting: Boolean = false,

    @SerialName("subtitle")
    var subtitle: String? = null,
    
    @SerialName("created_at")
    var createdAtStr: String? = null
) {
    val createdAt: Date
        get() {
            if (createdAtStr == null) return Date()
            return try {
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(createdAtStr!!) ?: Date()
            } catch (e: Exception) {
                Date()
            }
        }
}
