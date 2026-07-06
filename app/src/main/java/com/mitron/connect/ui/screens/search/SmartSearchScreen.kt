package com.mitron.connect.ui.screens.search

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
