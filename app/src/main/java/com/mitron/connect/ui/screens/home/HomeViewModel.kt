package com.mitron.connect.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.VercelRepository
import com.mitron.connect.data.model.ChatPreview
import com.mitron.connect.data.model.Company
import com.mitron.connect.data.model.Contact
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

class HomeViewModel(private val repository: VercelRepository = VercelRepository()) : ViewModel() {

    private val _chats = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chats: StateFlow<List<ChatPreview>> = _chats.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _companies = MutableStateFlow<List<Company>>(emptyList())
    val companies: StateFlow<List<Company>> = _companies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _connectionStatuses = MutableStateFlow<Map<String, String>>(emptyMap())
    val connectionStatuses: StateFlow<Map<String, String>> = _connectionStatuses.asStateFlow()

    private val _events = MutableStateFlow<List<com.mitron.connect.data.model.Event>>(emptyList())
    val events: StateFlow<List<com.mitron.connect.data.model.Event>> = _events.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _notifications = MutableStateFlow<List<com.mitron.connect.data.model.AppNotification>>(emptyList())
    val notifications: StateFlow<List<com.mitron.connect.data.model.AppNotification>> = _notifications.asStateFlow()

    var userLat: Double? = null
    var userLng: Double? = null

    init {
        startRealTimePolling()
    }

    fun updateGpsLocation(lat: Double, lng: Double) {
        userLat = lat
        userLng = lng
        refreshData(true)
    }

    private fun startRealTimePolling() {
        viewModelScope.launch {
            // Initial full load
            refreshData(isBackgroundSync = false)
            
            // Real-time polling for just Notifications and Chats
            while (true) {
                delay(2000) // Poll every 2 seconds for snappy Real-Time feel
                try {
                    val n = repository.getNotifications()
                    val ch = repository.getChats()
                    _notifications.value = n
                    _chats.value = ch
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun refreshData(isBackgroundSync: Boolean = false) {
        viewModelScope.launch {
            if (!isBackgroundSync) _isLoading.value = true
            try {
                kotlinx.coroutines.coroutineScope {
                    val c = async { repository.getContacts(userLat, userLng) }
                    val ch = async { repository.getChats() }
                    val cp = async { repository.getCompanies() }
                    val ev = async { repository.getEvents(userLat, userLng) }
                    val n = async { repository.getNotifications() }
                    
                    val fetchedChats = ch.await()
                    _contacts.value = c.await()
                    _chats.value = fetchedChats
                    _companies.value = cp.await()
                    _events.value = ev.await()
                    _notifications.value = n.await()
                    
                    val newStatuses = _connectionStatuses.value.toMutableMap()
                    fetchedChats.forEach { chat ->
                        newStatuses[chat.contactId] = "ACCEPTED"
                    }
                    _connectionStatuses.value = newStatuses
                }
                if (userLat != null && userLng != null) repository.updateLocation(userLat!!, userLng!!)
                _errorMessage.value = null // Clear errors on success
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Network Error: Failed to sync with Mitron Cloud. Retrying..."
            } finally {
                if (!isBackgroundSync) _isLoading.value = false
            }
        }
    }

    fun markNotificationRead(id: String) { viewModelScope.launch { repository.markNotificationRead(id); refreshData(true) } }

    fun connectWithContact(contactId: String) {
        viewModelScope.launch {
            try {
                val success = repository.sendConnectionRequest(contactId)
                if (success) {
                    _connectionStatuses.value = _connectionStatuses.value.toMutableMap().apply {
                        put(contactId, "SENT")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun acceptConnectionRequest(notification: com.mitron.connect.data.model.AppNotification) {
        viewModelScope.launch {
            try {
                if (notification.action_id != null) {
                    val success = repository.acceptConnectionRequest(notification.action_id)
                    if (success) {
                        repository.markNotificationRead(notification.id)
                        refreshData(true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun rejectConnectionRequest(notification: com.mitron.connect.data.model.AppNotification) {
        viewModelScope.launch {
            try {
                if (notification.action_id != null) {
                    val success = repository.rejectConnectionRequest(notification.action_id)
                    if (success) {
                        repository.markNotificationRead(notification.id)
                        refreshData(true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
