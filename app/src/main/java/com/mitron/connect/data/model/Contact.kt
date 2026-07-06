package com.mitron.connect.data.model

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    var id: String = "",
    var initials: String = "",
    var name: String = "",
    var title: String? = null,
    var company: String? = null,
    
    @SerialName("avatar_url")
    @get:PropertyName("avatar_url") @set:PropertyName("avatar_url")
    var avatarUrl: String? = null,
    
    var email: String = "",
    var phone: String? = null,
    var linkedin: String? = null,
    var website: String? = null,
    var score: Int = 0,
    
    @SerialName("days_since_contact")
    @get:PropertyName("days_since_contact") @set:PropertyName("days_since_contact")
    var daysSinceContact: Int = 0,
    
    @SerialName("shared_interests")
    @get:PropertyName("shared_interests") @set:PropertyName("shared_interests")
    var sharedInterests: List<String> = emptyList(),
    
    @SerialName("mutuals_count")
    @get:PropertyName("mutuals_count") @set:PropertyName("mutuals_count")
    var mutualsCount: Int = 0,
    
    @SerialName("next_action")
    @get:PropertyName("next_action") @set:PropertyName("next_action")
    var nextAction: String? = null,
    
    var color: AccentColor = AccentColor.NEUTRAL,
    var distance: Double? = null,
)
