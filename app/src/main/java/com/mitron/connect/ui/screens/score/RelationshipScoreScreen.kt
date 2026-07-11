package com.mitron.connect.ui.screens.score

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mitron.connect.data.model.Contact
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.screens.profile.ProfileViewModel
import com.mitron.connect.ui.theme.AvatarSize

@Composable
fun RelationshipScoreScreen(
    contactId: String,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    LaunchedEffect(contactId) {
        viewModel.loadContact(contactId)
    }

    val contact by viewModel.contact.collectAsState()

    if (contact == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF6366F1))
        }
        return
    }

    val c = contact!!

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            Column(modifier = Modifier.background(Color(0xFFF9FAFB))) {
                Spacer(modifier = Modifier.height(androidx.compose.foundation.layout.WindowInsets.statusBars.asPaddingValues().calculateTopPadding()))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back", tint = Color(0xFF4B5563), modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = "AI Relationship Score",
                        modifier = Modifier.weight(1f).padding(end = 40.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF111827)
                    )
                }
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
                    BottomNavIcon(Icons.Outlined.ChatBubbleOutline, "Chats", Color(0xFF9CA3AF))
                    BottomNavIcon(Icons.Outlined.Call, "Calls", Color(0xFF9CA3AF))
                    
                    // Discover FAB style
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                        ) {
                            Icon(Icons.Outlined.RemoveRedEye, contentDescription = "Discover", tint = Color(0xFF4F46E5), modifier = Modifier.size(24.dp))
                            Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp).size(8.dp).background(Color(0xFF4F46E5), CircleShape).border(2.dp, Color.White, CircleShape))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Discover", fontSize = 10.sp, color = Color(0xFF4F46E5), fontWeight = FontWeight.Bold)
                    }
                    
                    BottomNavIcon(Icons.Outlined.Event, "Events", Color(0xFF9CA3AF))
                    BottomNavIcon(Icons.Outlined.AutoAwesome, "AI", Color(0xFF9CA3AF))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Profile Header
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(56.dp)) {
                    if (!c.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = c.avatarUrl,
                            contentDescription = c.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Avatar(initials = c.initials, size = AvatarSize.medium, modifier = Modifier.fillMaxSize())
                    }
                    Box(modifier = Modifier.align(Alignment.BottomEnd).size(14.dp).background(Color(0xFF22C55E), CircleShape).border(2.dp, Color.White, CircleShape))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(c.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF111827))
                    Text(c.company ?: "Company Unknown", fontSize = 14.sp, color = Color(0xFF6B7280))
                }
            }

            // Score Section
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("RELATIONSHIP SCORE", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151), letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Circular Chart
                        Box(modifier = Modifier.size(128.dp), contentAlignment = Alignment.Center) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color(0xFFF3F4F6),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = Color(0xFF6366F1),
                                    startAngle = -90f,
                                    sweepAngle = (c.score / 100f) * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(c.score.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Text("/100", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF9CA3AF))
                            }
                        }

                        // Stats
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text("Last spoke", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                                Text("${c.daysSinceContact} days ago", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            }
                            Column {
                                Text("Strength", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF22C55E), CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when {
                                            c.score >= 80 -> "Strong"
                                            c.score >= 50 -> "Good"
                                            else -> "Weak"
                                        }, 
                                        fontSize = 14.sp, 
                                        fontWeight = FontWeight.Bold, 
                                        color = Color(0xFF111827)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Suggested Actions
            Text("SUGGESTED ACTION", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151), letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionItem("Send festival wishes", Icons.Outlined.Send, Color(0xFFEEF2FF), Color(0xFF4F46E5))
                ActionItem("Share relevant article", Icons.Outlined.FileCopy, Color(0xFFEFF6FF), Color(0xFF2563EB))
                ActionItem("Schedule follow up", Icons.Outlined.CalendarToday, Color(0xFFF5F3FF), Color(0xFF7C3AED))
            }

            Spacer(modifier = Modifier.weight(1f))
            
            // Footer CTA
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4F46E5)),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("View Full Insights", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ActionItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bgColor: Color, iconColor: Color) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().clickable { }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(bgColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color(0xFF1F2937))
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = "Proceed", tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun BottomNavIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {  }) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = color, fontWeight = FontWeight.Medium)
    }
}
