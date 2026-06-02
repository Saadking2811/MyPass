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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.Divider2
import com.example.myapplication.ui.components.ProfileMenuGroup
import com.example.myapplication.ui.components.ProfileMenuItem
import com.example.myapplication.ui.components.ProfileSectionLabel
import com.example.myapplication.ui.theme.DeepGold
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.PureWhite
import com.example.myapplication.ui.theme.SoftGold
import com.example.myapplication.ui.theme.StatusError
import com.example.myapplication.viewmodel.AppUiState

/** Bottom-nav "Profile" tab — gradient hero with avatar + Gold badge, then grouped menus. */
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
                email = user?.email ?: "Sign in to access your trips",
                miles = "12,840"
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
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
                    "MyPass v1.0  ·  Made with care",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "© 2026 MyPass · ENSI 2CS SIL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun ProfileHero(name: String, email: String, miles: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        EmeraldGreen,
                        Color(0xFF003520),
                        Color(0xFF002418)
                    )
                )
            )
    ) {
        // Decorative soft rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width * 0.12f
            val cy = size.height * 0.85f
            for (r in listOf(180f, 130f, 80f)) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = r,
                    center = Offset(cx, cy)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Profile",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black, letterSpacing = (-0.4).sp
                ),
                color = PureWhite
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp).clip(CircleShape)
                        .background(PureWhite)
                        .border(2.dp, SoftGold.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        name.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black, color = EmeraldGreen
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black, letterSpacing = (-0.4).sp
                        ),
                        color = PureWhite
                    )
                    Text(
                        email,
                        style = MaterialTheme.typography.bodySmall,
                        color = PureWhite.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SoftGold)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Star, null, tint = DeepGold, modifier = Modifier.size(12.dp))
                            Text(
                                "GOLD · $miles MILES",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                                color = DeepGold, fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Floating KPI tiles
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroStat("Tier",         "Gold",   modifier = Modifier.weight(1f))
                HeroStat("Member since", "2024",   modifier = Modifier.weight(1f))
                HeroStat("Status",       "Active", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f)),
        elevation= CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                color = PureWhite.copy(alpha = 0.65f), fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black, letterSpacing = (-0.3).sp
                ),
                color = PureWhite
            )
        }
    }
}
