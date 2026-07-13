package com.mitron.connect.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.VercelRepository
import com.mitron.connect.data.model.ChatPreview
import com.mitron.connect.data.model.Company
import com.mitron.connect.data.model.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmartSearchViewModel(private val repository: VercelRepository = VercelRepository()) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<Contact>>(emptyList())
    val results: StateFlow<List<Contact>> = _results.asStateFlow()

    private val _companyResults = MutableStateFlow<List<Company>>(emptyList())
    val companyResults: StateFlow<List<Company>> = _companyResults.asStateFlow()

    private val _chatResults = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chatResults: StateFlow<List<ChatPreview>> = _chatResults.asStateFlow()

    private var allContacts = listOf<Contact>()
    private var allCompanies = listOf<Company>()
    private var allChats = listOf<ChatPreview>()

    init {
        viewModelScope.launch {
            allContacts = repository.getContacts()
            allCompanies = try { repository.getCompanies() } catch (e: Exception) { emptyList() }
            _results.value = allContacts
            _companyResults.value = allCompanies
        }
        viewModelScope.launch {
            val chatDao = com.mitron.connect.data.local.MitronDatabase
                .getDatabase(com.mitron.connect.data.SessionManager.appContext)
                .chatDao()
            chatDao.getChatPreviewsFlow().collect { chats ->
                allChats = chats
                if (_query.value.isBlank()) {
                    _chatResults.value = allChats
                }
            }
        }
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isBlank()) {
            _results.value = allContacts
            _companyResults.value = allCompanies
            _chatResults.value = allChats
        } else {
            val q = newQuery.lowercase()
            _results.value = allContacts.filter {
                it.name.lowercase().contains(q) ||
                it.title?.lowercase()?.contains(q) == true ||
                it.company?.lowercase()?.contains(q) == true
            }
            _companyResults.value = allCompanies.filter {
                it.name.lowercase().contains(q) ||
                it.industry?.lowercase()?.contains(q) == true
            }
            _chatResults.value = allChats.filter {
                it.name.lowercase().contains(q) ||
                it.lastMessage.lowercase().contains(q)
            }
        }
    }
}
