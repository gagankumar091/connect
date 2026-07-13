package com.mitron.connect.ui.screens.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val WelcSurfaceColor = Color(0xFFFBF8FF)
private val WelcPrimaryColor = Color(0xFF5D5FEF)
private val WelcPrimaryLight = Color(0xFFE1E0FF)
private val WelcOnSurfaceColor = Color(0xFF1B1B20)
private val WelcOnSurfaceVariantColor = Color(0xFF464555)
private val WelcOutlineColor = Color(0xFF767586)
private val WelcOutlineVariantColor = Color(0xFFC7C4D7)

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit, onSignIn: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatAnim"
    )

    // Subtle background particles
    val particleOffsets = remember { List(3) { Pair((Math.random() * 200).toFloat(), (Math.random() * 400).toFloat()) } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WelcSurfaceColor)
    ) {
        // Decorative background blurs
        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = (-80).dp)
                .size(300.dp)
                .background(WelcPrimaryColor.copy(alpha = 0.05f), CircleShape)
                .blur(80.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 100.dp, y = (-50).dp)
                .size(250.dp)
                .background(Color(0xFF4848D2).copy(alpha = 0.05f), CircleShape)
                .blur(80.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Main Content Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Logo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(y = floatY.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Tilted background highlight
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .rotate(12f)
                            .background(WelcPrimaryColor.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                    )
                    // White container
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = WelcPrimaryColor.copy(alpha = 0.2f))
                            .background(Color.White, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AllInclusive,
                            contentDescription = "Connect Logo",
                            tint = WelcPrimaryColor,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Connect",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = WelcPrimaryColor,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Relationships that grow your business",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = WelcOnSurfaceColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "The messaging app that remembers, helps and grows your network.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = WelcOnSurfaceVariantColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Carousel Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(WelcPrimaryColor, CircleShape))
                    Box(modifier = Modifier.size(8.dp).background(WelcOutlineVariantColor, CircleShape))
                    Box(modifier = Modifier.size(8.dp).background(WelcOutlineVariantColor, CircleShape))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WelcPrimaryColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Get Started",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onSignIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WelcOnSurfaceColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WelcOutlineVariantColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "I have an account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "By joining, you agree to our Terms of Service.",
                    fontSize = 12.sp,
                    color = WelcOutlineColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}