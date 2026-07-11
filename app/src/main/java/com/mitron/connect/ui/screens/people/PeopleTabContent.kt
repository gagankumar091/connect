package com.mitron.connect.ui.screens.people

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.drawBehind
import com.mitron.connect.data.model.Contact
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.components.ConnectSecondaryButton
import com.mitron.connect.ui.components.ShimmerEffect
import com.mitron.connect.ui.theme.AvatarSize
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing
import com.mitron.connect.ui.theme.tints
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.drawBehind

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
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredContacts by remember {
        derivedStateOf {
            if (searchQuery.isBlank()) contacts else contacts.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                (it.company?.contains(searchQuery, ignoreCase = true) == true) 
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(colors.surface1)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search connections...") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = "Clear") }
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            contentPadding = PaddingValues(bottom = 80.dp)
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

        if (filteredContacts.isEmpty() && searchQuery.isNotEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No results found.", color = colors.textMuted)
                }
            }
        } else if (contacts.isEmpty()) {
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
            items(filteredContacts) { contact -> 
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val dismissState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF10B981)
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFF3B82F6)
                    else -> Color.Transparent
                }
            )
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Call
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Message
                else -> Icons.Default.Circle
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }
            Box(
                modifier = Modifier.fillMaxSize().background(color, RoundedCornerShape(12.dp)).padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .background(Color.White, RoundedCornerShape(12.dp))
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
        } else {
            val isPending = status == "SENT"
            val infiniteTransition = rememberInfiniteTransition()
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = if (isPending) 360f else 0f,
                animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing)),
                label = "loading"
            )
            
            Button(
                onClick = { if (!isPending) onConnect() }, 
                enabled = true,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(96.dp).height(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPending) colors.surface2 else colors.fillPrimary,
                    contentColor = if (isPending) colors.textMuted else Color.White
                )
            ) {
                if (isPending) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp).rotate(rotation))
                    Spacer(Modifier.width(4.dp))
                    Text("Pending", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                } else {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Connect", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
    }
}
