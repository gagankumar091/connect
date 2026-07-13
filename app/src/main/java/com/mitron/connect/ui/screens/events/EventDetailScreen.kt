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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.*
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

// Premium Tokens
private val EventBg = Color(0xFFF6F2FA)
private val EventSurface = Color(0xFFFBF8FF)
private val EventPrimary = Color(0xFF4343D5)
private val EventPrimaryContainer = Color(0xFF5D5FEF)
private val EventSecondary = Color(0xFF00CCF9)
private val EventOnSurface = Color(0xFF1B1B20)
private val EventVariant = Color(0xFF464555)
private val EventOutline = Color(0xFF767586)
private val EventSurfLow = Color(0xFFEEEAF4)
private val EventDivider = Color(0xFFC7C4D7)
private val SuccessColor = Color(0xFF10B981)

@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit = {},
    viewModel: EventDetailViewModel = viewModel()
) {
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }
    
    val event by viewModel.event.collectAsState()
    val attendees by viewModel.attendees.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }

    if (event == null) {
        Box(modifier = Modifier.fillMaxSize().background(EventBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = EventPrimary)
        }
        return
    }

    Scaffold(
        containerColor = EventBg,
        topBar = {
            Surface(
                color = EventBg.copy(alpha = 0.9f),
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onBack
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = EventOnSurface)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event!!.title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = EventOnSurface, lineHeight = 32.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(event!!.location ?: "Location Unknown", fontSize = 14.sp, color = EventOutline)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(EventPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onBack
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Change", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EventPrimary)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val tabs = listOf("Nearby (${attendees.size})", "People (${attendees.size})", "Groups")
                        tabs.forEachIndexed { index, title ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { selectedTab = index }
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 15.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == index) EventPrimary else EventOutline,
                                    modifier = Modifier.padding(bottom = 12.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (selectedTab == index) {
                                    Box(modifier = Modifier.fillMaxWidth(0.9f).height(3.dp).clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)).background(EventPrimary))
                                } else {
                                    Box(modifier = Modifier.fillMaxWidth(0.9f).height(3.dp).background(Color.Transparent))
                                }
                            }
                        }
                    }

                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            HorizontalDivider(color = EventDivider.copy(alpha = 0.3f))
            
            if (attendees.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No attendees yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EventVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Be the first to join this event!", fontSize = 14.sp, color = EventOutline)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 100.dp)
                ) {
                    itemsIndexed(attendees) { index, contact ->
                        EventAttendeeRow(
                            contact = contact,
                            index = index,
                            onClick = { onOpenProfile(contact.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventAttendeeRow(contact: Contact, index: Int, onClick: () -> Unit) {
    val gradientBrush = remember(index) {
        when (index % 4) {
            0 -> Brush.verticalGradient(listOf(EventPrimary, EventSecondary))
            1 -> Brush.verticalGradient(listOf(EventPrimaryContainer, EventPrimaryContainer))
            else -> Brush.verticalGradient(listOf(EventDivider, EventDivider))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = EventPrimary.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(16.dp))
            .background(EventSurface)
            .border(1.dp, EventDivider.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // Colored edge strip
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .align(Alignment.CenterVertically)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(gradientBrush))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(52.dp)) {
                if (!contact.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = contact.avatarUrl,
                        contentDescription = contact.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(EventPrimary.copy(alpha=0.15f), EventPrimaryContainer.copy(alpha=0.08f))), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(contact.initials.take(2).uppercase(), color = EventPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp)
                        .background(SuccessColor, CircleShape)
                        .border(2.dp, EventSurface, CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = EventOnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = buildString {
                        if (!contact.title.isNullOrEmpty()) append(contact.title)
                        if (!contact.title.isNullOrEmpty() && !contact.company.isNullOrEmpty()) append(" · ")
                        if (!contact.company.isNullOrEmpty()) append(contact.company)
                        if (isEmpty()) append("Mitron User")
                    }, 
                    fontSize = 13.sp, 
                    color = EventOutline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val distanceText = contact.distance?.let { "${it.toInt()}m" } ?: ""
                if (distanceText.isNotEmpty()) {
                    Text(distanceText, fontSize = 11.sp, color = EventPrimary, fontWeight = FontWeight.SemiBold)
                }
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(EventSurfLow, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.WavingHand, contentDescription = "Wave", tint = EventPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
