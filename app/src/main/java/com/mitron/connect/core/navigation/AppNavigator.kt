package com.mitron.connect.core.navigation

/**
 * Abstracts navigation routing from the UI ViewModels and components.
 * Enables scalable multi-module feature navigation without deep coupling to NavHostController.
 */
interface AppNavigator {
    fun navigateToProfile(contactId: String)
    fun navigateToCompany(companyId: String)
    fun navigateToEvent(eventId: String)
    fun navigateToChat(chatId: String)
    fun navigateToTimeline(contactId: String)
    fun navigateToHome()
    fun goBack()
}
