package com.mitron.connect.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mitron.connect.ui.screens.briefing.DailyBriefingScreen
import com.mitron.connect.ui.screens.businesscard.BusinessCardScreen
import com.mitron.connect.ui.screens.company.CompanyDetailScreen
import com.mitron.connect.ui.screens.health.HealthDashboardScreen
import com.mitron.connect.ui.screens.home.HomeScreen
import com.mitron.connect.ui.screens.meetingsummary.MeetingSummaryScreen
import com.mitron.connect.ui.screens.profile.ProfileScreen
import com.mitron.connect.ui.screens.search.SmartSearchScreen
import com.mitron.connect.ui.screens.timeline.TimelineScreen
import com.mitron.connect.ui.screens.welcome.WelcomeScreen
import com.mitron.connect.ui.screens.welcome.SignInScreen
import com.mitron.connect.ui.screens.welcome.SignUpScreen

@Composable
fun ConnectNavGraph(navController: NavHostController = rememberNavController()) {
    val startDest = if (com.mitron.connect.data.SessionManager.getUserId() != null) Destinations.BRIEFING else Destinations.WELCOME
    NavHost(navController = navController, startDestination = startDest) {

        composable(Destinations.WELCOME) {
            WelcomeScreen(
                onGetStarted = {
                    navController.navigate(Destinations.SIGN_UP)
                },
                onSignIn = {
                    navController.navigate(Destinations.SIGN_IN)
                },
            )
        }

        composable(Destinations.SIGN_IN) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(Destinations.BRIEFING) {
                        popUpTo(Destinations.WELCOME) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onNavigateToSignUp = {
                    navController.navigate(Destinations.SIGN_UP) {
                        popUpTo(Destinations.SIGN_IN) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.SIGN_UP) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Destinations.BRIEFING) {
                        popUpTo(Destinations.WELCOME) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Destinations.SIGN_IN) {
                        popUpTo(Destinations.SIGN_UP) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.BRIEFING) {
            DailyBriefingScreen(
                onContinue = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.BRIEFING) { inclusive = true }
                    }
                },
            )
        }

        composable(Destinations.HOME) {
            HomeScreen(
                onOpenProfile = { id -> navController.navigate(Destinations.profile(id)) },
                onOpenCompany = { id -> navController.navigate(Destinations.company(id)) },
                onOpenEvent = { id -> navController.navigate(Destinations.event(id)) },
                onOpenChat = { id -> navController.navigate(Destinations.chat(id)) },
                onOpenSmartSearch = { navController.navigate(Destinations.SMART_SEARCH) },
                onOpenBusinessCard = { id -> navController.navigate(Destinations.businessCard(id)) },
                onOpenHealth = { navController.navigate(Destinations.HEALTH_DASHBOARD) },
                onOpenNotifications = { navController.navigate(Destinations.NOTIFICATIONS) }
            )
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
                    navController.navigate(Destinations.WELCOME) {
                        popUpTo(0) { inclusive = true } // Clear entire back stack
                    }
                }
            )
        }

        composable(Destinations.EDIT_PROFILE) {
            com.mitron.connect.ui.screens.profile.EditProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(Destinations.TIMELINE) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId").orEmpty()
            TimelineScreen(
                contactId = contactId,
                onBack = { navController.popBackStack() },
                onOpenMeetingSummary = { id -> navController.navigate(Destinations.meetingSummary(id)) },
            )
        }

        composable(Destinations.MEETING_SUMMARY) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId").orEmpty()
            MeetingSummaryScreen(contactId = contactId, onBack = { navController.popBackStack() })
        }

        composable(Destinations.SMART_SEARCH) {
            SmartSearchScreen(
                onBack = { navController.popBackStack() },
                onOpenProfile = { id -> navController.navigate(Destinations.profile(id)) },
            )
        }

        composable(Destinations.BUSINESS_CARD) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId").orEmpty()
            BusinessCardScreen(
                contactId = contactId,
                onBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(Destinations.EDIT_PROFILE) },
                onLogout = {
                    com.mitron.connect.data.SessionManager.clearSession()
                    navController.navigate(Destinations.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.COMPANY_DETAIL) { backStackEntry ->
            val companyId = backStackEntry.arguments?.getString("companyId").orEmpty()
            CompanyDetailScreen(
                companyId = companyId,
                onBack = { navController.popBackStack() },
                onOpenChat = { id -> navController.navigate(Destinations.chat(id)) }
            )
        }

        composable(Destinations.CHAT) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId").orEmpty()
            com.mitron.connect.ui.screens.chats.ChatScreen(chatId = chatId, onBack = { navController.popBackStack() })
        }

        composable(Destinations.EVENT_DETAIL) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId").orEmpty()
            com.mitron.connect.ui.screens.events.EventDetailScreen(
                eventId = eventId,
                onBack = { navController.popBackStack() },
                onOpenProfile = { id -> navController.navigate(Destinations.profile(id)) }
            )
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
            HealthDashboardScreen(onBack = { navController.popBackStack() })
        }

        composable(Destinations.RELATIONSHIP_SCORE) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId").orEmpty()
            com.mitron.connect.ui.screens.score.RelationshipScoreScreen(
                contactId = contactId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
