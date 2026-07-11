package com.mitron.connect.ui.screens.company

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.VercelRepository
import com.mitron.connect.data.model.Company
import com.mitron.connect.data.model.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CompanyViewModel(
    private val repository: VercelRepository = VercelRepository()
) : ViewModel() {

    private val _company = MutableStateFlow<Company?>(null)
    val company: StateFlow<Company?> = _company.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _connectionStatuses = MutableStateFlow<Map<String, String>>(emptyMap())
    val connectionStatuses: StateFlow<Map<String, String>> = _connectionStatuses.asStateFlow()

    fun loadCompany(companyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Use direct endpoint — fetches single company row, much faster
                _company.value = repository.getCompanyByIdDirect(companyId)
                _contacts.value = repository.getContactsByCompany(companyId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
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
