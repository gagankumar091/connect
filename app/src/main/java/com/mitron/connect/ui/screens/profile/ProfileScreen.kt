package com.mitron.connect.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mitron.connect.data.model.Contact
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.components.ConnectPrimaryButton
import com.mitron.connect.ui.components.ConnectTopBar
import com.mitron.connect.ui.components.QrCode
import com.mitron.connect.ui.components.Tag
import com.mitron.connect.ui.components.TagStyle
import com.mitron.connect.ui.theme.AvatarSize
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing
import com.mitron.connect.ui.theme.tints

@Composable
fun ProfileScreen(
    contactId: String,
    onBack: () -> Unit,
    onOpenTimeline: (String) -> Unit,
    onOpenBusinessCard: (String) -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    val colors = ConnectTheme.colors

    LaunchedEffect(contactId) {
        if (contactId == "my_card") viewModel.loadCurrentUser()
        else viewModel.loadContact(contactId)
    }

    val contact by viewModel.contact.collectAsState()
    val isCurrentUser by viewModel.isCurrentUser.collectAsState()

    val currentContact = contact ?: run {
        if (contactId == "preview") {
            Contact(
                id = "preview", name = "Jane Doe", title = "Senior Developer",
                company = "Mitron Tech", email = "jane.doe@example.com",
                phone = "+1 987 654 3210", linkedin = "linkedin.com/in/janedoe",
                website = "janedoe.dev", initials = "JD", score = 92,
                daysSinceContact = 3, mutualsCount = 15,
                nextAction = "Send follow-up email about the new project.",
                sharedInterests = listOf("Kotlin", "Compose", "Design")
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colors.fillPrimary,
                    strokeWidth = 3.dp
                )
            }
            return
        }
    }

    val (avatarBg, avatarFg) = currentContact.color.tints(colors)

    Scaffold(
        topBar = {
            ConnectTopBar(
                title = currentContact.name,
                onBack = onBack,
                actions = {
                    if (isCurrentUser) {
                        IconButton(onClick = onEditProfile) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Edit Profile",
                                tint = colors.accent
                            )
                        }
                        IconButton(onClick = onLogout) {
                            Icon(
                                Icons.Filled.ExitToApp,
                                contentDescription = "Log out",
                                tint = colors.danger
                            )
                        }
                    } else {
                        IconButton(onClick = { onOpenBusinessCard(currentContact.id) }) {
                            Icon(
                                Icons.Filled.IosShare,
                                contentDescription = "Share profile",
                                tint = colors.textSecondary
                            )
                        }
                    }
                },
            )
        },
        containerColor = colors.surface1
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // Profile Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                avatarBg.copy(alpha = 0.2f),
                                colors.surface1
                            )
                        )
                    )
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (currentContact.avatarUrl != null) {
                        AsyncImage(
                            model = currentContact.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Avatar(
                            initials = currentContact.initials ?: "",
                            size = AvatarSize.large,
                            background = avatarBg,
                            foreground = avatarFg,
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        currentContact.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        "${currentContact.title ?: ""} at ${currentContact.company ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = colors.proBg
                        ) {
                            Text(
                                "Score ${currentContact.score}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.pro,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = colors.dangerBg
                        ) {
                            Text(
                                "${currentContact.daysSinceContact}d since contact",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.danger,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Contact Details Card
            ProfileSection(colors.surface2) {
                SectionTitle("Contact", Icons.Filled.Person)
                Spacer(modifier = Modifier.height(8.dp))
                ContactDetail(Icons.Filled.Email, currentContact.email ?: "—")
                ContactDetail(Icons.Filled.Phone, currentContact.phone ?: "—")
                ContactDetail(Icons.Filled.Link, currentContact.linkedin ?: "—")
                if (currentContact.website != null) {
                    ContactDetail(Icons.Filled.Language, currentContact.website!!)
                }
            }

            // Interests & Mutuals
            ProfileSection(colors.surface2) {
                SectionTitle("Shared Interests", Icons.Filled.Favorite)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    currentContact.sharedInterests.forEach { interest ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.accentBg
                        ) {
                            Text(
                                interest,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.accent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surface1
                    ) {
                        Text(
                            "${currentContact.mutualsCount} mutuals",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Next Suggested Action
            ProfileSection(colors.proBg) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = colors.pro,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Next suggested action",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.pro
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    currentContact.nextAction ?: "No suggestions yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.pro
                )
            }

            // QR Code Section
            ProfileSection(colors.surface2) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Scan to Connect",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Text(
                        "Share this QR with others to connect instantly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = Spacing.md)
                    )
                    val encodedName = android.net.Uri.encode(currentContact.name)
                    val encodedEmail = android.net.Uri.encode(currentContact.email ?: "")
                    val encodedTitle = android.net.Uri.encode(currentContact.title ?: "")
                    val qrContent = "mitron://connect/${currentContact.id}?name=$encodedName&email=$encodedEmail&title=$encodedTitle"
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        QrCode(
                            content = qrContent,
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .padding(8.dp)
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                ConnectPrimaryButton(
                    text = "Message",
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = { onOpenTimeline(currentContact.id) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.borderStrong),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary)
                ) {
                    Icon(
                        Icons.Filled.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Notes")
                }
            }

            // Logout Button (own profile only)
            if (isCurrentUser) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.danger),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.danger)
                ) {
                    Icon(
                        Icons.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log out", fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(Spacing.md))
            }
        }
    }
}

@Composable
private fun ProfileSection(
    backgroundColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = ConnectTheme.colors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            content()
        }
    }
}

@Composable
private fun SectionTitle(text: String, icon: ImageVector) {
    val colors = ConnectTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textMuted,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun ContactDetail(icon: ImageVector, text: String) {
    val colors = ConnectTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
    }
}
