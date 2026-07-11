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

    private val chatDao by lazy {
        com.mitron.connect.data.local.MitronDatabase.getDatabase(com.mitron.connect.data.SessionManager.appContext).chatDao()
    }

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

    private val _currentUser = MutableStateFlow<Contact?>(null)
    val currentUser: StateFlow<Contact?> = _currentUser.asStateFlow()

    var userLat: Double? = null
    var userLng: Double? = null

    // Tracks in-flight operations to prevent double-tap duplicate requests
    private val _pendingConnections = mutableSetOf<String>()
    private val _isAccepting = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            chatDao.getChatPreviewsFlow().collect { localChats ->
                _chats.value = localChats
            }
        }
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
            
            // Real-time polling for just Notifications, Reminders and Chats
            while (true) {
                delay(3000) // Poll every 3 seconds
                try {
                    val n = repository.getNotifications()
                    val r = repository.getReminders()
                    val ch = repository.getChats()
                    // Merge reminders into notifications list for the reminders screen
                    _notifications.value = (n + r).distinctBy { it.id }.sortedByDescending { it.isPastDue }
                    if (ch.isNotEmpty()) {
                        chatDao.insertChatPreviews(ch)
                    }
                    val myId = repository.getCurrentUserId()
                    if (myId != null) {
                        _currentUser.value = repository.getContactById(myId)
                    }
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
                    val myId = repository.getCurrentUserId()
                    
                    val fetchedChats = ch.await()
                    _contacts.value = c.await()
                    if (fetchedChats.isNotEmpty()) {
                        chatDao.insertChatPreviews(fetchedChats)
                    }
                    _companies.value = cp.await()
                    _events.value = ev.await()
                    _notifications.value = n.await()
                    if (myId != null) {
                        _currentUser.value = repository.getContactById(myId)
                    }
                    
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
                _errorMessage.value = "Network Error: Failed to sync with Connect Cloud. Retrying..."
            } finally {
                if (!isBackgroundSync) _isLoading.value = false
            }
        }
    }

    fun markNotificationRead(id: String) { viewModelScope.launch { repository.markNotificationRead(id); refreshData(true) } }

    fun connectWithContact(contactId: String) {
        // Guard: ignore if already sending a request to this contact
        if (!_pendingConnections.add(contactId)) return
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
            } finally {
                // Keep "SENT" state but allow re-try only on failure
                if (_connectionStatuses.value[contactId] != "SENT") {
                    _pendingConnections.remove(contactId)
                }
            }
        }
    }

    fun acceptConnectionRequest(notification: com.mitron.connect.data.model.AppNotification) {
        // Guard: ignore if already processing this notification
        if (!_isAccepting.add(notification.id)) return
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
            } finally {
                _isAccepting.remove(notification.id)
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
