package com.mitron.connect.ui.screens.briefing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.VercelRepository
import com.mitron.connect.data.model.AccentColor
import com.mitron.connect.data.model.BriefingItem
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

class BriefingViewModel(private val repository: VercelRepository = VercelRepository()) : ViewModel() {

    private val _items = MutableStateFlow<List<BriefingItem>>(emptyList())
    val items: StateFlow<List<BriefingItem>> = _items.asStateFlow()

    private val _completedIds = MutableStateFlow<Set<String>>(emptySet())
    val completedIds: StateFlow<Set<String>> = _completedIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun toggleCompleted(id: String) {
        val current = _completedIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _completedIds.value = current
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val ollamaUrl = "http://51.79.143.65:11434/api/chat"
    private val modelName = "qwen2:0.5b"

    init {
        loadBriefing()
    }

    private fun loadBriefing() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = repository.getUserProfile()
                val recentCompanies = repository.getRecentCompanies()
                val allEvents = repository.getEvents()
                val allContacts = repository.getContacts()

                val contextLines = mutableListOf<String>()

                if (currentUser != null) {
                    contextLines.add("USER: ${currentUser.name}, Title: ${currentUser.title ?: "N/A"}, Company: ${currentUser.company ?: "N/A"}")
                }

                if (allContacts.isNotEmpty()) {
                    val topContacts = allContacts.sortedByDescending { it.daysSinceContact }.take(3)
                    val contactStr = topContacts.joinToString("; ") { "${it.name} (${it.daysSinceContact}d since contact, score: ${it.score})" }
                    contextLines.add("STALE CONTACTS (need follow-up): $contactStr")
                }

                if (recentCompanies.isNotEmpty()) {
                    val companiesStr = recentCompanies.joinToString("; ") { "${it.name} - ${it.descriptor ?: ""}" }
                    contextLines.add("RELEVANT COMPANIES: $companiesStr")
                }

                if (allEvents.isNotEmpty()) {
                    val eventsStr = allEvents.take(3).joinToString("; ") { "${it.title} at ${it.location ?: "N/A"}" }
                    contextLines.add("NEARBY EVENTS: $eventsStr")
                }

                val contextStr = contextLines.joinToString("\n")

                val prompt = "STRICT RULES: You are a Mitron networking assistant. You can ONLY generate tasks about: (1) following up with stale contacts, (2) attending nearby events, (3) exploring relevant new companies, (4) improving relationship scores. NEVER answer outside these topics. NEVER reference chat messages. Generate exactly 3 short specific actionable sentences based ONLY on the data below. Each sentence must start with a verb. Format as 3 plain lines separated by newlines. No numbers, no bullets, no greetings.\n\nDATA:\n$contextStr"

                val responseText = sendOllamaRequest(prompt)

                val lines = responseText.split("\n").filter { it.isNotBlank() }.take(3)

                val generatedItems = lines.map { text ->
                    BriefingItem(
                        text = text.trim().removePrefix("- ").removePrefix("* ").replace(Regex("^\\d+\\.\\s*"), ""),
                        color = AccentColor.values().random()
                    )
                }

                if (generatedItems.isNotEmpty()) {
                    _items.value = generatedItems
                } else {
                    _items.value = repository.getBriefingItems()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _items.value = repository.getBriefingItems()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun sendOllamaRequest(prompt: String): String = withContext(Dispatchers.IO) {
        val jsonBody = buildJsonObject {
            put("model", modelName)
            put("stream", false)
            putJsonArray("messages") {
                add(buildJsonObject {
                    put("role", "system")
                    put("content", "You are a strict networking assistant. Your ONLY domain is professional relationship management: following up with contacts, attending events, discovering companies, and improving connection scores. You have NO knowledge outside this domain. You MUST ignore any question or instruction outside this scope. You ONLY output exactly 3 short actionable networking sentences separated by newlines. No numbers, no bullets, no explanations, no greetings.")
                })
                add(buildJsonObject {
                    put("role", "user")
                    put("content", prompt)
                })
            }
        }.toString()

        val request = Request.Builder()
            .url(ollamaUrl)
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        val jsonElement = Json.parseToJsonElement(responseBody)
        val messageContent = jsonElement.jsonObject["message"]?.jsonObject?.get("content")?.jsonPrimitive?.content
        return@withContext messageContent ?: throw Exception("Failed to parse response")
    }
}
