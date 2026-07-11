package com.mitron.connect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mitron.connect.navigation.ConnectNavGraph
import com.mitron.connect.services.NotificationHelper
import com.mitron.connect.ui.theme.ConnectAppTheme
import com.mitron.connect.ui.theme.ConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        com.mitron.connect.services.CallManager.initAgora(this)
        com.mitron.connect.data.SessionManager.init(this)
        if (com.mitron.connect.data.SessionManager.getUserId() != null) {
            val intent = android.content.Intent(this, com.mitron.connect.services.MitronSyncService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
        enableEdgeToEdge()
        setContent {
            ConnectAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    color = ConnectTheme.colors.surface1
                ) {
                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                        ConnectNavGraph()
                        
                        // Floating Call UI
                        com.mitron.connect.ui.screens.calls.IncomingCallBanner()
                        com.mitron.connect.ui.screens.calls.ActiveCallScreen()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        NotificationHelper.cancelAllNotifications(this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        com.mitron.connect.services.CallManager.destroy()
    }
}
