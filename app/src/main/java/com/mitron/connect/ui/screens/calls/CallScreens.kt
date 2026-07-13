package com.mitron.connect.ui.screens.calls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.mitron.connect.data.RetrofitClient
import com.mitron.connect.data.SessionManager
import com.mitron.connect.services.CallManager
import io.agora.rtc2.video.VideoCanvas
import kotlinx.coroutines.launch

private val CallPrimary = Color(0xFF4343D5)
private val CallError = Color(0xFFE63946)
private val CallSuccess = Color(0xFF10B981)

@Composable
fun IncomingCallBanner() {
    val incomingCall by CallManager.incomingCall.collectAsState()
    val isCallActive by CallManager.isCallActive.collectAsState()
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = incomingCall != null && !isCallActive,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        incomingCall?.let { call ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = CallPrimary.copy(alpha = 0.5f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1B1B20).copy(alpha = 0.95f))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!call.callerAvatar.isNullOrEmpty()) {
                        AsyncImage(
                            model = call.callerAvatar,
                            contentDescription = "Caller",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Brush.linearGradient(listOf(CallPrimary, Color(0xFF5D5FEF))), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(call.callerName.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(call.callerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(if (call.isVideo) "Incoming Video Call..." else "Incoming Audio Call...", color = Color(0xFFA1A1AA), fontSize = 14.sp)
                    }

                    Row {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CallError)
                                .clickable { CallManager.clearIncomingCall() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "Decline", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CallSuccess)
                                .clickable {
                                    scope.launch {
                                        try {
                                            val res = RetrofitClient.apiService.initiateCall(com.mitron.connect.data.InitiateCallRequest(SessionManager.getUserId() ?: "", call.callerId, false))
                                            val req = RetrofitClient.apiService.generateToken(com.mitron.connect.data.TokenRequest(call.channelName, 0, "publisher"))
                                            if (req.success && req.token != null) {
                                                CallManager.joinCall(req.token, call.channelName, call.isVideo)
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Accept", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveCallScreen() {
    val isCallActive by CallManager.isCallActive.collectAsState()
    val remoteUid by CallManager.remoteUid.collectAsState()
    val isMuted by CallManager.isMuted.collectAsState()
    val isVideoEnabled by CallManager.isVideoEnabled.collectAsState()

    if (!isCallActive) return

    val pulseTransition = rememberInfiniteTransition()
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F13)) // Deep dark background
    ) {
        if (isVideoEnabled) {
            // Video Call layout
            if (remoteUid != null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val surfaceView = io.agora.rtc2.RtcEngine.CreateRendererView(ctx)
                        surfaceView.setZOrderMediaOverlay(false)
                        CallManager.rtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, remoteUid!!))
                        surfaceView
                    },
                    update = { view ->
                        CallManager.rtcEngine?.setupRemoteVideo(VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, remoteUid!!))
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Waiting for video...", color = Color.Gray)
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 20.dp)
                    .size(width = 110.dp, height = 150.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val surfaceView = io.agora.rtc2.RtcEngine.CreateRendererView(ctx)
                        surfaceView.setZOrderMediaOverlay(true)
                        CallManager.rtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
                        surfaceView
                    },
                    update = { view ->
                        CallManager.rtcEngine?.setupLocalVideo(VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, 0))
                    }
                )
            }
        } else {
            // Audio Call Layout with gradient pulse
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1B1B20), Color(0xFF000000)))))
                
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(160.dp).scale(pulseScale).background(CallPrimary.copy(alpha = 0.2f), CircleShape))
                        Box(modifier = Modifier.size(140.dp).scale(pulseScale * 0.9f).background(CallPrimary.copy(alpha = 0.4f), CircleShape))
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(CallPrimary, CircleShape)
                                .shadow(12.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Audio Call in Progress...", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("00:00", color = Color(0xFFA1A1AA), fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
        
        // Floating Controls Pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFF1B1B20).copy(alpha = 0.85f))
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                CallControlButton(
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    isActive = isMuted,
                    onClick = { CallManager.toggleMute() }
                )
                
                CallControlButton(
                    icon = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    isActive = !isVideoEnabled,
                    onClick = { CallManager.toggleVideo() }
                )
                
                CallControlButton(
                    icon = Icons.Default.Cameraswitch,
                    isActive = false,
                    onClick = { CallManager.switchCamera() }
                )
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CallError)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { CallManager.endCall() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
fun CallControlButton(icon: androidx.compose.ui.graphics.vector.ImageVector, isActive: Boolean, onClick: () -> Unit) {
    val bgColor = if (isActive) Color.White else Color(0xFF3F3F46)
    val tint = if (isActive) Color.Black else Color.White

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
    }
}
