package com.mitron.connect.ui.screens.score

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.screens.profile.ProfileViewModel
import com.mitron.connect.ui.theme.AvatarSize

val BackgroundColor = Color(0xFFFAF8FF)
val SurfaceColor = Color(0xFFFAF8FF)
val SurfaceContainerHigh = Color(0xFFE2E7FF)
val SurfaceVariantColor = Color(0xFFDAE2FD)
val PrimaryColor = Color(0xFF0900DB)
val PrimaryContainer = Color(0xFF2D31FA)
val SecondaryContainer = Color(0xFF00CCF9)
val OnSurfaceColor = Color(0xFF131B2E)
val OnSurfaceVariantColor = Color(0xFF454557)
val OutlineColor = Color(0xFF767589)

@Composable
fun RelationshipScoreScreen(
    contactId: String,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    LaunchedEffect(contactId) {
        viewModel.loadContact(contactId)
    }

    val contact by viewModel.contact.collectAsState()

    if (contact == null) {
        Box(modifier = Modifier.fillMaxSize().background(BackgroundColor), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryColor)
        }
        return
    }

    val c = contact!!

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceColor.copy(alpha = 0.8f))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Transparent, CircleShape)
                        .offset(x = (-8).dp)
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = OnSurfaceColor)
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(2.dp, SurfaceContainerHigh, CircleShape)
                ) {
                    if (!c.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = c.avatarUrl,
                            contentDescription = c.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Avatar(initials = c.initials, size = AvatarSize.medium, modifier = Modifier.fillMaxSize())
                    }
                }

                IconButton(
                    onClick = {},
                    modifier = Modifier.size(40.dp).offset(x = 8.dp)
                ) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = PrimaryColor)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = c.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurfaceColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = c.company ?: "Unknown Company",
                    fontSize = 16.sp,
                    color = OnSurfaceVariantColor
                )
            }

            // Relationship Score Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = PrimaryContainer.copy(alpha = 0.05f))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Relationship Score",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceColor,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 24.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Circular Progress
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Background circle
                            drawArc(
                                color = SurfaceContainerHigh,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Progress gradient
                            val sweepGradient = Brush.sweepGradient(
                                0.0f to PrimaryColor,
                                0.5f to SecondaryContainer,
                                1.0f to SurfaceContainerHigh
                            )
                            drawArc(
                                brush = sweepGradient,
                                startAngle = -90f,
                                sweepAngle = (c.score / 100f) * 360f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Inner score bubble
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .shadow(20.dp, CircleShape, spotColor = PrimaryColor.copy(alpha = 0.05f))
                                .background(SurfaceColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = c.score.toString(),
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryColor,
                                    lineHeight = 48.sp
                                )
                                Text(
                                    text = "/100",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceVariantColor,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Last spoke", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariantColor, modifier = Modifier.padding(bottom = 4.dp))
                            Text("${c.daysSinceContact} days ago", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceColor)
                        }

                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(SurfaceContainerHigh))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Strength", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariantColor, modifier = Modifier.padding(bottom = 4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(SecondaryContainer, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when {
                                        c.score >= 80 -> "Strong"
                                        c.score >= 50 -> "Good"
                                        else -> "Weak"
                                    },
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnSurfaceColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Suggested Action
            Text(
                text = "SUGGESTED ACTION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariantColor,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 12.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionItem("Send festival wishes", Icons.Outlined.Celebration)
                ActionItem("Share relevant article", Icons.Outlined.Article)
                ActionItem("Schedule follow up", Icons.Outlined.CalendarMonth)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Primary Action
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, PrimaryColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor.copy(alpha = 0.05f),
                    contentColor = PrimaryColor
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = null
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Insights, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Full AI Analysis", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ActionItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = PrimaryContainer.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.7f))
            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceContainerHigh, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = OnSurfaceColor)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = "Proceed", tint = OnSurfaceVariantColor, modifier = Modifier.size(24.dp))
        }
    }
}
