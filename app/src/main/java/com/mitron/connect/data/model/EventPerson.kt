package com.mitron.connect.data.model

import kotlinx.serialization.Serializable

@Serializable
data class EventPerson(
    var id: String = "",
    var initials: String = "",
    var name: String = "",
    var roleLine: String = "",
    var color: AccentColor = AccentColor.NEUTRAL,
)
