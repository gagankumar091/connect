package com.mitron.connect.ui.screens.company

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mitron.connect.data.model.Company
import com.mitron.connect.data.model.Contact
import com.mitron.connect.ui.components.Avatar

@Composable
fun CompanyDetailScreen(
    companyId: String,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit = {},
    viewModel: CompanyViewModel = viewModel(),
) {
    androidx.compose.runtime.LaunchedEffect(companyId) {
        viewModel.loadCompany(companyId)
    }
    
    val company by viewModel.company.collectAsState()
    
    if (company == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF4F46E5))
        }
        return
    }
    val c = company!!

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavIcon(Icons.Outlined.ChatBubbleOutline, "Chats", Color(0xFF9CA3AF))
                    BottomNavIcon(Icons.Outlined.Call, "Calls", Color(0xFF9CA3AF))
                    BottomNavIcon(Icons.Outlined.Group, "Contacts", Color(0xFF4F46E5), isSelected = true)
                    BottomNavIcon(Icons.Outlined.Event, "Events", Color(0xFF9CA3AF))
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(24.dp).background(Color(0xFFE0E7FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("AI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("AI", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Header Section
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(androidx.compose.foundation.layout.WindowInsets.statusBars.asPaddingValues().calculateTopPadding()))
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back", tint = Color(0xFF111827))
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.MoreHoriz, contentDescription = "More", tint = Color(0xFF9CA3AF))
                    }
                }
                
                // Company Logo
                if (!c.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = c.avatarUrl,
                        contentDescription = c.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier.size(80.dp).background(Color.Black, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = c.name.take(3).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(c.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Verified", tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                }
                
                Text(c.website ?: "${c.name.lowercase().replace(" ", "")}.com", fontSize = 14.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(top = 4.dp))
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth().border(width = 1.dp, color = Color(0xFFF3F4F6), shape = RoundedCornerShape(0.dp)).padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Overview", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4F46E5), modifier = Modifier.padding(vertical = 16.dp))
                    Box(modifier = Modifier.height(2.dp).width(64.dp).background(Color(0xFF4F46E5)))
                }
                Text("People (12)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280), modifier = Modifier.padding(vertical = 16.dp))
                Text("Updates", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280), modifier = Modifier.padding(vertical = 16.dp))
                Text("Files", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280), modifier = Modifier.padding(vertical = 16.dp))
            }
            
            // Content Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text("ABOUT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF), letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = c.description ?: "Building innovative solutions for the future.",
                    fontSize = 15.sp,
                    color = Color(0xFF374151),
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Meta Info
                CompanyMetaRow(Icons.Outlined.Business, "Industry", c.industry ?: "Technology")
                Spacer(modifier = Modifier.height(24.dp))
                CompanyMetaRow(Icons.Outlined.CalendarToday, "Founded", c.founded ?: "2020")
                Spacer(modifier = Modifier.height(24.dp))
                CompanyMetaRow(Icons.Outlined.LocationOn, "Headquarters", c.headquarters ?: "Global")
                Spacer(modifier = Modifier.height(24.dp))
                CompanyMetaRow(Icons.Outlined.People, "Employees", c.employeeRange ?: "10-50")
                Spacer(modifier = Modifier.height(24.dp))
                CompanyMetaRow(Icons.Outlined.MonetizationOn, "Funding", c.funding ?: "Seed")
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4338CA)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("Connect with Team", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4338CA)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("Follow Company", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun CompanyMetaRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(36.dp).background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF4B5563), modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color(0xFF9CA3AF))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
        }
    }
}

@Composable
fun BottomNavIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, isSelected: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {  }) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = color, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun CompanyDetailScreenPreview() {
    CompanyDetailScreen(companyId = "comp-abc", onBack = {})
}
