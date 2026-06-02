package com.example.myapplication.ui.profile

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.Divider2
import com.example.myapplication.ui.components.ProfileMenuGroup
import com.example.myapplication.ui.components.ProfileMenuItem
import com.example.myapplication.ui.components.ProfileSectionLabel
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.PureWhite
import com.example.myapplication.ui.theme.StatusError
import com.example.myapplication.viewmodel.AppUiState

/** Bottom-nav "Profile" tab — clean emerald hero (no decorative shapes), then grouped menus. */
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
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ProfileHero(
                name = user?.fullName ?: "Guest",
                email = user?.email ?: "Sign in to access your trips"
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
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
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "MyPass v1.0",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "© 2026 MyPass",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun ProfileHero(name: String, email: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        EmeraldGreen,
                        Color(0xFF003520),
                        Color(0xFF002418)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Profile",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black, letterSpacing = (-0.4).sp
                ),
                color = PureWhite
            )

            // Avatar + name + email — clean, no badges
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(76.dp).clip(CircleShape)
                        .background(PureWhite)
                        .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        name.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black, color = EmeraldGreen
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black, letterSpacing = (-0.4).sp
                        ),
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PureWhite.copy(alpha = 0.85f)
                    )
                }
            }

            // Subtle dashed line at the bottom of the hero (matches Home)
            Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                drawLine(
                    color = Color.White.copy(alpha = 0.18f),
                    start = Offset(0f, size.height / 2),
                    end   = Offset(size.width, size.height / 2),
                    strokeWidth = 1.0f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))
                )
            }
        }
    }
}
