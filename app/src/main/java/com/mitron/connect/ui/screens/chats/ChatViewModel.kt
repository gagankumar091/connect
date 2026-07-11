package com.mitron.connect.ui.screens.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.SessionManager
import com.mitron.connect.data.VercelRepository
import com.mitron.connect.data.model.ChatMessage
import com.mitron.connect.data.model.Contact
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class ChatViewModel(
    private val repository: VercelRepository = VercelRepository()
) : ViewModel() {

    private val chatDao by lazy {
        com.mitron.connect.data.local.MitronDatabase.getDatabase(SessionManager.appContext).chatDao()
    }

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _contact = MutableStateFlow<Contact?>(null)
    val contact: StateFlow<Contact?> = _contact.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _aiSummary = MutableStateFlow<List<String>?>(null)
    val aiSummary: StateFlow<List<String>?> = _aiSummary.asStateFlow()

    private var currentChatId: String? = null
    private var pollingJob: Job? = null

    fun loadChat(paramId: String) {
        // Stop any existing polling for a previous chat
        pollingJob?.cancel()

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Resolve whether paramId is a chatId or a contactId
                val chats = repository.getChats()
                val chat = chats.find { it.id == paramId || it.contactId == paramId }
                
                val resolvedChatId = chat?.id ?: paramId
                currentChatId = resolvedChatId
                
                // Start observing local DB for immediate display
                chatDao.getChatMessagesFlow(resolvedChatId).collect { localMsgs ->
                    _messages.value = localMsgs
                }
                
                // 2. Fetch contact info
                if (chat != null) {
                    val fullContact = repository.getContactById(chat.contactId)
                    if (fullContact != null) {
                        _contact.value = fullContact
                    } else {
                        // Fallback to basic info from chat preview
                        _contact.value = Contact(
                            id = chat.contactId,
                            name = chat.name,
                            initials = chat.initials,
                            company = chat.company,
                            color = chat.color,
                            score = 0,
                            daysSinceContact = 0,
                            mutualsCount = 0
                        )
                    }
                } else {
                    // Entirely new chat or we were just passed contactId
                    val fullContact = repository.getContactById(paramId)
                    if (fullContact != null) {
                        _contact.value = fullContact
                    }
                }

                // Sync messages from server into local DB
                syncMessagesFromServer(resolvedChatId)
                markAsRead(resolvedChatId)

                // Fetch actual AI Summary
                generateAiSummary(resolvedChatId, _messages.value)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }

            // 3. Start polling loop
            startPolling(currentChatId!!)
        }
    }

    /**
     * Fetches messages from the server, resolves fromUser correctly using
     * the current session's userId, then upserts into Room.
     * 
     * Key fix: We do NOT clear ALL messages first — we upsert (REPLACE conflict strategy
     * in ChatDao will update existing rows). We only remove trailing temp_ messages
     * after a confirmed server sync.
     */
    private suspend fun syncMessagesFromServer(chatId: String) {
        val currentUserId = SessionManager.getUserId() ?: return
        val remoteMsgs = repository.getChatMessages(chatId)
        if (remoteMsgs.isEmpty()) return

        // Ensure fromUser is correctly set using local userId because
        // the server sets from_user based on the userId query param which is correct,
        // but we also double-check with senderId for extra reliability.
        val correctedMsgs = remoteMsgs.map { msg ->
            val isFromMe = when {
                msg.senderId.isNotEmpty() -> msg.senderId == currentUserId
                else -> msg.fromUser // fall back to server-computed value
            }
            msg.copy(fromUser = isFromMe)
        }

        // Remove stale temp messages first, then insert real ones
        chatDao.clearTempMessages()
        chatDao.insertChatMessages(correctedMsgs)
    }

    private fun startPolling(chatId: String) {
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(3000) // Poll every 3 seconds
                try {
                    syncMessagesFromServer(chatId)
                    markAsRead(chatId)
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

    fun markAsRead(chatId: String) {
        viewModelScope.launch {
            try {
                repository.markMessagesRead(chatId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(chatId: String, text: String) {
        if (text.isBlank()) return
        val currentUserId = SessionManager.getUserId() ?: return

        viewModelScope.launch {
            // Optimistic insert — show message immediately on sender's screen
            val tempId = "temp_${System.currentTimeMillis()}"
            val optimisticMsg = ChatMessage(
                id = tempId,
                chatId = chatId,
                text = text,
                senderId = currentUserId,
                fromUser = true, // Always true for messages we send
                createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }.format(java.util.Date())
            )
            chatDao.insertChatMessages(listOf(optimisticMsg))

            try {
                val success = repository.sendMessage(chatId, text)
                if (success) {
                    // Sync real messages from server — this will replace the temp message
                    // with the real server-confirmed one (clearTempMessages is called inside syncMessagesFromServer)
                    syncMessagesFromServer(chatId)
                } else {
                    // Failed — remove optimistic message so UI shows nothing was sent
                    chatDao.clearTempMessages()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                chatDao.clearTempMessages()
            }
        }
    }

    private fun generateAiSummary(chatId: String, messages: List<ChatMessage>) {
        if (messages.isEmpty()) return
        
        viewModelScope.launch {
            try {
                val ollamaUrl = "http://51.79.143.65:11434/api/chat"
                val modelName = "qwen2:0.5b"
                val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()
                
                val chatLog = messages.takeLast(10).joinToString("\n") { 
                    if (it.fromUser) "You: ${it.text}" else "Them: ${it.text}"
                }
                
                val systemPrompt = "You are an AI assistant. Extract 3-4 short, concise bullet points summarizing the following chat conversation. Return ONLY the bullet points, one per line. Do not include any intro or outro text. Conversation:\n$chatLog"
                
                val jsonBody = buildJsonObject {
                    put("model", modelName)
                    put("stream", false)
                    putJsonArray("messages") {
                        add(buildJsonObject { put("role", "system"); put("content", systemPrompt) })
                    }
                }.toString()
                
                val request = Request.Builder().url(ollamaUrl).post(jsonBody.toRequestBody("application/json".toMediaType())).build()
                
                val responseText = withContext(Dispatchers.IO) {
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) return@withContext null
                    val responseBody = response.body?.string() ?: return@withContext null
                    val jsonElement = Json.parseToJsonElement(responseBody)
                    jsonElement.jsonObject["message"]?.jsonObject?.get("content")?.jsonPrimitive?.content
                }
                
                if (responseText != null) {
                    val summaryPoints = responseText.split("\n").map { it.trim().removePrefix("-").removePrefix("*").trim() }.filter { it.isNotBlank() }
                    if (summaryPoints.isNotEmpty()) {
                        _aiSummary.value = summaryPoints
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
