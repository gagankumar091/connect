package com.mitron.connect.ui.screens.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitron.connect.data.model.*
import com.mitron.connect.services.NotificationHelper
import com.mitron.connect.ui.components.ConnectCard
import com.mitron.connect.ui.components.ConnectTopBar
import com.mitron.connect.ui.platform.LocalContext
import com.mitron.connect.ui.screens.home.HomeViewModel
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenEvent: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val colors = ConnectTheme.colors
    val notifications by viewModel.notifications.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        NotificationHelper.cancelConnectionNotifications(context)
    }

    Scaffold(
        topBar = { ConnectTopBar(title = "Notifications", onBack = onBack) },
        containerColor = colors.surface1
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            if (notifications.isEmpty()) {
                item {
                    Text("No new notifications.", color = colors.textMuted, modifier = Modifier.padding(Spacing.md))
                }
            } else {
                items(notifications) { notification ->
                    NotificationRow(
                        notification = notification,
                        viewModel = viewModel,
                        onClick = {
                            val actionId = notification.action_id ?: ""
                            when (notification.type) {
                                NotificationType.CONNECTION_REQUEST -> onOpenProfile(actionId)
                                NotificationType.NEW_MESSAGE -> onOpenChat(actionId)
                                NotificationType.NEW_EVENT -> onOpenEvent(actionId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: AppNotification, viewModel: HomeViewModel, onClick: () -> Unit) {
    val colors = ConnectTheme.colors
    ConnectCard(modifier = Modifier.clickable { viewModel.markNotificationRead(notification.id); onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (notification.type) {
                NotificationType.CONNECTION_REQUEST -> Icons.Filled.PersonAdd
                NotificationType.NEW_MESSAGE -> Icons.Filled.Message
                NotificationType.NEW_EVENT -> Icons.Filled.Event
            }
            
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = colors.accentBg,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.padding(12.dp))
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.md)
            ) {
                Text(text = notification.title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Text(text = notification.description, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            
            if (notification.type == NotificationType.CONNECTION_REQUEST) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.padding(top = Spacing.sm)
                ) {
                    Button(
                        onClick = { viewModel.acceptConnectionRequest(notification) },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.pro),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text("Accept", color = colors.textOnColor)
                    }
                    OutlinedButton(
                        onClick = { viewModel.rejectConnectionRequest(notification) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.danger),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text("Reject", color = colors.danger)
                    }
                }
            }
        }
    }
}
