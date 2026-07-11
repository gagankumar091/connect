package com.mitron.connect.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Event(
    var id: String = "",
    var title: String = "",
    var location: String? = null,
    var description: String? = null,
    var date: String? = null,
    var distance: Double? = null,
    @SerialName("attendee_ids")
    var attendeeIds: List<String> = emptyList()
)
