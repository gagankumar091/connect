package com.mitron.connect.ui.screens.meetingsummary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mitron.connect.data.SampleData
import com.mitron.connect.ui.components.ConnectCard
import com.mitron.connect.ui.components.ConnectPrimaryButton
import com.mitron.connect.ui.components.ConnectSecondaryButton
import com.mitron.connect.ui.components.ConnectTopBar
import com.mitron.connect.ui.components.Tag
import com.mitron.connect.ui.components.TagStyle
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MeetingSummaryScreen(
    contactId: String,
    onBack: () -> Unit,
    viewModel: MeetingSummaryViewModel = viewModel()
) {
    val colors = ConnectTheme.colors
    
    androidx.compose.runtime.LaunchedEffect(contactId) {
        viewModel.loadSummary(contactId)
    }
    
    val summary by viewModel.summary.collectAsState()
    
    if (summary == null) {
        Scaffold(topBar = { ConnectTopBar(title = "AI meeting summary", onBack = onBack) }) { innerPadding ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(color = colors.accent)
            }
        }
        return
    }

    Scaffold(topBar = { ConnectTopBar(title = "AI meeting summary", onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(Spacing.md)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            ConnectCard {
                Column {
                    Text("Summary", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    Text(summary!!.summary, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                ConnectCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text("Pain points", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                        Tag(text = summary!!.painPoint, style = TagStyle.DANGER)
                    }
                }
                ConnectCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text("Budget", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                        Tag(text = summary!!.budget, style = TagStyle.SUCCESS)
                    }
                }
            }
            ConnectCard {
                Column {
                    Text("Action items", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    Text(summary!!.actionItems, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                ConnectPrimaryButton(text = "Accept")
                ConnectSecondaryButton(text = "Edit")
                ConnectSecondaryButton(text = "Share")
            }
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun MeetingSummaryScreenPreview() {
    com.mitron.connect.ui.theme.ConnectTheme {
        MeetingSummaryScreen(contactId = "preview", onBack = {})
    }
}
