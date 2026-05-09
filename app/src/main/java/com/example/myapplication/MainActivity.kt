package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication.data.PreferencesManager
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.ui.AirlineCheckInApp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize backend network layer (reads/writes URL + token from prefs)
        RetrofitClient.init(applicationContext)

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
}