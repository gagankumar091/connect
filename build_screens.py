import os

base_dir = "/home/randomx/Videos/mitron/app/src/main/java/com/mitron/connect/ui/screens"

smart_search_vm = """package com.mitron.connect.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.FirebaseRepository
import com.mitron.connect.data.model.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmartSearchViewModel(private val repository: FirebaseRepository = FirebaseRepository()) : ViewModel() {
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
                it.title.lowercase().contains(q) || 
                it.company.lowercase().contains(q)
            }
        }
    }
}
"""

smart_search_ui = """package com.mitron.connect.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitron.connect.data.model.Contact
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.components.ConnectCard
import com.mitron.connect.ui.components.ConnectTopBar
import com.mitron.connect.ui.theme.AvatarSize
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing

@Composable
fun SmartSearchScreen(
    onBack: () -> Unit, 
    onOpenProfile: (String) -> Unit,
    viewModel: SmartSearchViewModel = viewModel()
) {
    val colors = ConnectTheme.colors
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()

    Scaffold(topBar = { ConnectTopBar(title = "Live Search", onBack = onBack) }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(Spacing.md).fillMaxSize()) {
            TextField(
                value = query,
                onValueChange = { viewModel.updateQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name, title, or company...", color = colors.textMuted) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = colors.accent) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surface2,
                    unfocusedContainerColor = colors.surface2,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Text(
                "Results",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.xxs),
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                items(results) { person -> 
                    ResultRow(person, onClick = { onOpenProfile(person.id) }) 
                }
            }
        }
    }
}

@Composable
private fun ResultRow(person: Contact, onClick: () -> Unit) {
    val colors = ConnectTheme.colors
    ConnectCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(initials = person.initials, size = AvatarSize.small, background = colors.accentBg, foreground = colors.accent)
            Column(modifier = Modifier.padding(start = Spacing.xs)) {
                Text(person.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Text("${person.title} at ${person.company}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun SmartSearchScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        SmartSearchScreen(onBack = {}, onOpenProfile = {})
    }
}
"""


health_vm = """package com.mitron.connect.ui.screens.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.FirebaseRepository
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

class HealthViewModel(private val repository: FirebaseRepository = FirebaseRepository()) : ViewModel() {
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
    private val ollamaUrl = "http://51.79.143.65:11434/api/chat"

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
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
"""

health_ui = """package com.mitron.connect.ui.screens.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitron.connect.ui.components.ConnectCard
import com.mitron.connect.ui.components.ConnectTopBar
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing

@Composable
fun HealthDashboardScreen(
    onBack: () -> Unit,
    viewModel: HealthViewModel = viewModel()
) {
    val colors = ConnectTheme.colors
    
    val avgScore by viewModel.avgScore.collectAsState()
    val responseRate by viewModel.responseRate.collectAsState()
    val meetings by viewModel.meetings.collectAsState()
    val overdue by viewModel.overdue.collectAsState()
    val suggestedActions by viewModel.suggestedActions.collectAsState()

    Scaffold(topBar = { ConnectTopBar(title = "Relationship health", onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(Spacing.md).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                MetricCard(avgScore, "Avg score", colors.pro, Modifier.weight(1f))
                MetricCard(responseRate, "Response rate", colors.success, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                MetricCard(meetings, "Meetings", colors.textPrimary, Modifier.weight(1f))
                MetricCard(overdue, "Overdue chats", colors.danger, Modifier.weight(1f))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(colors.surface2, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.ShowChart, contentDescription = null, tint = colors.accent, modifier = Modifier.height(24.dp))
            }
            ConnectCard {
                Column {
                    Text("AI Suggested Action", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    Text(
                        suggestedActions,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(value: String, label: String, valueColor: Color, modifier: Modifier = Modifier) {
    val colors = ConnectTheme.colors
    ConnectCard(modifier = modifier) {
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            Text(value, style = MaterialTheme.typography.titleLarge, color = valueColor)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun HealthDashboardScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        HealthDashboardScreen(onBack = {})
    }
}
"""

with open(os.path.join(base_dir, "search", "SmartSearchViewModel.kt"), "w") as f:
    f.write(smart_search_vm)

with open(os.path.join(base_dir, "search", "SmartSearchScreen.kt"), "w") as f:
    f.write(smart_search_ui)

with open(os.path.join(base_dir, "health", "HealthViewModel.kt"), "w") as f:
    f.write(health_vm)

with open(os.path.join(base_dir, "health", "HealthDashboardScreen.kt"), "w") as f:
    f.write(health_ui)

print("Created ViewModels and updated screens for SmartSearch and HealthDashboard!")
