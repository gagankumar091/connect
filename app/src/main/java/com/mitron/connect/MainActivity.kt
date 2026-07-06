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
        com.mitron.connect.data.SessionManager.init(this)
        enableEdgeToEdge()
        setContent {
            ConnectAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    color = ConnectTheme.colors.surface1
                ) {
                    ConnectNavGraph()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        NotificationHelper.cancelAllNotifications(this)
    }
}
