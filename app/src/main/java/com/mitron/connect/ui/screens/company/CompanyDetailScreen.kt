package com.mitron.connect.ui.screens.company

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitron.connect.data.model.Company
import com.mitron.connect.data.model.Contact
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.components.ConnectCard
import com.mitron.connect.ui.components.ConnectSecondaryButton
import com.mitron.connect.ui.components.ConnectTopBar
import com.mitron.connect.ui.theme.AvatarSize
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing
import com.mitron.connect.ui.theme.tints

@Composable
fun CompanyDetailScreen(
    companyId: String,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit = {},
    viewModel: CompanyViewModel = viewModel(),
) {
    val colors = ConnectTheme.colors
    
    androidx.compose.runtime.LaunchedEffect(companyId) {
        viewModel.loadCompany(companyId)
    }
    
    val company by viewModel.company.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val connectionStatuses by viewModel.connectionStatuses.collectAsState()
    
    if (company == null) {
        return
    }

    val (bg, fg) = company!!.color.tints(colors)

    Scaffold(topBar = { ConnectTopBar(title = company!!.name, onBack = onBack) }) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).padding(Spacing.md).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).background(bg, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Business, contentDescription = null, tint = fg)
                    }
                    Column(modifier = Modifier.padding(start = Spacing.xs)) {
                        Text(company!!.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Text(company!!.descriptor ?: "", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    StatCard(value = company!!.funding ?: "", label = "funding", modifier = Modifier.weight(1f))
                    StatCard(value = company!!.employees?.toString() ?: "0", label = "employees", modifier = Modifier.weight(1f))
                    StatCard(value = company!!.openDeals.toString(), label = "open deals", modifier = Modifier.weight(1f), valueColor = colors.success)
                }
            }

            item {
                Text("Contacts inside company", style = MaterialTheme.typography.bodySmall, color = colors.textMuted, modifier = Modifier.padding(top = Spacing.sm))
            }

            items(contacts) { contact ->
                CompanyContactRow(
                    contact = contact,
                    status = connectionStatuses[contact.id],
                    onConnect = { viewModel.connectWithContact(contact.id) },
                    onChat = { onOpenChat(contact.id) }
                )
            }

            item {
                ConnectCard {
                    Column {
                        Text("Recent news", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                        Text(company!!.recentNews ?: "", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanyContactRow(contact: Contact, status: String?, onConnect: () -> Unit, onChat: () -> Unit) {
    val colors = ConnectTheme.colors
    ConnectCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Avatar(initials = contact.initials ?: "", size = AvatarSize.small)
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                Text(contact.title ?: "", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
            if (status == "ACCEPTED") {
                ConnectSecondaryButton(
                    text = "Chat",
                    compact = true,
                    modifier = Modifier.width(80.dp),
                    onClick = onChat
                )
            } else if (status == "SENT") {
                ConnectSecondaryButton(
                    text = "Sent",
                    compact = true,
                    modifier = Modifier.width(80.dp),
                    onClick = { }
                )
            } else {
                ConnectSecondaryButton(
                    text = "Connect",
                    compact = true,
                    modifier = Modifier.width(80.dp),
                    onClick = onConnect
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = ConnectTheme.colors.textPrimary,
) {
    val colors = ConnectTheme.colors
    ConnectCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = valueColor)
            Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun CompanyDetailScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        CompanyDetailScreen(companyId = "preview", onBack = {}, onOpenChat = {})
    }
}
