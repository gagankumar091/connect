package com.mitron.connect.navigation

object Destinations {
    const val WELCOME = "welcome"
    const val SIGN_IN = "signIn"
    const val SIGN_UP = "signUp"
    const val BRIEFING = "briefing"
    const val HOME = "home"
    const val PROFILE = "profile/{contactId}"
    const val TIMELINE = "timeline/{contactId}"
    const val MEETING_SUMMARY = "meetingSummary/{contactId}"
    const val SMART_SEARCH = "smartSearch"
    const val BUSINESS_CARD = "businessCard/{contactId}"
    const val COMPANY_DETAIL = "company/{companyId}"
    const val EVENT_DETAIL = "event/{eventId}"
    const val HEALTH_DASHBOARD = "health"
    const val CHAT = "chat/{chatId}"
    const val EDIT_PROFILE = "editProfile"
    const val NOTIFICATIONS = "notifications"
    const val RELATIONSHIP_SCORE = "relationshipScore/{contactId}"

    fun profile(contactId: String) = "profile/$contactId"
    fun timeline(contactId: String) = "timeline/$contactId"
    fun meetingSummary(contactId: String) = "meetingSummary/$contactId"
    fun businessCard(contactId: String) = "businessCard/$contactId"
    fun company(companyId: String) = "company/$companyId"
    fun event(eventId: String) = "event/$eventId"
    fun chat(chatId: String) = "chat/$chatId"
    fun relationshipScore(contactId: String) = "relationshipScore/$contactId"
}

enum class HomeTab { CHATS, CALLS, CONTACTS, EVENTS, AI }
