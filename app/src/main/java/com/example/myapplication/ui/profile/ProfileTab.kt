package com.example.myapplication.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.Divider2
import com.example.myapplication.ui.components.GoldAccent
import com.example.myapplication.ui.components.ProfileMenuGroup
import com.example.myapplication.ui.components.ProfileMenuItem
import com.example.myapplication.ui.components.ProfileSectionLabel
import com.example.myapplication.ui.theme.DeepGold
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.SoftGold
import com.example.myapplication.ui.theme.StatusError
import com.example.myapplication.viewmodel.AppUiState

/** Bottom-nav "Profile" tab — header card with Gold badge plus three grouped menus. */
@Composable
fun ProfileTab(
    state: AppUiState,
    @Suppress("UNUSED_PARAMETER") s: (String) -> String,
    onOpenPreferences: () -> Unit,
    onLogout: () -> Unit
) {
    val user = state.currentUser
    val ctx = LocalContext.current
    val openMailIntent: (String, String) -> Unit = { addr, subj ->
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:$addr")
            putExtra(android.content.Intent.EXTRA_SUBJECT, subj)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { ctx.startActivity(intent) }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoldAccent(modifier = Modifier.height(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black, letterSpacing = (-0.6).sp
                        )
                    )
                    Text(
                        "Account & preferences",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape    = RoundedCornerShape(18.dp),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation= CardDefaults.cardElevation(2.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(1.dp, EmeraldGreen.copy(0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            user?.fullName?.take(1)?.uppercase() ?: "G",
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black,
                            color = EmeraldGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user?.fullName ?: "Guest", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            user?.email ?: "Sign in to access your trips",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SoftGold).padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Filled.Star, null, tint = DeepGold, modifier = Modifier.size(11.dp))
                            Text("GOLD", style = MaterialTheme.typography.labelSmall, color = DeepGold, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { ProfileSectionLabel("Account") }
        item {
            ProfileMenuGroup {
                ProfileMenuItem(Icons.Filled.Person, "Personal information", onClick = onOpenPreferences)
                Divider2()
                ProfileMenuItem(Icons.Filled.ConfirmationNumber, "My bookings", onClick = { })
                Divider2()
                ProfileMenuItem(Icons.Filled.Shield, "Passport & documents", onClick = onOpenPreferences)
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { ProfileSectionLabel("Preferences") }
        item {
            ProfileMenuGroup {
                ProfileMenuItem(Icons.Filled.Settings, "App settings", onClick = onOpenPreferences)
                Divider2()
                ProfileMenuItem(Icons.Filled.Notifications, "Notifications", onClick = onOpenPreferences)
                Divider2()
                ProfileMenuItem(Icons.Filled.Language, "Language", onClick = onOpenPreferences)
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { ProfileSectionLabel("Support") }
        item {
            ProfileMenuGroup {
                ProfileMenuItem(
                    Icons.Filled.SupportAgent, "Help center",
                    onClick = { openMailIntent("support@mypass.dz", "MyPass help request") }
                )
                Divider2()
                ProfileMenuItem(
                    Icons.AutoMirrored.Filled.Logout, "Log out",
                    iconTint = StatusError, onClick = onLogout
                )
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "MyPass v1.0  ·  Made in Algeria",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
