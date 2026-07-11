package com.mitron.connect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.mitron.connect.navigation.Destinations
import com.mitron.connect.services.NotificationHelper
import com.mitron.connect.ui.screens.home.HomeScreen
import com.mitron.connect.ui.screens.people.PeopleTabContent
import com.mitron.connect.ui.screens.events.EventsTabContent
import com.mitron.connect.ui.screens.chats.ChatsTabContent
import com.mitron.connect.ui.screens.profile.ProfileScreen
import com.mitron.connect.ui.screens.welcome.WelcomeScreen
import com.mitron.connect.ui.screens.welcome.SignInScreen
import com.mitron.connect.ui.screens.welcome.SignUpScreen
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.ConnectAppTheme

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
                
                val bottomNavItems = listOf(
                    Triple(Destinations.HOME, "Home", Icons.Default.Home),
                    Triple("contacts", "Contacts", Icons.Default.People),
                    Triple("events", "Events", Icons.Default.Event),
                    Triple("chat_tab", "Chat", Icons.Default.Chat),
                    Triple(Destinations.PROFILE, "Profile", Icons.Default.Person)
                )

                val showBottomNav = currentRoute in bottomNavItems.map { it.first }

                Scaffold(
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (showBottomNav) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp)
                                    .navigationBarsPadding(),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .height(64.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.7f))
                                        .blur(radius = 16.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
                                        .background(Color.White.copy(alpha = 0.7f)) // double layer for visibility without blur bleeding
                                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    bottomNavItems.forEach { (route, label, icon) ->
                                        val isSelected = currentRoute == route
                                        val width by animateDpAsState(
                                            targetValue = if (isSelected) 110.dp else 48.dp,
                                            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
                                            label = "width"
                                        )
                                        val bgColor by animateColorAsState(
                                            targetValue = if (isSelected) Color(0xFF4F46E5) else Color.Transparent,
                                            label = "bgColor"
                                        )
                                        val contentColor by animateColorAsState(
                                            targetValue = if (isSelected) Color.White else Color(0xFF6B7280),
                                            label = "contentColor"
                                        )
                                        
                                        Box(
                                            modifier = Modifier
                                                .width(width)
                                                .height(48.dp)
                                                .clip(CircleShape)
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
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    icon,
                                                    contentDescription = label,
                                                    tint = contentColor,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                if (isSelected) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = label,
                                                        color = contentColor,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1
                                                    )
                                                }
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
                            startDestination = if (com.mitron.connect.data.SessionManager.getUserId() != null) Destinations.HOME else Destinations.WELCOME
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
                                    onNavigateToEventsTab = { navController.navigate("events") }
                                )
                            }
                            composable("contacts") { PeopleTabContent(onOpenProfile = { navController.navigate(Destinations.profile(it)) }) }
                            composable("events") { EventsTabContent(onOpenEvent = { navController.navigate(Destinations.event(it)) }) }
                            composable("chat_tab") { ChatsTabContent(onOpenChat = { navController.navigate(Destinations.chat(it)) }) }
                            composable(Destinations.PROFILE) {
                                ProfileScreen(
                                    contactId = com.mitron.connect.data.SessionManager.getUserId() ?: "",
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
                                    onSignInSuccess = { navController.navigate(Destinations.HOME) { popUpTo(Destinations.WELCOME) { inclusive = true } } },
                                    onBack = { navController.popBackStack() },
                                    onNavigateToSignUp = { navController.navigate(Destinations.SIGN_UP) { popUpTo(Destinations.SIGN_IN) { inclusive = true } } }
                                )
                            }
                            composable(Destinations.SIGN_UP) {
                                SignUpScreen(
                                    onSignUpSuccess = { navController.navigate(Destinations.HOME) { popUpTo(Destinations.WELCOME) { inclusive = true } } },
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
