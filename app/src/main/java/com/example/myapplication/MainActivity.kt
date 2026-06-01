package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.example.myapplication.data.PreferencesManager
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.ui.AirlineCheckInApp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* result is granted boolean — we don't block UI on this */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize backend network layer (reads/writes URL + token from prefs)
        RetrofitClient.init(applicationContext)

        // Request POST_NOTIFICATIONS at first launch on Android 13+
        requestNotificationPermissionIfNeeded()

        val preferencesManager = PreferencesManager(applicationContext)

        setContent {
            val isDarkMode by preferencesManager.isDarkMode.collectAsState(initial = false)
            val language by preferencesManager.language.collectAsState(initial = "en")

            MyApplicationTheme(darkTheme = isDarkMode) {
                AirlineCheckInApp(
                    preferencesManager = preferencesManager,
                    isDarkMode = isDarkMode,
                    currentLanguage = language
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
