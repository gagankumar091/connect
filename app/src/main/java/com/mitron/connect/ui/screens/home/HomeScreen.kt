package com.mitron.connect.ui.screens.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.mitron.connect.ui.screens.chats.ChatsTabContent
import com.mitron.connect.ui.screens.companies.CompaniesTabContent
import com.mitron.connect.ui.screens.events.EventsTabContent
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
    val chats by viewModel.chats.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val companies by viewModel.companies.collectAsState()
    val connectionStatuses by viewModel.connectionStatuses.collectAsState()
    val events by viewModel.events.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

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

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Connect",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onOpenNotifications) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(
                                            containerColor = ConnectTheme.colors.danger,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                if (unreadCount > 9) "9+" else "$unreadCount",
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Notifications,
                                    contentDescription = "Notifications",
                                    tint = if (unreadCount > 0) ConnectTheme.colors.accent
                                           else ConnectTheme.colors.textSecondary
                                )
                            }
                        }
                        IconButton(onClick = onOpenHealth) {
                            Icon(
                                Icons.Filled.BarChart,
                                contentDescription = "Relationship health",
                                tint = ConnectTheme.colors.textSecondary
                            )
                        }
                        IconButton(onClick = {
                            val options = ScanOptions()
                            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            options.setPrompt("Scan a profile QR code")
                            options.setBeepEnabled(false)
                            options.setOrientationLocked(true)
                            options.setCaptureActivity(CustomScannerActivity::class.java)
                            scanLauncher.launch(options)
                        }) {
                            Icon(
                                Icons.Filled.QrCodeScanner,
                                contentDescription = "Scan QR",
                                tint = ConnectTheme.colors.textSecondary
                            )
                        }
                        IconButton(onClick = { onOpenBusinessCard("my_card") }) {
                            Icon(
                                Icons.Filled.QrCode,
                                contentDescription = "My card",
                                tint = ConnectTheme.colors.textSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ConnectTheme.colors.surface1
                    )
                )
                if (errorMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        ConnectTheme.colors.danger,
                                        ConnectTheme.colors.danger.copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .padding(horizontal = Spacing.md, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Info,
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
        },
        bottomBar = {
            NavigationBar(
                containerColor = ConnectTheme.colors.surface1,
                tonalElevation = 0.dp,
                shadowElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == HomeTab.CHATS,
                    onClick = { selectedTab = HomeTab.CHATS },
                    icon = {
                        Icon(
                            if (selectedTab == HomeTab.CHATS) Icons.Filled.Mail else Icons.Filled.MailOutline,
                            contentDescription = "Chats"
                        )
                    },
                    label = { Text("Chats", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ConnectTheme.colors.accent,
                        selectedTextColor = ConnectTheme.colors.accent,
                        indicatorColor = ConnectTheme.colors.accentBg
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.PEOPLE,
                    onClick = { selectedTab = HomeTab.PEOPLE },
                    icon = {
                        Icon(
                            if (selectedTab == HomeTab.PEOPLE) Icons.Filled.Groups else Icons.Filled.Groups,
                            contentDescription = "People"
                        )
                    },
                    label = { Text("People", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ConnectTheme.colors.accent,
                        selectedTextColor = ConnectTheme.colors.accent,
                        indicatorColor = ConnectTheme.colors.accentBg
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.COMPANIES,
                    onClick = { selectedTab = HomeTab.COMPANIES },
                    icon = {
                        Icon(
                            if (selectedTab == HomeTab.COMPANIES) Icons.Filled.Business else Icons.Filled.Business,
                            contentDescription = "Companies"
                        )
                    },
                    label = { Text("Companies", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ConnectTheme.colors.accent,
                        selectedTextColor = ConnectTheme.colors.accent,
                        indicatorColor = ConnectTheme.colors.accentBg
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.EVENTS,
                    onClick = { selectedTab = HomeTab.EVENTS },
                    icon = {
                        Icon(
                            if (selectedTab == HomeTab.EVENTS) Icons.Filled.CalendarMonth else Icons.Filled.CalendarMonth,
                            contentDescription = "Events"
                        )
                    },
                    label = { Text("Events", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ConnectTheme.colors.accent,
                        selectedTextColor = ConnectTheme.colors.accent,
                        indicatorColor = ConnectTheme.colors.accentBg
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.AI,
                    onClick = { selectedTab = HomeTab.AI },
                    icon = {
                        Icon(
                            if (selectedTab == HomeTab.AI) Icons.Filled.AutoAwesome else Icons.Filled.AutoAwesome,
                            contentDescription = "AI"
                        )
                    },
                    label = { Text("AI", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ConnectTheme.colors.pro,
                        selectedTextColor = ConnectTheme.colors.pro,
                        indicatorColor = ConnectTheme.colors.proBg
                    )
                )
            }
        },
        containerColor = ConnectTheme.colors.surface1
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith
                    fadeOut(animationSpec = tween(200))
                },
                label = "tab_content"
            ) { tab ->
                when (tab) {
                    HomeTab.CHATS -> ChatsTabContent(
                        chats = chats,
                        onOpenChat = onOpenChat,
                        onOpenSearch = onOpenSmartSearch
                    )
                    HomeTab.PEOPLE -> PeopleTabContent(
                        contacts = contacts,
                        connectionStatuses = connectionStatuses,
                        hasLocationPermission = hasLocationPermission,
                        onOpenProfile = onOpenProfile,
                        onConnect = { id -> viewModel.connectWithContact(id) },
                        onOpenChat = onOpenChat
                    )
                    HomeTab.COMPANIES -> CompaniesTabContent(
                        companies = companies,
                        onOpenCompany = onOpenCompany
                    )
                    HomeTab.EVENTS -> EventsTabContent(
                        events = events,
                        contacts = contacts,
                        connectionStatuses = connectionStatuses,
                        hasLocationPermission = hasLocationPermission,
                        onOpenProfile = onOpenProfile,
                        onConnect = { id -> viewModel.connectWithContact(id) },
                        onOpenChat = onOpenChat,
                        onOpenEvent = onOpenEvent
                    )
                    HomeTab.AI -> AiAssistantScreen()
                }
            }
        }
    }
}
