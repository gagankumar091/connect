package com.mitron.connect.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitron.connect.ui.components.ConnectButton
import com.mitron.connect.ui.components.ButtonVariant
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.animation.*
import androidx.compose.animation.core.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenProfile: (String) -> Unit,
    onOpenCompany: (String) -> Unit,
    onOpenEvent: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenSmartSearch: () -> Unit,
    onOpenBusinessCard: (String) -> Unit,
    onOpenHealth: () -> Unit,
    onOpenNotifications: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    onNavigateToChatTab: () -> Unit = {},
    onNavigateToEventsTab: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current
    var showCallSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { innerPadding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalItemSpacing = 16.dp,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Daily Briefing Card (Full Width)
            item(span = StaggeredGridItemSpan.FullLine) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Daily Briefing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Unread Messages")
                            TextButton(onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToChatTab() 
                            }) { Text("View All") }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Upcoming Events")
                            TextButton(onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToEventsTab() 
                            }) { Text("View All") }
                        }
                    }
                }
            }

            // Relationship Health (Half Width)
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.8f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Health", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Animated Radial Gauge
                        val progressAnim = remember { Animatable(0f) }
                        LaunchedEffect(Unit) {
                            progressAnim.animateTo(
                                targetValue = 0.75f,
                                animationSpec = spring(dampingRatio = 0.4f, stiffness = 100f)
                            )
                        }
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color.LightGray,
                                    startAngle = 135f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = Color(0xFF4F46E5),
                                    startAngle = 135f,
                                    sweepAngle = 270f * progressAnim.value,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Text("${(progressAnim.value * 100).toInt()}%", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Actions (Half Width)
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.8f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("Quick Actions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            IconButton(onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showCallSheet = true 
                            }, modifier = Modifier.background(Color(0xFFEEF2FF), androidx.compose.foundation.shape.CircleShape)) {
                                Icon(Icons.Default.Call, contentDescription = "Start Call", tint = Color(0xFF4F46E5))
                            }
                            IconButton(onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToChatTab() 
                            }, modifier = Modifier.background(Color(0xFFEEF2FF), androidx.compose.foundation.shape.CircleShape)) {
                                Icon(Icons.Default.Message, contentDescription = "Send Message", tint = Color(0xFF4F46E5))
                            }
                        }
                    }
                }
            }
            
            // Empty State Magic (Full Width)
            item(span = StaggeredGridItemSpan.FullLine) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.8f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pulsing button
                        val infiniteTransition = rememberInfiniteTransition()
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        Icon(Icons.Filled.Handshake, contentDescription = null, modifier = Modifier.size(64.dp).androidx.compose.ui.draw.scale(scale), tint = Color(0xFF10B981))
                        Spacer(Modifier.height(16.dp))
                        Text("Ready to grow your network?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {}, modifier = Modifier.androidx.compose.ui.draw.scale(scale)) {
                            Text("Invite Connections")
                        }
                    }
                }
            }
        }
        
        if (showCallSheet) {
            ModalBottomSheet(onDismissRequest = { showCallSheet = false }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Top Online Contacts", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Feature coming soon...") // Temporary stub
                }
            }
        }
    }
}
