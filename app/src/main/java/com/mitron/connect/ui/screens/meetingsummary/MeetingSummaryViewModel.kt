package com.mitron.connect.ui.screens.meetingsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.VercelRepository
import com.mitron.connect.data.model.MeetingSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MeetingSummaryViewModel(
    private val repository: VercelRepository = VercelRepository()
) : ViewModel() {

    private val _summary = MutableStateFlow<MeetingSummary?>(null)
    val summary: StateFlow<MeetingSummary?> = _summary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadSummary(contactId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _summary.value = repository.getMeetingSummary(contactId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
