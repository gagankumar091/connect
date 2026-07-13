package com.mitron.connect.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mitron.connect.data.model.Contact

private val SearchBg = Color(0xFFF6F2FA)
private val SearchSurface = Color(0xFFFBF8FF)
private val SearchPrimary = Color(0xFF4343D5)
private val SearchPrimaryContainer = Color(0xFF5D5FEF)
private val SearchOnSurface = Color(0xFF1B1B20)
private val SearchVariant = Color(0xFF464555)
private val SearchOutline = Color(0xFF767586)
private val SearchSurfLow = Color(0xFFEEEAF4)
private val SearchDivider = Color(0xFFC7C4D7)
private val SuccessColor = Color(0xFF10B981)

@Composable
fun SmartSearchScreen(
    onBack: () -> Unit, 
    onOpenProfile: (String) -> Unit,
    viewModel: SmartSearchViewModel = viewModel()
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val companyCount by viewModel.companyResults.collectAsState()
    val chatCount by viewModel.chatResults.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = SearchBg,
        topBar = {
            Surface(
                color = SearchSurface,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = SearchOnSurface)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(SearchSurfLow)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Search, contentDescription = "Search", tint = SearchOutline, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (query.isEmpty()) {
                                        Text("Search network...", color = SearchOutline, fontSize = 15.sp)
                                    }
                                    BasicTextField(
                                        value = query,
                                        onValueChange = { viewModel.updateQuery(it) },
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(color = SearchOnSurface, fontSize = 15.sp),
                                        cursorBrush = SolidColor(SearchPrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(SearchPrimary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Tune, contentDescription = "Filter", tint = SearchPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            item {
                // AI Insight Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(SearchPrimary.copy(alpha = 0.08f), SearchPrimaryContainer.copy(alpha = 0.02f))
                            )
                        )
                        .border(1.dp, SearchPrimary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(SearchPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = SearchPrimary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = androidx.compose.ui.text.buildAnnotatedString {
                                withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = SearchPrimary)) {
                                    append("AI Insight: ")
                                }
                                append("Found ${results.size} people across your network who recently updated their profiles related to your search.")
                            },
                            fontSize = 13.sp,
                            color = SearchOnSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    val tabs = listOf("People (${results.size})", "Companies (${companyCount.size})", "Chats (${chatCount.size})")
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
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) SearchPrimary else SearchOutline,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            if (selectedTab == index) {
                                Box(modifier = Modifier.fillMaxWidth(0.8f).height(3.dp).clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)).background(SearchPrimary))
                            } else {
                                Box(modifier = Modifier.fillMaxWidth(0.8f).height(3.dp).background(Color.Transparent))
                            }
                        }
                    }
                }
                
                HorizontalDivider(color = SearchDivider.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(results) { person -> 
                ResultRow(person, onClick = { onOpenProfile(person.id) }) 
            }
            
            if (results.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { }
                            .background(SearchSurfLow)
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Show more results", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SearchPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(person: Contact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(52.dp)) {
            if (!person.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = person.avatarUrl,
                    contentDescription = person.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(SearchPrimary.copy(alpha=0.15f), SearchPrimaryContainer.copy(alpha=0.08f))), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(person.initials.take(2).uppercase(), color = SearchPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            // Online/Status Indicator
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(14.dp)
                    .background(SuccessColor, CircleShape)
                    .border(2.dp, SearchBg, CircleShape)
            )
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = person.name, 
                fontSize = 15.sp, 
                fontWeight = FontWeight.SemiBold, 
                color = SearchOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            val title = person.title ?: "Professional"
            val company = person.company ?: "Independent"
            Text(
                text = "$title at $company", 
                fontSize = 12.sp, 
                color = SearchOutline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Icon(Icons.Outlined.Event, contentDescription = "Meet", tint = SearchPrimary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
    }
}
