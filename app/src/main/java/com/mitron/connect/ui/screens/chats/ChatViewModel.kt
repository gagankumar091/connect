package com.mitron.connect.ui.screens.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.VercelRepository
import com.mitron.connect.data.model.ChatMessage
import com.mitron.connect.data.model.Contact
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: VercelRepository = VercelRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _contact = MutableStateFlow<Contact?>(null)
    val contact: StateFlow<Contact?> = _contact.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentChatId: String? = null
    private var pollingActive = false

    fun loadChat(chatId: String) {
        currentChatId = chatId
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val chats = repository.getChats()
                val chat = chats.find { it.id == chatId }
                if (chat != null) {
                    _contact.value = repository.getContactById(chat.contactId)
                }
                _messages.value = repository.getChatMessages(chatId)
                markAsRead(chatId)
                startPolling(chatId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startPolling(chatId: String) {
        if (pollingActive) return
        pollingActive = true
        viewModelScope.launch {
            while (true) {
                delay(2000)
                try {
                    val fresh = repository.getChatMessages(chatId)
                    _messages.value = fresh
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun markAsRead(chatId: String) {
        viewModelScope.launch {
            repository.markMessagesRead(chatId)
        }
    }

    fun sendMessage(chatId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                val optimisticMsg = ChatMessage(
                    id = "temp",
                    chatId = chatId,
                    text = text,
                    fromUser = true,
                    createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }.format(java.util.Date())
                )
                _messages.value = _messages.value + optimisticMsg

                val success = repository.sendMessage(chatId, text)
                if (success) {
                    _messages.value = repository.getChatMessages(chatId)
                } else {
                    _messages.value = _messages.value.filter { it.id != "temp" }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
