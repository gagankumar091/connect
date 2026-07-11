package com.mitron.connect.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Company(
    var id: String = "",
    var name: String = "",
    var descriptor: String? = null,
    var funding: String? = null,
    var employees: Int? = null,
    
    @SerialName("avatar_url")
    var avatarUrl: String? = null,
    
    var website: String? = null,
    var description: String? = null,
    var industry: String? = null,
    var founded: String? = null,
    var headquarters: String? = null,
    
    @SerialName("employee_range")
    var employeeRange: String? = null,
    
    @SerialName("open_deals")
    var openDeals: Int? = null,
    
    @SerialName("contacts_inside")
    var contactsInside: String? = null,
    
    @SerialName("recent_news")
    var recentNews: String? = null,
    
    var color: AccentColor = AccentColor.NEUTRAL,
)
