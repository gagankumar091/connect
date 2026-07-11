package com.mitron.connect.ui.screens.people

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitron.connect.data.model.Contact
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.components.ConnectSecondaryButton
import com.mitron.connect.ui.components.ShimmerEffect
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
    val colors = ConnectTheme.colors

    LazyColumn(
        modifier = modifier.fillMaxSize().background(colors.surface1),
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        item {
            if (hasLocationPermission) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Discover people near you", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                }
            }
        }

        if (contacts.isEmpty()) {
            items(5) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerEffect(modifier = Modifier.size(AvatarSize.medium + 4.dp).clip(CircleShape))
                    Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                        ShimmerEffect(modifier = Modifier.height(18.dp).fillMaxWidth(0.6f).clip(RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.height(8.dp))
                        ShimmerEffect(modifier = Modifier.height(14.dp).fillMaxWidth(0.8f).clip(RoundedCornerShape(4.dp)))
                    }
                    ShimmerEffect(modifier = Modifier.padding(start = Spacing.xs).height(32.dp).width(72.dp).clip(RoundedCornerShape(16.dp)))
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
                Divider(
                    color = colors.border,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 82.dp)
                )
            }
        }
    }
}

@Composable
private fun PersonRow(
    contact: Contact, 
    status: String?, 
    onClick: () -> Unit, 
    onConnect: () -> Unit, 
    onChat: () -> Unit
) {
    val colors = ConnectTheme.colors
    val (bg, fg) = contact.color.tints(colors)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            initials = contact.initials, 
            size = AvatarSize.medium + 4.dp, 
            background = bg, 
            foreground = fg
        )
        
        Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
            Text(
                text = contact.name, 
                style = MaterialTheme.typography.bodyLarge, 
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            val displayTitle = contact.title ?: "Professional"
            val displayCompany = contact.company ?: "Independent"
            Text(
                text = "$displayTitle, $displayCompany", 
                style = MaterialTheme.typography.bodyMedium, 
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))

        if (status == "ACCEPTED") {
            ConnectSecondaryButton(
                text = "Message",
                compact = true,
                modifier = Modifier.width(86.dp),
                onClick = onChat
            )
        } else if (status == "SENT") {
            Button(
                onClick = {}, 
                enabled = false,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(86.dp).height(32.dp),
                colors = ButtonDefaults.buttonColors(disabledContainerColor = colors.surface2, disabledContentColor = colors.textMuted)
            ) {
                Text("Pending", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        } else {
            Button(
                onClick = onConnect,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(86.dp).height(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.fillPrimary, contentColor = Color.White)
            ) {
                Text("Connect", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
