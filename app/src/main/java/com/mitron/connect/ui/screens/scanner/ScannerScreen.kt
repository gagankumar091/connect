package com.mitron.connect.ui.screens.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView

private val ScannerPrimaryColor = Color(0xFF5D5FEF)

@Composable
fun ScannerScreen(
    onBack: () -> Unit,
    onResult: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = QR, 1 = NFC
    var hasCameraPermission by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
    var barcodeViewRef by remember { mutableStateOf<BarcodeView?>(null) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Real-time Camera Background using ZXing BarcodeView
        if (hasCameraPermission && selectedTab == 0) {
            AndroidView(
                factory = { ctx ->
                    BarcodeView(ctx).apply {
                        barcodeViewRef = this
                        decodeContinuous(object : BarcodeCallback {
                            override fun barcodeResult(result: BarcodeResult?) {
                                result?.text?.let { 
                                    // Pause to avoid spamming
                                    pause()
                                    onResult(it)
                                    Toast.makeText(ctx, "Scanned: $it", Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                        resume()
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    if (flashEnabled) {
                        view.setTorch(true)
                    } else {
                        view.setTorch(false)
                    }
                }
            )
        } else if (selectedTab == 1) {
            // NFC View background placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF111111))
            )
        } else {
            // Awaiting permission or denied
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF111111)),
                contentAlignment = Alignment.Center
            ) {
                Text("Camera permission required", color = Color.White)
            }
        }

        // Overlay & Darkening
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            // Top AppBar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Toggle Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedTab == 0) Color.White else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "QR Code",
                            color = if (selectedTab == 0) Color.Black else Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedTab == 1) Color.White else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NFC",
                            color = if (selectedTab == 1) Color.Black else Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(40.dp)) // Symmetry
            }
            
            // AI Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(ScannerPrimaryColor.copy(alpha = 0.9f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("AI Ready to Process Scan", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Scanner Area
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (selectedTab == 0) {
                    ScannerFrame()
                } else {
                    NfcScannerFrame()
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Text("Scan to connect", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (selectedTab == 0) "Position the QR code inside the frame to instantly\nexchange professional profiles." else "Hold your phone near the NFC tag to connect.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Bottom Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 32.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScannerBottomAction(
                        icon = Icons.Outlined.QrCode2, 
                        label = "My QR", 
                        onClick = { Toast.makeText(context, "Opening My QR...", Toast.LENGTH_SHORT).show() }
                    )
                    ScannerBottomAction(
                        icon = Icons.Outlined.Bolt, 
                        label = "Flash", 
                        isActive = flashEnabled,
                        onClick = { flashEnabled = !flashEnabled }
                    )
                    ScannerBottomAction(
                        icon = Icons.Outlined.Image, 
                        label = "Gallery", 
                        onClick = { Toast.makeText(context, "Opening Gallery...", Toast.LENGTH_SHORT).show() }
                    )
                }
            }
        }
    }
}

@Composable
fun ScannerFrame() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanlineAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline"
    )

    Box(modifier = Modifier.size(280.dp)) {
        // Custom corners (Clear hole punched inside the dark overlay is tricky in pure compose without blend modes,
        // so we just draw the white corners here)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val length = 40.dp.toPx()
            val stroke = 4.dp.toPx()
            val radius = 16.dp.toPx()
            val color = Color.White

            // Top Left
            drawPath(
                path = Path().apply {
                    moveTo(0f, length)
                    lineTo(0f, radius)
                    quadraticBezierTo(0f, 0f, radius, 0f)
                    lineTo(length, 0f)
                },
                color = color,
                style = Stroke(width = stroke)
            )
            // Top Right
            drawPath(
                path = Path().apply {
                    moveTo(size.width - length, 0f)
                    lineTo(size.width - radius, 0f)
                    quadraticBezierTo(size.width, 0f, size.width, radius)
                    lineTo(size.width, length)
                },
                color = color,
                style = Stroke(width = stroke)
            )
            // Bottom Left
            drawPath(
                path = Path().apply {
                    moveTo(0f, size.height - length)
                    lineTo(0f, size.height - radius)
                    quadraticBezierTo(0f, size.height, radius, size.height)
                    lineTo(length, size.height)
                },
                color = color,
                style = Stroke(width = stroke)
            )
            // Bottom Right
            drawPath(
                path = Path().apply {
                    moveTo(size.width - length, size.height)
                    lineTo(size.width - radius, size.height)
                    quadraticBezierTo(size.width, size.height, size.width, size.height - radius)
                    lineTo(size.width, size.height - length)
                },
                color = color,
                style = Stroke(width = stroke)
            )
        }

        // Animated Scanning Line with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .graphicsLayer {
                    translationY = scanlineAnim
                }
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, ScannerPrimaryColor, Color.Transparent)
                    )
                )
        )
    }
}

@Composable
fun NfcScannerFrame() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseSize"
    )

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.WifiTethering, 
            contentDescription = "NFC",
            tint = ScannerPrimaryColor,
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    scaleX = pulseAnim
                    scaleY = pulseAnim
                    alpha = 2f - pulseAnim
                }
        )
    }
}

@Composable
fun ScannerBottomAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isActive: Boolean = false, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.dp, Color.White.copy(alpha = if (isActive) 0.6f else 0.0f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = if (isActive) ScannerPrimaryColor else Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
    }
}
