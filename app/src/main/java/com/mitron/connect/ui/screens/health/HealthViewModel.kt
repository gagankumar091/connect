package com.mitron.connect.ui.screens.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.VercelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

class HealthViewModel(private val repository: VercelRepository = VercelRepository()) : ViewModel() {
    private val _avgScore = MutableStateFlow("0")
    val avgScore: StateFlow<String> = _avgScore.asStateFlow()

    private val _responseRate = MutableStateFlow("100%")
    val responseRate: StateFlow<String> = _responseRate.asStateFlow()

    private val _meetings = MutableStateFlow("0")
    val meetings: StateFlow<String> = _meetings.asStateFlow()

    private val _overdue = MutableStateFlow("0")
    val overdue: StateFlow<String> = _overdue.asStateFlow()

    private val _suggestedActions = MutableStateFlow("Analyzing your network health...")
    val suggestedActions: StateFlow<String> = _suggestedActions.asStateFlow()

    private val client = OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build()
    private val ollamaUrl = "${com.mitron.connect.BuildConfig.AI_BASE_URL}api/ai/chat"

    init {
        viewModelScope.launch {
            while (true) {
                loadData()
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    private suspend fun loadData() {
        try {
            val contacts = repository.getContacts()
            val chats = repository.getChats()
            
            val avg = if (contacts.isNotEmpty()) contacts.map { it.score }.average().toInt() else 0
            _avgScore.value = avg.toString()
            
            val overdueCount = chats.count { it.overdue }
            _overdue.value = overdueCount.toString()
            
            val contextStr = "Average relationship score is $avg out of 100. Overdue follow-up chats: $overdueCount."
            val prompt = "Based on this live relationship data: $contextStr. Write exactly one short, motivating sentence suggesting what the user should do next to improve their network."
            
            val aiResponse = sendOllamaRequest(prompt)
            _suggestedActions.value = aiResponse.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            _suggestedActions.value = "Connect with more people to generate insights."
        }
    }

    private suspend fun sendOllamaRequest(prompt: String): String = withContext(Dispatchers.IO) {
        val jsonBody = buildJsonObject {
            put("model", "qwen2:0.5b")
            put("stream", false)
            putJsonArray("messages") {
                add(buildJsonObject { put("role", "system"); put("content", "You are a concise networking assistant.") })
                add(buildJsonObject { put("role", "user"); put("content", prompt) })
            }
        }.toString()

        val request = Request.Builder().url(ollamaUrl).post(jsonBody.toRequestBody("application/json".toMediaType())).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
        
        val jsonElement = Json.parseToJsonElement(response.body?.string() ?: "")
        return@withContext jsonElement.jsonObject["message"]?.jsonObject?.get("content")?.jsonPrimitive?.content ?: ""
    }
}
