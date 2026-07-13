package com.mitron.connect.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    var id: String = "",
    var initials: String = "",
    var name: String = "",
    var username: String? = null,
    var title: String? = null,
    var company: String? = null,
    var about: String? = null,
    
    @SerialName("avatar_url")
    var avatarUrl: String? = null,
    
    var email: String = "",
    var phone: String? = null,
    var linkedin: String? = null,
    var website: String? = null,
    var score: Int = 0,
    
    @SerialName("days_since_contact")
    var daysSinceContact: Int = 0,
    
    @SerialName("shared_interests")
    var sharedInterests: List<String> = emptyList(),
    
    @SerialName("mutuals_count")
    var mutualsCount: Int = 0,
    
    @SerialName("next_action")
    var nextAction: String? = null,
    
    var color: AccentColor = AccentColor.NEUTRAL,
    var distance: Double? = null,
)
