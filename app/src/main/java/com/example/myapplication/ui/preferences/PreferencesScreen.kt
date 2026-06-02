package com.example.myapplication.ui.preferences

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.PreferencesManager
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.ui.components.Divider2
import com.example.myapplication.ui.components.ProfileMenuGroup
import com.example.myapplication.ui.components.ProfileSectionLabel
import com.example.myapplication.ui.components.ScreenScaffold
import com.example.myapplication.ui.theme.CrimsonRed
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.PureWhite
import kotlinx.coroutines.launch

/** App settings — backend URL, theme, language, notifications, biometric. */
@Composable
fun PreferencesScreen(
    preferencesManager: PreferencesManager?,
    isDarkMode: Boolean, currentLanguage: String,
    @Suppress("UNUSED_PARAMETER") s: (String) -> String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var darkMode    by remember { mutableStateOf(isDarkMode) }
    var selectedLang by remember { mutableStateOf(currentLanguage) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var biometricEnabled     by remember { mutableStateOf(false) }
    var serverUrl by remember { mutableStateOf(RetrofitClient.getBaseUrl()) }
    var serverStatus by remember { mutableStateOf<String?>(null) }
    var probing by remember { mutableStateOf(false) }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            notificationsEnabled = false
            scope.launch { preferencesManager?.setNotifications(false) }
        }
    }

    LaunchedEffect(preferencesManager) { preferencesManager?.notificationsEnabled?.collect { notificationsEnabled = it } }
    LaunchedEffect(preferencesManager) { preferencesManager?.biometricEnabled?.collect { biometricEnabled = it } }

    ScreenScaffold(title = "Settings", onBack = onBack) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 16.dp)) {
            item { ProfileSectionLabel("Backend Server") }
            item {
                ProfileMenuGroup {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Server URL", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(
                            "PostgreSQL-backed API. Use 10.0.2.2:8082 for emulator, or your PC's LAN IP for a real device.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            placeholder = { Text("http://10.0.2.2:8082/api/") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen,
                                focusedLabelColor = EmeraldGreen,
                                cursorColor = EmeraldGreen
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    RetrofitClient.setBaseUrl(serverUrl)
                                    serverStatus = "Saved"
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, EmeraldGreen),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen)
                            ) { Text("Save", fontWeight = FontWeight.SemiBold) }
                            Button(
                                onClick = {
                                    probing = true
                                    RetrofitClient.setBaseUrl(serverUrl)
                                    scope.launch {
                                        val err = RetrofitClient.probe()
                                        serverStatus = if (err == null) "✓ Connected" else "✗ $err"
                                        probing = false
                                    }
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = PureWhite),
                                enabled = !probing
                            ) {
                                if (probing) {
                                    CircularProgressIndicator(color = PureWhite, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                } else Text("Test Connection", fontWeight = FontWeight.SemiBold)
                            }
                        }
                        serverStatus?.let { status ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (status.startsWith("✓")) EmeraldGreen.copy(alpha = 0.10f)
                                        else if (status.startsWith("✗")) CrimsonRed.copy(alpha = 0.10f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    status,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (status.startsWith("✓")) EmeraldGreen
                                            else if (status.startsWith("✗")) CrimsonRed
                                            else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item { ProfileSectionLabel("Appearance") }
            item {
                ProfileMenuGroup {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dark mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Use dark theme across the app",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = darkMode,
                            onCheckedChange = { darkMode = it; scope.launch { preferencesManager?.setDarkMode(it) } },
                            colors = SwitchDefaults.colors(checkedTrackColor = EmeraldGreen)
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item { ProfileSectionLabel("Language") }
            item {
                ProfileMenuGroup {
                    val languages = listOf(
                        Triple("en", "English", "EN"),
                        Triple("fr", "Français", "FR"),
                        Triple("ar", "العربية", "AR")
                    )
                    languages.forEachIndexed { i, (code, name, badge) ->
                        val isSelected = selectedLang == code
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { selectedLang = code; scope.launch { preferencesManager?.setLanguage(code) } }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) EmeraldGreen.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    badge,
                                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp),
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                name, style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)
                            )
                            if (isSelected) Icon(Icons.Filled.Check, null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                        }
                        if (i < languages.size - 1) Divider2()
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item { ProfileSectionLabel("Notifications & Security") }
            item {
                ProfileMenuGroup {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Notifications, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            "Push notifications", style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { wanted ->
                                notificationsEnabled = wanted
                                scope.launch { preferencesManager?.setNotifications(wanted) }
                                if (wanted && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                                        ctx, Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!granted) notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = EmeraldGreen)
                        )
                    }
                    Divider2()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Shield, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            "Biometric authentication", style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { biometricEnabled = it; scope.launch { preferencesManager?.setBiometric(it) } },
                            colors = SwitchDefaults.colors(checkedTrackColor = EmeraldGreen)
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "MyPass", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black,
                        color = EmeraldGreen
                    )
                    Text("Version 1.0.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "© 2026 MyPass. All rights reserved.",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
