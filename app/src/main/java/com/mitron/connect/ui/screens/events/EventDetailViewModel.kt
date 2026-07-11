package com.mitron.connect.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.VercelRepository
import com.mitron.connect.data.model.Contact
import com.mitron.connect.data.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventDetailViewModel(private val repository: VercelRepository = VercelRepository()) : ViewModel() {
    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _attendees = MutableStateFlow<List<Contact>>(emptyList())
    val attendees: StateFlow<List<Contact>> = _attendees.asStateFlow()

    private val _connectionStatuses = MutableStateFlow<Map<String, String>>(emptyMap())
    val connectionStatuses: StateFlow<Map<String, String>> = _connectionStatuses.asStateFlow()

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            // Prefer single-event fetch, fall back to list search
            val currentEvent = try {
                repository.getEventById(eventId)
            } catch (e: Exception) {
                repository.getEvents().find { it.id == eventId }
            }
            _event.value = currentEvent ?: return@launch

            // Load REAL attendees from the event's attendee_ids
            if (currentEvent.attendeeIds.isNotEmpty()) {
                val allUsers = repository.getContacts()
                val realAttendees = allUsers.filter { it.id in currentEvent.attendeeIds }
                _attendees.value = realAttendees
            } else {
                // No attendees registered yet
                _attendees.value = emptyList()
            }
        }
    }

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
}
