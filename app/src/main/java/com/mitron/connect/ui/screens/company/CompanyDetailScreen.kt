package com.mitron.connect.ui.screens.company

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.material.icons.outlined.OpenInNew
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mitron.connect.data.model.Company
import com.mitron.connect.ui.components.Avatar

// Premium Tokens
private val CompanyBg = Color(0xFFF6F2FA)
private val CompanySurface = Color(0xFFFBF8FF)
private val CompanyPrimary = Color(0xFF4343D5)
private val CompanyPrimaryContainer = Color(0xFF5D5FEF)
private val CompanyOnSurface = Color(0xFF1B1B20)
private val CompanyVariant = Color(0xFF464555)
private val CompanyOutline = Color(0xFF767586)
private val CompanySurfLow = Color(0xFFEEEAF4)
private val CompanyDivider = Color(0xFFC7C4D7)
private val SuccessColor = Color(0xFF10B981)

@Composable
fun CompanyDetailScreen(
    companyId: String,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit = {},
    viewModel: CompanyViewModel = viewModel(),
) {
    LaunchedEffect(companyId) {
        viewModel.loadCompany(companyId)
    }
    
    val company by viewModel.company.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    if (company == null) {
        Box(modifier = Modifier.fillMaxSize().background(CompanyBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CompanyPrimary)
        }
        return
    }
    val c = company!!

    Scaffold(
        containerColor = CompanyBg,
        topBar = {
            Surface(
                color = CompanyBg.copy(alpha = 0.8f),
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = CompanyOnSurface)
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {}
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = CompanyOnSurface)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(CompanyPrimary.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo
                    if (!c.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = c.avatarUrl,
                            contentDescription = c.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .shadow(8.dp, CircleShape)
                                .background(Brush.linearGradient(listOf(CompanyPrimary.copy(alpha=0.15f), CompanyPrimaryContainer.copy(alpha=0.08f))), CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = c.name.take(3).uppercase(),
                                color = CompanyPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(c.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CompanyOnSurface)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Verified", tint = SuccessColor, modifier = Modifier.size(20.dp))
                    }
                    
                    Row(
                        modifier = Modifier.padding(top = 6.dp).clickable { },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = c.website ?: "${c.name.lowercase().replace(" ", "")}.com",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = CompanyPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = CompanyPrimary, modifier = Modifier.size(12.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(28.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CompanyPrimary),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text("Connect Team", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 0.5.sp)
                        }
                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CompanySurfLow, contentColor = CompanyOnSurface),
                            shape = RoundedCornerShape(14.dp),
                            elevation = null
                        ) {
                            Text("Follow", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 0.5.sp)
                        }
                    }
                }
            }

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                val tabs = listOf("Overview", "People (${c.contactsInside ?: "0"})", "Updates")
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
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) CompanyPrimary else CompanyOutline,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        if (selectedTab == index) {
                            Box(modifier = Modifier.fillMaxWidth(0.8f).height(3.dp).clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)).background(CompanyPrimary))
                        } else {
                            Box(modifier = Modifier.fillMaxWidth(0.8f).height(3.dp).background(Color.Transparent))
                        }
                    }
                }
            }
            HorizontalDivider(color = CompanyDivider.copy(alpha = 0.3f))
            
            // Tab Content
            if (selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text("About", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CompanyOnSurface, modifier = Modifier.padding(bottom = 12.dp))
                    Text(
                        text = c.description ?: "No description available.",
                        fontSize = 15.sp,
                        color = CompanyVariant,
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Info Cards
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CompanyInfoRow(
                            icon = Icons.Outlined.PrecisionManufacturing,
                            label = "Industry",
                            value = c.industry ?: "Not specified"
                        )
                        CompanyInfoRow(
                            icon = Icons.Outlined.CalendarMonth,
                            label = "Founded",
                            value = c.founded ?: "Not specified"
                        )
                        CompanyInfoRow(
                            icon = Icons.Outlined.LocationOn,
                            label = "Headquarters",
                            value = c.headquarters ?: "Not specified"
                        )
                        CompanyInfoRow(
                            icon = Icons.Outlined.Group,
                            label = "Company Size",
                            value = c.employeeRange ?: c.employees?.toString() ?: "Not specified"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (selectedTab == 1) Icons.Outlined.Group else Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = CompanyOutline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Content for ${if(selectedTab == 1) "People" else "Updates"} will appear here.",
                            color = CompanyVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompanyInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CompanySurface)
            .border(1.dp, CompanyDivider.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(CompanyPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = CompanyPrimary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = CompanyOutline)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = CompanyOnSurface)
        }
    }
}
