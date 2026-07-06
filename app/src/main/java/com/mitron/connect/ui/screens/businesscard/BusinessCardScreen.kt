package com.mitron.connect.ui.screens.businesscard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitron.connect.data.model.Contact
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.components.ConnectTopBar
import com.mitron.connect.ui.components.QrCode
import com.mitron.connect.ui.components.ShimmerEffect
import com.mitron.connect.ui.components.Tag
import com.mitron.connect.ui.components.TagStyle
import com.mitron.connect.ui.screens.profile.ProfileViewModel
import com.mitron.connect.ui.theme.AvatarSize
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing

@Composable
fun BusinessCardScreen(
    contactId: String,
    onBack: () -> Unit,
    onEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val colors = ConnectTheme.colors

    LaunchedEffect(contactId) {
        if (contactId == "my_card") {
            viewModel.loadCurrentUser()
        } else {
            viewModel.loadContact(contactId)
        }
    }

    val stateContact by viewModel.contact.collectAsState()
    val isCurrentUser by viewModel.isCurrentUser.collectAsState()
    val contact = if (contactId == "preview") {
        Contact(
            id = "preview", name = "Jane Doe", title = "Senior Developer",
            company = "Mitron Tech", website = "janedoe.dev",
            linkedin = "linkedin.com/in/janedoe", initials = "JD"
        )
    } else { stateContact }

    Scaffold(
        topBar = {
            ConnectTopBar(
                title = "My card",
                onBack = onBack,
                actions = {
                    if (isCurrentUser) {
                        IconButton(onClick = onEditProfile) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Profile")
                        }
                    }
                }
            )
        },
        containerColor = colors.surface1
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Ambient glowing orbs behind the glass card
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-50).dp, y = 50.dp)
                    .background(Color(0xFF4F46E5).copy(alpha = 0.3f), CircleShape)
                    .blur(100.dp)
            )
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = (-100).dp)
                    .background(Color(0xFFEC4899).copy(alpha = 0.3f), CircleShape)
                    .blur(100.dp)
            )

            // Glassmorphism Card
            Column(
                modifier = Modifier
                    .padding(Spacing.lg)
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (contact == null) {
                    // Shimmer Loading State
                    ShimmerEffect(modifier = Modifier.size(80.dp).clip(CircleShape))
                    Spacer(modifier = Modifier.height(16.dp))
                    ShimmerEffect(modifier = Modifier.height(24.dp).width(150.dp).clip(RoundedCornerShape(8.dp)))
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerEffect(modifier = Modifier.height(16.dp).width(200.dp).clip(RoundedCornerShape(8.dp)))
                    Spacer(modifier = Modifier.height(32.dp))
                    ShimmerEffect(modifier = Modifier.size(196.dp).clip(RoundedCornerShape(14.dp)))
                } else {
                    // Actual Content
                    Avatar(
                        initials = contact.initials,
                        size = AvatarSize.large,
                        background = colors.accentBg,
                        foreground = colors.accent,
                    )
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(top = Spacing.md),
                    )
                    Text(
                        text = "${contact.title}, ${contact.company}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                    )
                    Box(
                        modifier = Modifier
                            .padding(vertical = Spacing.xl)
                            .size(200.dp)
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(2.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        QrCode(
                            content = "mitron://connect/${contact.id}",
                            modifier = Modifier.fillMaxSize().padding(12.dp)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        if (contact.website?.isNotBlank() == true) Tag(text = "Website", style = TagStyle.NEUTRAL)
                        if (contact.linkedin?.isNotBlank() == true) Tag(text = "LinkedIn", style = TagStyle.NEUTRAL)
                    }

                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        OutlinedButton(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.danger),
                            border = BorderStroke(1.dp, colors.danger)
                        ) {
                            Icon(Icons.Filled.ExitToApp, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("Log out", color = colors.danger)
                        }
                    }
                }
            }
        }
    }
}
