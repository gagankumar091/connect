package com.mitron.connect.ui.screens.people

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mitron.connect.data.model.Contact
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.components.ConnectCard
import com.mitron.connect.ui.components.ConnectSecondaryButton
import com.mitron.connect.ui.components.ShimmerEffect
import com.mitron.connect.ui.components.Tag
import com.mitron.connect.ui.components.TagStyle
import com.mitron.connect.ui.theme.AvatarSize
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing
import com.mitron.connect.ui.theme.tints

@Composable
fun PeopleTabContent(
    modifier: Modifier = Modifier,
    contacts: List<Contact>,
    connectionStatuses: Map<String, String>,
    hasLocationPermission: Boolean,
    onOpenProfile: (String) -> Unit,
    onConnect: (String) -> Unit,
    onOpenChat: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        item {
            if (hasLocationPermission) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(ConnectTheme.colors.surface2, RoundedCornerShape(8.dp)).padding(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("People", style = MaterialTheme.typography.bodySmall, color = ConnectTheme.colors.textPrimary)
                }
            } else {
                Text("People", style = MaterialTheme.typography.bodySmall, color = ConnectTheme.colors.textMuted, modifier = Modifier.padding(bottom = Spacing.sm))
            }
        }

        if (contacts.isEmpty()) {
            items(5) {
                ConnectCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ShimmerEffect(modifier = Modifier.size(AvatarSize.small).clip(CircleShape))
                        Column(modifier = Modifier.padding(start = Spacing.xs).fillMaxWidth().weight(1f)) {
                            ShimmerEffect(modifier = Modifier.height(20.dp).fillMaxWidth(0.6f).clip(RoundedCornerShape(4.dp)))
                            ShimmerEffect(modifier = Modifier.padding(top = 8.dp).height(16.dp).fillMaxWidth(0.8f).clip(RoundedCornerShape(4.dp)))
                        }
                        ShimmerEffect(modifier = Modifier.padding(start = Spacing.xs).height(32.dp).width(80.dp).clip(RoundedCornerShape(16.dp)))
                    }
                }
            }
        } else {
            items(contacts) { contact -> 
                PersonRow(
                    contact = contact, 
                    status = connectionStatuses[contact.id],
                    onClick = { onOpenProfile(contact.id) },
                    onConnect = { onConnect(contact.id) },
                    onChat = { onOpenChat(contact.id) }
                ) 
            }
        }
    }
}

@Composable
private fun PersonRow(contact: Contact, status: String?, onClick: () -> Unit, onConnect: () -> Unit, onChat: () -> Unit) {
    val colors = ConnectTheme.colors
    val (bg, fg) = contact.color.tints(colors)
    ConnectCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(initials = contact.initials, size = AvatarSize.small, background = bg, foreground = fg)
            Column(modifier = Modifier.padding(start = Spacing.xs).fillMaxWidth().weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(contact.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Tag(text = "score ${contact.score}", style = TagStyle.PRO)
                }
                Text("${contact.title}, ${contact.company}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
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
