package com.mitron.connect.data.model

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
enum class TimelineIcon { LOCATION, QR, FILE, COFFEE, SEND, CHECK }

@Serializable
data class TimelineEvent(
    var id: String = "",
    
    @SerialName("contact_id")
    @get:PropertyName("contact_id") @set:PropertyName("contact_id")
    var contactId: String = "",
    
    @SerialName("content")
    @get:PropertyName("content") @set:PropertyName("content")
    var label: String = "",
    
    var icon: TimelineIcon = TimelineIcon.LOCATION,
    
    var color: AccentColor = AccentColor.NEUTRAL,
    
    @SerialName("is_meeting")
    @get:PropertyName("is_meeting") @set:PropertyName("is_meeting")
    var isMeeting: Boolean = false,
    
    @get:PropertyName("created_at") @set:PropertyName("created_at")
    @kotlinx.serialization.Transient
    var createdAt: Date = Date()
)
