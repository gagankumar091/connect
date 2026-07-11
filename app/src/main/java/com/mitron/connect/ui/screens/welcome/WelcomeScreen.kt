package com.mitron.connect.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import com.mitron.connect.ui.theme.Spacing

val BrandBlue = Color(0xFF4F46E5)
val AppGray = Color(0xFFF9FAFB)
val TextDark = Color(0xFF111827)
val TextMuted = Color(0xFF6B7280)
val Slate800 = Color(0xFF1E293B)
val Slate900 = Color(0xFF0F172A)
val Slate500 = Color(0xFF64748B)
val Slate600 = Color(0xFF475569)
val Slate200 = Color(0xFFE2E8F0)

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit, onSignIn: () -> Unit) {
    val infinityIcon = ImageVector.Builder(
        name = "infinity",
        defaultWidth = 80.dp,
        defaultHeight = 40.dp,
        viewportWidth = 80f,
        viewportHeight = 40f
    ).path(
        stroke = SolidColor(BrandBlue),
        strokeLineWidth = 6f,
        strokeLineCap = StrokeCap.Round
    ) {
        moveTo(20f, 10f)
        curveTo(10f, 10f, 5f, 15f, 5f, 20f)
        curveTo(5f, 25f, 10f, 30f, 20f, 30f)
        curveTo(28f, 30f, 32f, 23f, 40f, 20f)
        curveTo(48f, 17f, 52f, 10f, 60f, 10f)
        curveTo(70f, 10f, 75f, 15f, 75f, 20f)
        curveTo(75f, 25f, 70f, 30f, 60f, 30f)
    }.build()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGray)
            .padding(horizontal = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top spacing
        Spacer(modifier = Modifier.weight(0.3f))

        // Logo
        Icon(
            imageVector = infinityIcon,
            contentDescription = "App Logo",
            tint = Color.Unspecified,
            modifier = Modifier.size(80.dp, 40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Connect",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Slate800,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(64.dp))

        val onboardingSlides = listOf(
            Pair("Relationships that\ngrow your business", "The messaging app that remembers,\nhelps and grows your network."),
            Pair("AI-Powered\nNetworking", "Get intelligent meeting summaries\nand follow-up reminders instantly."),
            Pair("Secure\nVideo Calls", "Connect face-to-face with built-in\nhigh definition video meetings.")
        )
        val pagerState = rememberPagerState(pageCount = { onboardingSlides.size })
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(160.dp)
        ) { page ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = onboardingSlides[page].first,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = onboardingSlides[page].second,
                    fontSize = 15.sp,
                    color = Slate500,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pagination Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(onboardingSlides.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) BrandBlue else Slate200
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.7f))

        // Action Buttons
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(16.dp), spotColor = BrandBlue, ambientColor = BrandBlue),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Get Started",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "I have an account",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Slate600
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}