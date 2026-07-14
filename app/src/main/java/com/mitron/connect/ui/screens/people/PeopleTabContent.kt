package com.mitron.connect.ui.screens.people

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitron.connect.data.model.Contact
import com.mitron.connect.data.model.RelationshipType
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.components.ConnectSecondaryButton
import com.mitron.connect.ui.components.ShimmerEffect
import com.mitron.connect.ui.theme.AvatarSize
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing
import com.mitron.connect.ui.theme.tints

private val PeopleBg = Color(0xFFF6F2FA)
private val PeopleSurface = Color(0xFFFBF8FF)
private val PeoplePrimary = Color(0xFF4343D5)
private val PeopleOnSurface = Color(0xFF1B1B20)
private val PeopleVariant = Color(0xFF464555)
private val PeopleOutline = Color(0xFF767586)
private val PeopleSurfLow = Color(0xFFEEEAF4)
private val PeopleDivider = Color(0xFFC7C4D7)

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
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredContacts by remember {
        derivedStateOf {
            if (searchQuery.isBlank()) contacts else contacts.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                (it.company?.contains(searchQuery, ignoreCase = true) == true) 
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(PeopleBg)) {
        // Header
        Surface(
            color = PeopleSurface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Network",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PeopleOnSurface
                    )
                    Box(
                        modifier = Modifier
                            .background(PeoplePrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${contacts.size} Connections",
                            color = PeoplePrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Premium Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(PeopleSurfLow)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = PeopleOutline,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text("Search connections...", color = PeopleOutline, fontSize = 15.sp)
                            }
                            androidx.compose.foundation.text.BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = PeopleOnSurface,
                                    fontSize = 15.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = PeopleOutline,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    }
                }
            }
        }
        
        HorizontalDivider(color = PeopleDivider.copy(alpha = 0.3f))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
        ) {
            if (hasLocationPermission) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Discover people near you", fontSize = 13.sp, color = PeopleOutline)
                    }
                }
            }

            if (filteredContacts.isEmpty() && searchQuery.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = PeopleOutline, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No results found.", color = PeopleVariant, fontSize = 15.sp)
                        }
                    }
                }
            } else if (contacts.isEmpty()) {
                items(5) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerEffect(modifier = Modifier.size(AvatarSize.medium + 4.dp).clip(CircleShape))
                        Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                            ShimmerEffect(modifier = Modifier.height(18.dp).fillMaxWidth(0.6f).clip(RoundedCornerShape(4.dp)))
                            Spacer(modifier = Modifier.height(8.dp))
                            ShimmerEffect(modifier = Modifier.height(14.dp).fillMaxWidth(0.8f).clip(RoundedCornerShape(4.dp)))
                        }
                        ShimmerEffect(modifier = Modifier.padding(start = 8.dp).height(32.dp).width(72.dp).clip(RoundedCornerShape(16.dp)))
                    }
                }
            } else {
                items(filteredContacts) { contact -> 
                    val contextBadgeText = if (contact.daysSinceContact > 30) {
                        "⚠️ Re-engage: No contact in ${contact.daysSinceContact} days"
                    } else {
                        "Last interacted ${contact.daysSinceContact} days ago"
                    }
                    
                    com.mitron.connect.ui.components.ProfessionalCard(
                        name = contact.name,
                        headline = "${contact.title ?: "Professional"} at ${contact.company ?: "Independent"}",
                        avatarUrl = contact.avatarUrl,
                        contextBadge = contextBadgeText,
                        score = contact.score,
                        onClick = { onOpenProfile(contact.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
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
                    SwipeToDismissBoxValue.EndToStart -> PeoplePrimary
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
                modifier = Modifier.fillMaxSize().background(color).padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .background(PeopleBg)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isHighValueProspect = contact.relationshipType == RelationshipType.PROSPECT && contact.score > 80
            val bgModifier = if (isHighValueProspect) {
                Modifier.background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(PeoplePrimary, Color(0xFF8B5CF6)) // Indigo to Violet
                    )
                )
            } else {
                Modifier.background(bg)
            }
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .then(bgModifier),
                contentAlignment = Alignment.Center
            ) {
                Text(contact.initials.take(2).uppercase(), color = if (isHighValueProspect) Color.White else fg, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                Text(
                    text = contact.name, 
                    fontSize = 15.sp,
                    color = PeopleOnSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val displayTitle = contact.title ?: "Professional"
                val displayCompany = contact.company ?: "Independent"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val badgeColor = when (contact.relationshipType) {
                        RelationshipType.PROSPECT -> PeoplePrimary
                        RelationshipType.CUSTOMER -> Color(0xFF10B981) // Green
                        RelationshipType.CANDIDATE -> Color(0xFFF59E0B) // Amber
                        RelationshipType.GENERAL -> PeopleOutline
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = contact.relationshipType.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$displayTitle, $displayCompany", 
                        fontSize = 12.sp,
                        color = PeopleOutline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))

            if (status == "ACCEPTED") {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PeoplePrimary.copy(alpha = 0.1f))
                        .clickable { onChat() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Message", color = PeoplePrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                val isPending = status == "SENT"
                val infiniteTransition = rememberInfiniteTransition()
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f, targetValue = if (isPending) 360f else 0f,
                    animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing)),
                    label = "loading"
                )
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isPending) PeopleSurfLow else PeoplePrimary)
                        .clickable(enabled = !isPending) { if (!isPending) onConnect() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isPending) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = PeopleOutline, modifier = Modifier.size(14.dp).rotate(rotation))
                            Spacer(Modifier.width(4.dp))
                            Text("Pending", color = PeopleOutline, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        } else {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Connect", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
