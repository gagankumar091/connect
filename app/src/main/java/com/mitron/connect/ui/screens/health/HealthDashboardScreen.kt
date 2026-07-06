package com.mitron.connect.ui.screens.health

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
