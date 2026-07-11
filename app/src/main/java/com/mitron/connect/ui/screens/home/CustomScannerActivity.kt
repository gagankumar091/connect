package com.mitron.connect.ui.screens.home

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomScannerActivity : ComponentActivity() {

    private lateinit var barcodeScannerView: DecoratedBarcodeView
    private var isFlashOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        barcodeScannerView = DecoratedBarcodeView(this).apply {
            barcodeView.decoderFactory = DefaultDecoderFactory(listOf(com.google.zxing.BarcodeFormat.QR_CODE))
            statusView.text = "" // We'll show our own overlay
            // Hide the default viewfinder since we are drawing our own
            viewFinder.visibility = android.view.View.GONE
        }

        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { decodeUri(it) }
        }

        barcodeScannerView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.text?.let { handleResult(it) }
            }
            override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {}
        })

        setContent {
            ScannerScreen(
                barcodeView = barcodeScannerView,
                onClose = { finish() },
                onFlashToggle = {
                    isFlashOn = !isFlashOn
                    if (isFlashOn) barcodeScannerView.setTorchOn() else barcodeScannerView.setTorchOff()
                    isFlashOn
                },
                onGalleryClick = { galleryLauncher.launch("image/*") }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeScannerView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeScannerView.pause()
    }

    private fun handleResult(text: String) {
        val intent = Intent()
        intent.putExtra("SCAN_RESULT", text)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun decodeUri(uri: Uri) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val intArray = IntArray(bitmap.width * bitmap.height)
                    bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                    
                    val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
                    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                    
                    val reader = MultiFormatReader()
                    reader.decode(binaryBitmap).text
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            if (result != null) {
                handleResult(result)
            } else {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(this@CustomScannerActivity, "No QR code found in image", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Composable
fun ScannerScreen(
    barcodeView: DecoratedBarcodeView,
    onClose: () -> Unit,
    onFlashToggle: () -> Boolean,
    onGalleryClick: () -> Unit
) {
    var flashState by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Camera Feed
        AndroidView(
            factory = { barcodeView },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Content
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row (Back, Tabs)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    // Tabs
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.width(180.dp).height(40.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                            // Active Tab: QR Code
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("QR Code", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            // Inactive Tab: NFC
                            Box(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("NFC", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Spacer for symmetry
                    Spacer(modifier = Modifier.size(40.dp))
                }

                // Titles
                Text(
                    text = "Scan to connect",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Position the QR code inside the frame",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // Scanner Frame Area
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .aspectRatio(1f) // Square for QR
            ) {
                // Simple corners using Box
                Box(modifier = Modifier.align(Alignment.TopStart).size(32.dp).border(4.dp, Color.White, RoundedCornerShape(topStart = 16.dp)).padding(4.dp).background(Color.Transparent))
                Box(modifier = Modifier.align(Alignment.TopEnd).size(32.dp).border(4.dp, Color.White, RoundedCornerShape(topEnd = 16.dp)).padding(4.dp).background(Color.Transparent))
                Box(modifier = Modifier.align(Alignment.BottomStart).size(32.dp).border(4.dp, Color.White, RoundedCornerShape(bottomStart = 16.dp)).padding(4.dp).background(Color.Transparent))
                Box(modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).border(4.dp, Color.White, RoundedCornerShape(bottomEnd = 16.dp)).padding(4.dp).background(Color.Transparent))

                // Animated Line
                val infiniteTransition = rememberInfiniteTransition()
                val offsetY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = maxHeight * offsetY)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF3B82F6).copy(alpha = 0f), Color(0xFF3B82F6), Color(0xFF3B82F6).copy(alpha = 0f))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

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
                        .padding(horizontal = 40.dp, vertical = 24.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // My QR
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { /* TODO: Open My QR */ }) {
                        Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(48.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.GridView, contentDescription = "My QR", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("My QR", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    // Flash
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { flashState = onFlashToggle() }) {
                        Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(48.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(if (flashState) Icons.Filled.FlashOn else Icons.Outlined.FlashOn, contentDescription = "Flash", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Flash", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    // Gallery
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onGalleryClick() }) {
                        Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(48.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Image, contentDescription = "Gallery", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Gallery", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
