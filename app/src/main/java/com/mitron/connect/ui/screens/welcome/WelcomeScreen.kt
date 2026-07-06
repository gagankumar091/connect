package com.mitron.connect.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mitron.connect.R
import com.mitron.connect.ui.components.ConnectPrimaryButton
import com.mitron.connect.ui.components.ConnectSecondaryButton
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit, onSignIn: () -> Unit) {
    val colors = ConnectTheme.colors
    
    // Premium gradient background
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(colors.surface1, colors.surface2)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(colors.fillPrimary.copy(alpha = 0.1f), CircleShape)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                tint = colors.fillPrimary,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(Spacing.xl))

        Text(
            text = "Connect",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            color = colors.textPrimary,
            modifier = Modifier.fillMaxWidth(),
        )
        
        Spacer(modifier = Modifier.height(Spacing.md))
        
        Text(
            text = "Build relationships that matter. Scan, connect, and remember every detail effortlessly.",
            style = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center
            ),
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = Spacing.md),
        )

        Spacer(modifier = Modifier.weight(1.5f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            ConnectPrimaryButton(
                text = "Create Account", 
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
            ConnectSecondaryButton(
                text = "Sign In", 
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun WelcomeScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        WelcomeScreen(onGetStarted = {}, onSignIn = {})
    }
}