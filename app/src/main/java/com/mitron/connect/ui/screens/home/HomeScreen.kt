package com.mitron.connect.ui.screens.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.mitron.connect.navigation.HomeTab
import com.mitron.connect.ui.screens.aiassistant.AiAssistantScreen
import com.mitron.connect.ui.screens.people.PeopleTabContent
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing

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
) {
    var selectedTab by remember { mutableStateOf(HomeTab.CHATS) }
    val contacts by viewModel.contacts.collectAsState()
    val connectionStatuses by viewModel.connectionStatuses.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }
    val chats by viewModel.chats.collectAsState()
    val events by viewModel.events.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(selectedTab) {
        viewModel.refreshData()
    }

    LaunchedEffect(Unit) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            hasLocationPermission = true
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) viewModel.updateGpsLocation(loc.latitude, loc.longitude)
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val uri = Uri.parse(result.contents)
            if (uri.scheme == "mitron" && uri.host == "connect") {
                val contactId = uri.lastPathSegment
                if (contactId != null) {
                    viewModel.connectWithContact(contactId)
                    onOpenProfile(contactId)
                }
            }
        }
    }

    val colors = ConnectTheme.colors

    Scaffold(
        topBar = {
            if (selectedTab != HomeTab.CHATS) {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (selectedTab) {
                                    HomeTab.CONTACTS -> "People"
                                    HomeTab.AI -> "AI Assistant"
                                    else -> "Connect"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colors.surface1,
                            titleContentColor = colors.textPrimary,
                            actionIconContentColor = colors.textPrimary
                        ),
                    )
                    if (errorMessage != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.danger)
                                .padding(horizontal = Spacing.md, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                errorMessage!!,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.drawBehind { 
                    drawLine(Color(0xFFE4E1E7), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 1.dp.toPx()) 
                },
                containerColor = Color.White,
                tonalElevation = 8.dp,
            ) {
                NavigationBarItem(
                    selected = selectedTab == HomeTab.CHATS,
                    onClick = { selectedTab = HomeTab.CHATS },
                    icon = { Icon(if (selectedTab == HomeTab.CHATS) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline, contentDescription = "Chats") },
                    label = { Text("Chats", fontSize = 10.sp, fontWeight = if (selectedTab == HomeTab.CHATS) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF2563EB), // blue-600
                        selectedTextColor = Color(0xFF2563EB),
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = Color(0xFF9CA3AF), // gray-400
                        unselectedTextColor = Color(0xFF9CA3AF)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.CALLS,
                    onClick = { selectedTab = HomeTab.CALLS },
                    icon = { Icon(if (selectedTab == HomeTab.CALLS) Icons.Filled.Call else Icons.Outlined.Call, contentDescription = "Calls") },
                    label = { Text("Calls", fontSize = 10.sp, fontWeight = if (selectedTab == HomeTab.CALLS) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF2563EB),
                        selectedTextColor = Color(0xFF2563EB),
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = Color(0xFF9CA3AF),
                        unselectedTextColor = Color(0xFF9CA3AF)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.CONTACTS,
                    onClick = { selectedTab = HomeTab.CONTACTS },
                    icon = { Icon(if (selectedTab == HomeTab.CONTACTS) Icons.Filled.Group else Icons.Outlined.Group, contentDescription = "Contacts") },
                    label = { Text("Contacts", fontSize = 10.sp, fontWeight = if (selectedTab == HomeTab.CONTACTS) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF2563EB),
                        selectedTextColor = Color(0xFF2563EB),
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = Color(0xFF9CA3AF),
                        unselectedTextColor = Color(0xFF9CA3AF)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.EVENTS,
                    onClick = { selectedTab = HomeTab.EVENTS },
                    icon = { Icon(if (selectedTab == HomeTab.EVENTS) Icons.Filled.Event else Icons.Outlined.Event, contentDescription = "Events") },
                    label = { Text("Events", fontSize = 10.sp, fontWeight = if (selectedTab == HomeTab.EVENTS) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF2563EB),
                        selectedTextColor = Color(0xFF2563EB),
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = Color(0xFF9CA3AF),
                        unselectedTextColor = Color(0xFF9CA3AF)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.AI,
                    onClick = { selectedTab = HomeTab.AI },
                    icon = { Icon(if (selectedTab == HomeTab.AI) Icons.Filled.FlashOn else Icons.Outlined.FlashOn, contentDescription = "AI") },
                    label = { Text("AI", fontSize = 10.sp, fontWeight = if (selectedTab == HomeTab.AI) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF2563EB),
                        selectedTextColor = Color(0xFF2563EB),
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = Color(0xFF9CA3AF),
                        unselectedTextColor = Color(0xFF9CA3AF)
                    )
                )
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "tab_content"
            ) { tab ->
                when (tab) {
                    HomeTab.CHATS -> HomeFeedContent(
                        onOpenSearch = onOpenSmartSearch,
                        onOpenProfile = onOpenProfile,
                        onOpenChat = onOpenChat,
                        onOpenEvent = onOpenEvent,
                        onDraftFollowUp = { /* TODO */ },
                        onScheduleMeeting = { /* TODO */ },
                        onOpenNotifications = onOpenNotifications,
                        unreadCount = unreadCount,
                        chats = chats,
                        events = events,
                        currentUser = currentUser
                    )
                    HomeTab.CONTACTS -> PeopleTabContent(
                        contacts = contacts,
                        connectionStatuses = connectionStatuses,
                        hasLocationPermission = hasLocationPermission,
                        onOpenProfile = onOpenProfile,
                        onConnect = { id -> viewModel.connectWithContact(id) },
                        onOpenChat = onOpenChat
                    )
                    HomeTab.AI -> AiAssistantScreen()
                    else -> {}
                }
            }
        }
    }
}
