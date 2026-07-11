package com.mitron.connect.ui.screens.calls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mitron.connect.data.RetrofitClient
import com.mitron.connect.data.SessionManager
import com.mitron.connect.services.CallManager
import com.mitron.connect.services.IncomingCallData
import kotlinx.coroutines.launch
import androidx.compose.ui.viewinterop.AndroidView
import io.agora.rtc2.video.VideoCanvas
import android.view.SurfaceView

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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .windowInsetsPadding(WindowInsets.statusBars),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!call.callerAvatar.isNullOrEmpty()) {
                        AsyncImage(
                            model = call.callerAvatar,
                            contentDescription = "Caller",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.Gray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(call.callerName.take(2).uppercase(), color = Color.White)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(call.callerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(if (call.isVideo) "Incoming Video Call" else "Incoming Audio Call", color = Color(0xFF94A3B8), fontSize = 14.sp)
                    }

                    Row {
                        IconButton(
                            onClick = { CallManager.clearIncomingCall() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFEF4444), CircleShape)
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "Decline", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val res = RetrofitClient.apiService.initiateCall(com.mitron.connect.data.InitiateCallRequest(SessionManager.getUserId() ?: "", call.callerId, false)) // dummy
                                        // Generate token endpoint
                                        val req = com.mitron.connect.data.RetrofitClient.apiService.generateToken(com.mitron.connect.data.TokenRequest(call.channelName, 0, "publisher"))
                                        if (req.success && req.token != null) {
                                            CallManager.joinCall(req.token, call.channelName, call.isVideo)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF22C55E), CircleShape)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Render remote video (takes up full screen)
        if (isVideoEnabled && remoteUid != null) {
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
        }

        // Render local video (floating in corner)
        if (isVideoEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
                    .size(width = 120.dp, height = 160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
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
        }
        
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("In Call", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CallButton(
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    onClick = { CallManager.toggleMute() }
                )
                
                CallButton(
                    icon = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    onClick = { CallManager.toggleVideo() }
                )
                
                CallButton(
                    icon = Icons.Default.Cameraswitch,
                    onClick = { CallManager.switchCamera() }
                )
                
                CallButton(
                    icon = Icons.Default.CallEnd,
                    onClick = { CallManager.endCall() },
                    color = Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
fun CallButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, color: Color = Color(0xFF334155)) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(color, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
    }
}
