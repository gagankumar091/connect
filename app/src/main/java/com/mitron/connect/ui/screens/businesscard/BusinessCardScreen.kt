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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (contact == null) {
                    ShimmerEffect(modifier = Modifier.size(250.dp).clip(RoundedCornerShape(16.dp)))
                } else {
                    Text(
                        text = "Scan to Connect",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(bottom = Spacing.xl)
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .border(2.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        QrCode(
                            content = "mitron://connect/${contact.id}",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Spacing.xl))
                    
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text(
                        text = "${contact.title}, ${contact.company}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                    )
                }
            }
        }
    }
}
