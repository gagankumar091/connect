package com.mitron.connect.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.VercelRepository
import com.mitron.connect.data.model.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: VercelRepository = VercelRepository()
) : ViewModel() {

    private val _contact = MutableStateFlow<Contact?>(null)
    val contact: StateFlow<Contact?> = _contact.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isCurrentUser = MutableStateFlow(false)
    val isCurrentUser: StateFlow<Boolean> = _isCurrentUser.asStateFlow()

    private var currentContactId: String? = null
    private var pollingJob: kotlinx.coroutines.Job? = null

    fun loadContact(contactId: String) {
        currentContactId = contactId
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _contact.value = repository.getContactById(contactId)
                _isCurrentUser.value = repository.getCurrentUserId() == contactId
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
        startPolling(contactId)
    }

    fun loadCurrentUser() {
        currentContactId = "my_card"
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentId = repository.getCurrentUserId()
                if (currentId != null) {
                    _contact.value = repository.getContactById(currentId)
                    _isCurrentUser.value = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
        startPolling("my_card")
    }
    
    private fun startPolling(contactId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(2000)
                try {
                    if (contactId == "my_card") {
                        val currentId = repository.getCurrentUserId()
                        if (currentId != null) {
                            _contact.value = repository.getContactById(currentId)
                        }
                    } else {
                        _contact.value = repository.getContactById(contactId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
