package com.mitron.connect.ui.screens.briefing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitron.connect.data.model.BriefingItem
import com.mitron.connect.ui.components.ConnectCard
import com.mitron.connect.ui.components.ConnectPrimaryButton
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing
import com.mitron.connect.ui.theme.tints
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyBriefingScreen(
    onContinue: () -> Unit,
    viewModel: BriefingViewModel = viewModel()
) {
    val colors = ConnectTheme.colors
    val items by viewModel.items.collectAsState()
    val completedIds by viewModel.completedIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val todayDate = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.US).format(Date())
    }
    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface1)
            .padding(Spacing.md),
    ) {
        Text(
            text = todayDate,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMuted,
        )
        Text(
            text = greeting,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
        )
        Text(
            text = "Here's your daily networking plan. Tap items you've completed.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = Spacing.md),
        )

        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = colors.fillPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    items(items) { item ->
                        BriefingRow(
                            item = item,
                            isCompleted = completedIds.contains(item.id),
                            onToggle = { viewModel.toggleCompleted(item.id) }
                        )
                    }
                }
            }
        }

        if (!isLoading) {
            Spacer(modifier = Modifier.height(Spacing.md))
            ConnectPrimaryButton(
                text = if (completedIds.size >= items.size && items.isNotEmpty()) "All done! Continue →"
                       else "Continue →",
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BriefingRow(item: BriefingItem, isCompleted: Boolean, onToggle: () -> Unit) {
    val colors = ConnectTheme.colors
    val (_, tint) = item.color.tints(colors)
    ConnectCard(
        modifier = Modifier.clickable(onClick = onToggle),
        containerColor = if (isCompleted) colors.surface1 else colors.surface2
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = if (isCompleted) "Completed" else "Mark complete",
                tint = if (isCompleted) colors.pro else colors.textMuted,
                modifier = Modifier.padding(end = Spacing.sm)
            )
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCompleted) colors.textMuted else colors.textPrimary
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun DailyBriefingScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        DailyBriefingScreen(onContinue = {})
    }
}
