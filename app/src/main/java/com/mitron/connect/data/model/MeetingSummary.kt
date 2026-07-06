package com.mitron.connect.data.model

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MeetingSummary(
    var id: String = "",
    var summary: String = "",
    @SerialName("pain_point")
    @get:PropertyName("pain_point") @set:PropertyName("pain_point")
    var painPoint: String = "",
    var budget: String = "",
    @SerialName("action_items")
    @get:PropertyName("action_items") @set:PropertyName("action_items")
    var actionItems: String = "",
    @SerialName("contact_id")
    @get:PropertyName("contact_id") @set:PropertyName("contact_id")
    var contactId: String = ""
)
