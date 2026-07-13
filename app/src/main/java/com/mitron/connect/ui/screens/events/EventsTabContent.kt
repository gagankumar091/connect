package com.mitron.connect.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mitron.connect.data.model.Contact
import com.mitron.connect.ui.screens.home.HomeViewModel

// ── Design Tokens ──────────────────────────────────────────────────────────
private val EventBg       = Color(0xFFF6F2FA)
private val EventSurface  = Color(0xFFFBF8FF)
private val EventPrimary  = Color(0xFF4343D5)
private val EventOnSurf   = Color(0xFF1B1B20)
private val EventVariant  = Color(0xFF464555)
private val EventOutline  = Color(0xFF767586)
private val EventSurfLow  = Color(0xFFEEEAF4)
private val EventGreen    = Color(0xFF10B981)
private val EventDivider  = Color(0xFFC7C4D7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsTabContent(
    modifier: Modifier = Modifier,
    onOpenEvent: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val contacts by viewModel.contacts.collectAsState()
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val firstEvent = events.firstOrNull()

    val nearbyContacts = contacts
    val tabs = listOf(
        "Nearby (${nearbyContacts.size})",
        "Events (${events.size})",
        "Groups"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EventBg)
    ) {
        // ── Header ──────────────────────────────────────────────────────
        Surface(color = EventSurface, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(bottom = 4.dp)
            ) {
                // Hero section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(EventPrimary.copy(alpha = 0.08f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Events & People",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EventOnSurf
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                if (firstEvent != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            tint = EventOutline,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${firstEvent.title} · ${firstEvent.location ?: "Unknown location"}",
                                            fontSize = 13.sp,
                                            color = EventOutline,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "No active events nearby",
                                        fontSize = 13.sp,
                                        color = EventOutline
                                    )
                                }
                            }

                            if (events.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(EventPrimary.copy(alpha = 0.1f))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { onOpenEvent(firstEvent?.id ?: "") }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        "Change",
                                        color = EventPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Stats row
                        if (!isLoading && (contacts.isNotEmpty() || events.isNotEmpty())) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatBadge(label = "People", count = contacts.size, icon = Icons.Outlined.Groups)
                                StatBadge(label = "Events", count = events.size, icon = Icons.Outlined.CalendarMonth)
                            }
                        }
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = EventSurface,
                    contentColor = EventPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            height = 3.dp,
                            color = EventPrimary
                        )
                    },
                    divider = { HorizontalDivider(color = EventDivider.copy(alpha = 0.3f)) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == index) EventPrimary else EventOutline
                                )
                            }
                        )
                    }
                }
            }
        }

        // ── Content ──────────────────────────────────────────────────────
        if (isLoading && contacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EventPrimary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = 12.dp, bottom = 110.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                when (selectedTab) {
                    0 -> { // Nearby People
                        if (nearbyContacts.isEmpty()) {
                            item { EmptyEventsState("No people nearby", "Enable location to see people around you") }
                        } else {
                            itemsIndexed(nearbyContacts) { index, person ->
                                EventPersonRow(
                                    contact = person,
                                    onConnect = {}
                                )
                                if (index < nearbyContacts.size - 1) {
                                    HorizontalDivider(
                                        color = EventDivider.copy(alpha = 0.25f),
                                        modifier = Modifier.padding(start = 80.dp, end = 20.dp)
                                    )
                                }
                            }
                        }
                    }
                    1 -> { // Events
                        if (events.isEmpty()) {
                            item { EmptyEventsState("No events found", "Check back later for events near you") }
                        } else {
                            itemsIndexed(events) { index, event ->
                                EventCard(
                                    title = event.title,
                                    location = event.location ?: "Unknown location",
                                    date = event.date ?: "Date TBD",
                                    onClick = { onOpenEvent(event.id) }
                                )
                                if (index < events.size - 1) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                    2 -> { // Groups
                        item { EmptyEventsState("Groups coming soon", "Connect with more people to form groups") }
                    }
                }
            }
        }
    }
}

// ── Stat Badge ─────────────────────────────────────────────────────────────
@Composable
private fun StatBadge(label: String, count: Int, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(EventSurfLow)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = EventPrimary, modifier = Modifier.size(14.dp))
        Text(
            text = "$count $label",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = EventPrimary
        )
    }
}

// ── Person Row ─────────────────────────────────────────────────────────────
@Composable
fun EventPersonRow(contact: Contact, onConnect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(modifier = Modifier.size(52.dp)) {
            if (!contact.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = contact.avatarUrl,
                    contentDescription = contact.name,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(EventPrimary.copy(0.15f), EventPrimary.copy(0.08f))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        contact.initials.take(2).uppercase(),
                        color = EventPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = EventOnSurf,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = buildString {
                    if (!contact.title.isNullOrEmpty()) append(contact.title)
                    if (!contact.title.isNullOrEmpty() && !contact.company.isNullOrEmpty()) append(" · ")
                    if (!contact.company.isNullOrEmpty()) append(contact.company)
                    if (isEmpty()) append("Attendee")
                },
                fontSize = 12.sp,
                color = EventOutline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(EventPrimary)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onConnect
                )
                .padding(8.dp)
        ) {
            Icon(Icons.Filled.PersonAdd, contentDescription = "Connect", tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

// ── Event Card ─────────────────────────────────────────────────────────────
@Composable
private fun EventCard(title: String, location: String, date: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(EventSurface)
            .border(1.dp, EventDivider.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(EventPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = EventPrimary, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = EventOnSurf, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = EventOutline, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(location, fontSize = 12.sp, color = EventOutline, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(date, fontSize = 11.sp, color = EventGreen, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Empty State ────────────────────────────────────────────────────────────
@Composable
private fun EmptyEventsState(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = EventDivider, modifier = Modifier.size(52.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = EventVariant)
            Text(subtitle, fontSize = 13.sp, color = EventOutline)
        }
    }
}


