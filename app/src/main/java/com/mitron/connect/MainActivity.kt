package com.mitron.connect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.mitron.connect.navigation.Destinations
import com.mitron.connect.services.NotificationHelper
import com.mitron.connect.ui.screens.home.HomeScreen
import com.mitron.connect.ui.screens.home.HomeViewModel
import com.mitron.connect.ui.screens.people.PeopleTabContent
import com.mitron.connect.ui.screens.events.EventsTabContent
import com.mitron.connect.ui.screens.chats.ChatsTabContent
import com.mitron.connect.ui.screens.profile.ProfileScreen
import com.mitron.connect.ui.screens.welcome.WelcomeScreen
import com.mitron.connect.ui.screens.welcome.SignInScreen
import com.mitron.connect.ui.screens.welcome.SignUpScreen
import com.mitron.connect.ui.theme.ConnectTheme
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    private val _pendingIntents = kotlinx.coroutines.flow.MutableSharedFlow<android.content.Intent>(extraBufferCapacity = 1)
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        _pendingIntents.tryEmit(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        com.mitron.connect.services.CallManager.initAgora(this)
        com.mitron.connect.data.SessionManager.init(this)
        
        // Handle cold start intent
        intent?.let { _pendingIntents.tryEmit(it) }

        if (com.mitron.connect.data.SessionManager.getUserId() != null) {
            val intent = android.content.Intent(this, com.mitron.connect.services.MitronSyncService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
        enableEdgeToEdge()
        setContent {
            ConnectTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                
                // Single shared ViewModel for all tabs — critical for real data
                val homeViewModel: HomeViewModel = viewModel()
                val chats by homeViewModel.chats.collectAsState()
                val contacts by homeViewModel.contacts.collectAsState()
                val events by homeViewModel.events.collectAsState()
                val connectionStatuses by homeViewModel.connectionStatuses.collectAsState()
                val currentUser by homeViewModel.currentUser.collectAsState()
                val errorMessage by homeViewModel.errorMessage.collectAsState()
                
                val isCallActive by com.mitron.connect.services.CallManager.isCallActive.collectAsState()

                val bottomNavItems = listOf(
                    Triple("chat_tab", "Chats", Icons.Filled.Chat),
                    Triple("contacts", "Network", Icons.Filled.Group),
                    Triple("ai_tab", "AI", Icons.Filled.AutoAwesome),
                    Triple("profile_tab", "Profile", Icons.Filled.AccountCircle)
                )

                val showBottomNav = currentRoute in bottomNavItems.map { it.first } && !isCallActive

                Scaffold(
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (showBottomNav) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 24.dp, vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .shadow(24.dp, RoundedCornerShape(40.dp), spotColor = Color(0xFF4343D5).copy(alpha = 0.15f))
                                        .clip(RoundedCornerShape(40.dp))
                                        .background(Color(0xFFFBF8FF).copy(alpha = 0.95f))
                                        .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(40.dp))
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    bottomNavItems.forEach { (route, label, icon) ->
                                        val isSelected = currentRoute == route
                                        
                                        val bgColor by animateColorAsState(
                                            targetValue = if (isSelected) Color(0xFF4343D5) else Color.Transparent,
                                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                                        )
                                        val contentColor by animateColorAsState(
                                            targetValue = if (isSelected) Color.White else Color(0xFF767586),
                                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                                        )
                                        
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(24.dp))
                                                .background(bgColor)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    navController.navigate(route) {
                                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                                .padding(horizontal = if (isSelected) 18.dp else 12.dp, vertical = 11.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                icon,
                                                contentDescription = label,
                                                tint = contentColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            androidx.compose.animation.AnimatedVisibility(visible = isSelected) {
                                                Text(
                                                    text = label,
                                                    color = contentColor,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    modifier = Modifier.padding(start = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navController, 
                            startDestination = if (com.mitron.connect.data.SessionManager.getUserId() != null) "chat_tab" else Destinations.WELCOME
                        ) {
                            // Bottom Nav Tabs
                            composable(Destinations.HOME) {
                                HomeScreen(
                                    onOpenProfile = { id -> navController.navigate(Destinations.profile(id)) },
                                    onOpenCompany = { id -> navController.navigate(Destinations.company(id)) },
                                    onOpenEvent = { id -> navController.navigate(Destinations.event(id)) },
                                    onOpenChat = { id -> navController.navigate(Destinations.chat(id)) },
                                    onOpenSmartSearch = { navController.navigate(Destinations.SMART_SEARCH) },
                                    onOpenBusinessCard = { id -> navController.navigate(Destinations.businessCard(id)) },
                                    onOpenHealth = { navController.navigate(Destinations.HEALTH_DASHBOARD) },
                                    onOpenNotifications = { navController.navigate(Destinations.NOTIFICATIONS) },
                                    onNavigateToChatTab = { navController.navigate("chat_tab") },
                                    onNavigateToEventsTab = { navController.navigate("events") },
                                    onNavigateToContacts = { navController.navigate("contacts") },
                                    viewModel = homeViewModel
                                )
                            }
                            composable("contacts") { PeopleTabContent(contacts = contacts, connectionStatuses = connectionStatuses, hasLocationPermission = false, onConnect = { homeViewModel.connectWithContact(it) }, onOpenChat = { navController.navigate(Destinations.chat(it)) }, onOpenProfile = { navController.navigate(Destinations.profile(it)) }) }
                            composable("events") { EventsTabContent(onOpenEvent = { navController.navigate(Destinations.event(it)) }) }
                            composable("chat_tab") { 
                                ChatsTabContent(
                                    chats = chats, 
                                    currentUser = currentUser, 
                                    onOpenSearch = { navController.navigate(Destinations.SMART_SEARCH) }, 
                                    onOpenChat = { navController.navigate(Destinations.chat(it)) }, 
                                    onOpenScanner = { navController.navigate(Destinations.SCANNER) },
                                    onOpenEvents = { navController.navigate("events") },
                                    onOpenNotifications = { navController.navigate(Destinations.NOTIFICATIONS) },
                                    onOpenProfile = { navController.navigate(Destinations.profile(it)) },
                                    notificationsCount = homeViewModel.notifications.collectAsState().value.count { !it.isRead }
                                ) 
                            }
                            composable("calls_tab") { 
                                com.mitron.connect.ui.screens.calls.CallsListScreen()
                            }
                            composable("ai_tab") { 
                                // AI Assistant disabled per user request - coming soon
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color(0xFFFBF8FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFF4343D5),
                                            modifier = Modifier.size(56.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("AI Assistant", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B1B20))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Coming Soon", fontSize = 14.sp, color = Color(0xFF767586))
                                    }
                                }
                            }
                            composable("my_profile") {
                                ProfileScreen(
                                    contactId = "my_card",
                                    onBack = { navController.popBackStack() },
                                    onOpenTimeline = { id -> navController.navigate(Destinations.timeline(id)) },
                                    onOpenBusinessCard = { id -> navController.navigate(Destinations.businessCard(id)) },
                                    onOpenRelationshipScore = { id -> navController.navigate(Destinations.relationshipScore(id)) },
                                    onOpenChat = { id -> navController.navigate(Destinations.chat(id)) },
                                    onEditProfile = { navController.navigate(Destinations.EDIT_PROFILE) },
                                    onLogout = {
                                        com.mitron.connect.data.SessionManager.clearSession()
                                        navController.navigate(Destinations.WELCOME) { popUpTo(0) { inclusive = true } }
                                    }
                                )
                            }

                            // Other Routes
                            composable(Destinations.WELCOME) {
                                WelcomeScreen(
                                    onGetStarted = { navController.navigate(Destinations.SIGN_UP) },
                                    onSignIn = { navController.navigate(Destinations.SIGN_IN) }
                                )
                            }
                            composable(Destinations.SIGN_IN) {
                                SignInScreen(
                                    onSignInSuccess = { navController.navigate("chat_tab") { popUpTo(Destinations.WELCOME) { inclusive = true } } },
                                    onBack = { navController.popBackStack() },
                                    onNavigateToSignUp = { navController.navigate(Destinations.SIGN_UP) { popUpTo(Destinations.SIGN_IN) { inclusive = true } } }
                                )
                            }
                            composable(Destinations.SIGN_UP) {
                                SignUpScreen(
                                    onSignUpSuccess = { navController.navigate("chat_tab") { popUpTo(Destinations.WELCOME) { inclusive = true } } },
                                    onBack = { navController.popBackStack() },
                                    onNavigateToLogin = { navController.navigate(Destinations.SIGN_IN) { popUpTo(Destinations.SIGN_UP) { inclusive = true } } }
                                )
                            }
                            composable(Destinations.SMART_SEARCH) {
                                com.mitron.connect.ui.screens.search.SmartSearchScreen(onBack = { navController.popBackStack() }, onOpenProfile = { id -> navController.navigate(Destinations.profile(id)) })
                            }
                            composable(Destinations.CHAT) { backStackEntry ->
                                val chatId = backStackEntry.arguments?.getString("chatId").orEmpty()
                                com.mitron.connect.ui.screens.chats.ChatScreen(chatId = chatId, onBack = { navController.popBackStack() })
                            }
                            composable("profile_tab") {
                                val currentUserId = currentUser?.id ?: ""
                                com.mitron.connect.ui.screens.profile.ProfileScreen(
                                    contactId = currentUserId,
                                    onBack = { navController.popBackStack() },
                                    onOpenTimeline = { id -> navController.navigate(Destinations.timeline(id)) },
                                    onOpenBusinessCard = { id -> navController.navigate(Destinations.businessCard(id)) },
                                    onOpenRelationshipScore = { id -> navController.navigate(Destinations.relationshipScore(id)) },
                                    onOpenChat = { id -> navController.navigate(Destinations.chat(id)) },
                                    onEditProfile = { navController.navigate(Destinations.EDIT_PROFILE) },
                                    onLogout = {
                                        com.mitron.connect.data.SessionManager.clearSession()
                                        navController.navigate(Destinations.WELCOME) { popUpTo(0) { inclusive = true } }
                                    }
                                )
                            }
                            composable(Destinations.EVENT_DETAIL) { backStackEntry ->
                                val eventId = backStackEntry.arguments?.getString("eventId").orEmpty()
                                com.mitron.connect.ui.screens.events.EventDetailScreen(eventId = eventId, onBack = { navController.popBackStack() }, onOpenProfile = { id -> navController.navigate(Destinations.profile(id)) })
                            }
                            composable(Destinations.COMPANY_DETAIL) { backStackEntry ->
                                val companyId = backStackEntry.arguments?.getString("companyId").orEmpty()
                                com.mitron.connect.ui.screens.company.CompanyDetailScreen(companyId = companyId, onBack = { navController.popBackStack() }, onOpenChat = { id -> navController.navigate(Destinations.chat(id)) })
                            }
                            composable(Destinations.TIMELINE) { backStackEntry ->
                                val contactId = backStackEntry.arguments?.getString("contactId").orEmpty()
                                com.mitron.connect.ui.screens.timeline.TimelineScreen(contactId = contactId, onBack = { navController.popBackStack() }, onOpenMeetingSummary = { id -> navController.navigate(Destinations.meetingSummary(id)) })
                            }
                            composable(Destinations.BUSINESS_CARD) { backStackEntry ->
                                val contactId = backStackEntry.arguments?.getString("contactId").orEmpty()
                                com.mitron.connect.ui.screens.businesscard.BusinessCardScreen(contactId = contactId, onBack = { navController.popBackStack() }, onEditProfile = { navController.navigate(Destinations.EDIT_PROFILE) }, onLogout = {
                                    com.mitron.connect.data.SessionManager.clearSession()
                                    navController.navigate(Destinations.WELCOME) { popUpTo(0) { inclusive = true } }
                                })
                            }
                            composable(Destinations.NOTIFICATIONS) {
                                com.mitron.connect.ui.screens.notifications.NotificationsScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenProfile = { id -> navController.navigate(Destinations.profile(id)) },
                                    onOpenChat = { id -> navController.navigate(Destinations.chat(id)) },
                                    onOpenEvent = { id -> navController.navigate(Destinations.event(id)) }
                                )
                            }
                            composable(Destinations.HEALTH_DASHBOARD) {
                                com.mitron.connect.ui.screens.health.HealthDashboardScreen(onBack = { navController.popBackStack() })
                            }
                            composable(Destinations.RELATIONSHIP_SCORE) { backStackEntry ->
                                val contactId = backStackEntry.arguments?.getString("contactId").orEmpty()
                                com.mitron.connect.ui.screens.score.RelationshipScoreScreen(contactId = contactId, onBack = { navController.popBackStack() })
                            }
                            composable(Destinations.EDIT_PROFILE) {
                                com.mitron.connect.ui.screens.profile.EditProfileScreen(onBack = { navController.popBackStack() })
                            }
                            composable(Destinations.SCANNER) {
                                com.mitron.connect.ui.screens.scanner.ScannerScreen(onBack = { navController.popBackStack() })
                            }
                            composable(Destinations.PROFILE) { backStackEntry ->
                                val contactId = backStackEntry.arguments?.getString("contactId").orEmpty()
                                ProfileScreen(
                                    contactId = contactId,
                                    onBack = { navController.popBackStack() },
                                    onOpenTimeline = { id -> navController.navigate(Destinations.timeline(id)) },
                                    onOpenBusinessCard = { id -> navController.navigate(Destinations.businessCard(id)) },
                                    onOpenRelationshipScore = { id -> navController.navigate(Destinations.relationshipScore(id)) },
                                    onOpenChat = { id -> navController.navigate(Destinations.chat(id)) },
                                    onEditProfile = { navController.navigate(Destinations.EDIT_PROFILE) },
                                    onLogout = {
                                        com.mitron.connect.data.SessionManager.clearSession()
                                        navController.navigate(Destinations.WELCOME) { popUpTo(0) { inclusive = true } }
                                    }
                                )
                            }
                        }
                        
                        // Floating Call UI
                        com.mitron.connect.ui.screens.calls.IncomingCallBanner()
                        com.mitron.connect.ui.screens.calls.ActiveCallScreen()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        NotificationHelper.cancelAllNotifications(this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        com.mitron.connect.services.CallManager.destroy()
    }
}
