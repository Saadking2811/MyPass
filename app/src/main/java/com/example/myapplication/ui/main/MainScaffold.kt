package com.example.myapplication.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.explore.ExploreTab
import com.example.myapplication.ui.home.HomeTab
import com.example.myapplication.ui.profile.ProfileTab
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.PureWhite
import com.example.myapplication.ui.trips.TripsTab
import com.example.myapplication.viewmodel.AppUiState
import com.example.myapplication.viewmodel.AppViewModel

/** Bottom-nav host for the four main tabs + central Check-in launcher. */
@Composable
fun MainScaffold(
    state: AppUiState,
    vm: AppViewModel,
    s: (String) -> String,
    onStartCheckIn: () -> Unit,
    onOpenPreferences: () -> Unit,
    onLogout: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    LuxNavItem(selected = tab == 0, icon = Icons.Filled.Home,
                        label = "Home", onClick = { tab = 0 })
                    LuxNavItem(selected = tab == 1, icon = Icons.Filled.ConfirmationNumber,
                        label = "Trips", onClick = { tab = 1 })
                    NavigationBarItem(
                        selected = false,
                        onClick = onStartCheckIn,
                        icon = {
                            Box(
                                modifier = Modifier.size(44.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.QrCodeScanner, null,
                                    tint = PureWhite, modifier = Modifier.size(20.dp))
                            }
                        },
                        label = {
                            Text("Check-in",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium)
                        }
                    )
                    LuxNavItem(selected = tab == 2, icon = Icons.Outlined.Place,
                        label = "Explore", onClick = { tab = 2 })
                    LuxNavItem(selected = tab == 3, icon = Icons.Filled.Person,
                        label = "Profile", onClick = { tab = 3 })
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> HomeTab(state, vm, s, onStartCheckIn = onStartCheckIn)
                1 -> TripsTab(state, s)
                2 -> ExploreTab()
                else -> ProfileTab(state, s, onOpenPreferences, onLogout)
            }
        }
    }
}

/** Standard bottom-nav tab — emerald accent when selected, no pill indicator. */
@Composable
private fun RowScope.LuxNavItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, null, modifier = Modifier.size(22.dp)) },
        label = {
            Text(label,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor   = EmeraldGreen,
            selectedTextColor   = EmeraldGreen,
            indicatorColor      = Color.Transparent,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
