package com.mitron.connect.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitron.connect.data.model.Contact
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.components.ConnectCard
import com.mitron.connect.ui.components.ConnectTopBar
import com.mitron.connect.ui.theme.AvatarSize
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun SmartSearchScreen(
    onBack: () -> Unit, 
    onOpenProfile: (String) -> Unit,
    viewModel: SmartSearchViewModel = viewModel()
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF8FAFC), // bg-slate-50
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                // Status bar padding area
                Spacer(modifier = Modifier.height(androidx.compose.foundation.layout.WindowInsets.statusBars.asPaddingValues().calculateTopPadding()))
                
                // Header Search Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(24.dp).padding(end = 8.dp)) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back", tint = Color.Black, modifier = Modifier.size(18.dp))
                    }
                    
                    TextField(
                        value = query,
                        onValueChange = { viewModel.updateQuery(it) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        placeholder = { Text("Search people, companies...", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp)) },
                        trailingIcon = { 
                            IconButton(onClick = {}) {
                                Icon(Icons.Filled.Tune, contentDescription = "Filter", tint = Color(0xFF4B5563), modifier = Modifier.size(20.dp)) 
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }
                
                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("People (${results.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), modifier = Modifier.padding(bottom = 12.dp))
                        Box(modifier = Modifier.height(2.dp).fillMaxWidth().background(Color(0xFF2563EB)))
                    }
                    Text("Companies", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF9CA3AF), modifier = Modifier.padding(bottom = 12.dp))
                    Text("Chats", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF9CA3AF), modifier = Modifier.padding(bottom = 12.dp))
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3F4F6)))
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavIcon(Icons.Filled.ChatBubbleOutline, "Chats", Color(0xFF9CA3AF), opacity = 0.4f)
                    BottomNavIcon(Icons.Outlined.Call, "Calls", Color(0xFF9CA3AF), opacity = 0.4f)
                    BottomNavIcon(Icons.Filled.Group, "Contacts", Color(0xFF2563EB), opacity = 1.0f)
                    BottomNavIcon(Icons.Outlined.Event, "Events", Color(0xFF9CA3AF), opacity = 0.4f)
                    BottomNavIcon(Icons.Outlined.FlashOn, "AI", Color(0xFF9CA3AF), opacity = 0.4f)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            items(results) { person -> 
                ResultRow(person, onClick = { onOpenProfile(person.id) }) 
            }
            
            if (results.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Show more results", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, opacity: Float = 1.0f) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {  }.alpha(opacity)) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = color, fontWeight = if (opacity == 1.0f) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun ResultRow(person: Contact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (!person.avatarUrl.isNullOrEmpty()) {
            AsyncImage(
                model = person.avatarUrl,
                contentDescription = person.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(CircleShape)
            )
        } else {
            Avatar(initials = person.initials, size = AvatarSize.medium)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            Text(person.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            
            val subtitle = buildString {
                if (!person.title.isNullOrEmpty()) append(person.title)
                if (!person.title.isNullOrEmpty() && !person.company.isNullOrEmpty()) append(" at ")
                if (!person.company.isNullOrEmpty()) append(person.company)
            }
            if (subtitle.isNotEmpty()) {
                Text(subtitle, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
            }
            
            
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF9FAFB)))
        }
    }
}
