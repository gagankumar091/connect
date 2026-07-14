package com.mitron.connect.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BriefingItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String = "",
    val color: AccentColor = AccentColor.NEUTRAL,
    val contactName: String? = null,
    val meetingTime: String? = null,
    val lastContextSummary: String? = null
)
