package com.mitron.connect.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MeetingSummary(
    var id: String = "",
    var summary: String = "",
    @SerialName("pain_point")
    var painPoint: String = "",
    var budget: String = "",
    @SerialName("action_items")
    var actionItems: String = "",
    @SerialName("contact_id")
    var contactId: String = ""
)
