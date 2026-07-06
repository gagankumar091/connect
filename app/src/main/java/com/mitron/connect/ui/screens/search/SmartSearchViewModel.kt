package com.mitron.connect.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.VercelRepository
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

    private var allContacts = listOf<Contact>()

    init {
        viewModelScope.launch {
            allContacts = repository.getContacts()
        }
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isBlank()) {
            _results.value = emptyList()
        } else {
            val q = newQuery.lowercase()
            _results.value = allContacts.filter { 
                it.name.lowercase().contains(q) || 
                it.title?.lowercase()?.contains(q) == true || 
                it.company?.lowercase()?.contains(q) == true
            }
        }
    }
}
