package com.example.phylaxfileaccess

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.phylaxfileaccess.screens.HomeScreen
import com.example.phylaxfileaccess.screens.PermissionScreen
import com.example.phylaxfileaccess.screens.WelcomeScreen
import com.example.phylaxfileaccess.ui.theme.PhylaxFileAccessTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            PhylaxFileAccessTheme {
                val lifecycleOwner = LocalLifecycleOwner.current
                var showWelcome by remember { mutableStateOf(true) }
                
                // Use a state for permission so Compose recomposes when it changes
                var hasPermission by remember { mutableStateOf(hasFilePermission()) }

                // Observe lifecycle to re-check permission when user returns from Settings
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            hasPermission = hasFilePermission()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                if (showWelcome) {
                    WelcomeScreen(
                        onContinue = { showWelcome = false }
                    )
                } else {
                    if (hasPermission) {
                        HomeScreen()
                    } else {
                        PermissionScreen()
                    }
                }
            }
        }
    }

    /**
     * Checks if the app has the required file access permissions.
     */
    fun hasFilePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            // For Android 10 and below, usually handled via standard runtime permissions.
            // Returning true here as the PermissionScreen specifically targets Android 11+.
            true 
        }
    }

    // Helper method if you need to manually trigger the intent (optional)
    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = "package:$packageName".toUri()
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            }
        }
    }
}
