package com.mitron.connect.data.model

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Company(
    var id: String = "",
    var name: String = "",
    var descriptor: String? = null,
    var funding: String? = null,
    var employees: Int? = null,
    
    @SerialName("open_deals")
    @get:PropertyName("open_deals") @set:PropertyName("open_deals")
    var openDeals: Int? = null,
    
    @SerialName("contacts_inside")
    @get:PropertyName("contacts_inside") @set:PropertyName("contacts_inside")
    var contactsInside: String? = null,
    
    @SerialName("recent_news")
    @get:PropertyName("recent_news") @set:PropertyName("recent_news")
    var recentNews: String? = null,
    
    var color: AccentColor = AccentColor.NEUTRAL,
)
