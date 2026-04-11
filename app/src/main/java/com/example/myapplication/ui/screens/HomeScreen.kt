package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.BoardingPass
import com.example.myapplication.model.FlightItinerary
import com.example.myapplication.ui.AppUiState
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    state: AppUiState,
    onLookup: (String, String) -> Unit,
    onSync: () -> Unit,
    onContinue: () -> Unit,
    onBoardingPassClick: (BoardingPass) -> Unit,
    onLogout: () -> Unit
) {
    var bookingRef by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top header gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(SkyBlue, Color(0xFF00C6FB))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Hello, ${state.currentUser?.fullName?.split(" ")?.firstOrNull() ?: "Traveller"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            "Ready for your next flight?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Connectivity chip
                        StatusChip(
                            text = if (state.isOnline) "Online" else "Offline",
                            color = if (state.isOnline) SuccessGreen else ErrorRed,
                            icon = if (state.isOnline) Icons.Filled.Wifi else Icons.Filled.WifiOff
                        )
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Outlined.Logout, "Logout", tint = Color.White)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Flight lookup card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Find Your Booking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }

                        AppTextField(
                            value = bookingRef,
                            onValueChange = { bookingRef = it.uppercase() },
                            label = "Booking Reference (e.g. AB12CD)",
                            leadingIcon = Icons.Outlined.ConfirmationNumber
                        )

                        AppTextField(
                            value = lastName,
                            onValueChange = { lastName = it.uppercase() },
                            label = "Last Name",
                            leadingIcon = Icons.Outlined.Person
                        )

                        // Demo quick-fill chips
                        Text(
                            "Test data (tap to fill):",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeutralGray600
                        )
                        val demoFlights = listOf(
                            "AB12CD" to "DOE",
                            "ZX98MN" to "SMITH",
                            "KL45PQ" to "BENALI",
                            "MN77RS" to "HADJ"
                        )
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            demoFlights.forEach { (ref, name) ->
                                SuggestionChip(
                                    onClick = {
                                        bookingRef = ref
                                        lastName = name
                                        onLookup(ref, name)
                                    },
                                    label = {
                                        Text(
                                            "$ref / $name",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    icon = {
                                        Icon(
                                            Icons.Filled.FlightTakeoff,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }

                        GradientButton(
                            text = if (state.isLoading) "Searching..." else "Retrieve Booking",
                            onClick = { onLookup(bookingRef, lastName) },
                            enabled = !state.isLoading && bookingRef.isNotBlank() && lastName.isNotBlank(),
                            icon = Icons.Filled.FlightTakeoff
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }

        // Content below header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Flight result
            AnimatedVisibility(
                visible = state.lookupResult != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                state.lookupResult?.let { flight ->
                    FlightResultCard(flight = flight, onContinue = onContinue)
                }
            }

            // Sync button
            if (state.isSyncing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = SkyBlue
                    )
                    Text("Syncing...", style = MaterialTheme.typography.bodySmall, color = NeutralGray600)
                }
            } else if (!state.isOnline) {
                SecondaryButton(
                    text = "Retry Sync",
                    onClick = onSync,
                    icon = Icons.Filled.Sync
                )
            }

            // Quick actions
            SectionHeader(title = "Quick Actions")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Filled.FlightTakeoff,
                    label = "Check In",
                    color = SkyBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { /* Already on this screen */ }
                )
                QuickActionCard(
                    icon = Icons.Filled.Sync,
                    label = "Sync Data",
                    color = CoralOrange,
                    modifier = Modifier.weight(1f),
                    onClick = onSync
                )
                QuickActionCard(
                    icon = Icons.Filled.AirplaneTicket,
                    label = "My Passes",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { isExpanded = !isExpanded }
                )
            }

            // Cached boarding passes
            AnimatedVisibility(
                visible = state.cachedBoardingPasses.isNotEmpty() && isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(
                        title = "Your Boarding Passes",
                        subtitle = "${state.cachedBoardingPasses.size} saved passes"
                    )
                    state.cachedBoardingPasses.forEach { pass ->
                        SavedBoardingPassCard(pass = pass, onClick = { onBoardingPassClick(pass) })
                    }
                }
            }

            // Cached flights
            if (state.cachedFlights.isNotEmpty()) {
                SectionHeader(
                    title = "Recent Flights",
                    subtitle = "${state.cachedFlights.size} flights found"
                )
                state.cachedFlights.forEach { flight ->
                    CachedFlightCard(flight = flight)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FlightResultCard(flight: FlightItinerary, onContinue: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(flight.airlineName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(flight.flightNumber, style = MaterialTheme.typography.bodySmall, color = NeutralGray600)
                }
                StatusChip(
                    text = flight.checkInStatus,
                    color = if (flight.checkInOpen) SuccessGreen else WarningAmber,
                    icon = if (flight.checkInOpen) Icons.Filled.CheckCircle else Icons.Filled.Schedule
                )
            }

            FlightRouteDisplay(
                origin = flight.origin,
                originCity = flight.originCity,
                destination = flight.destination,
                destinationCity = flight.destinationCity,
                duration = flight.duration
            )

            HorizontalDivider(color = NeutralGray200)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FlightDetailItem("DEPARTURE", flight.departureTime)
                FlightDetailItem("GATE", flight.gate)
                FlightDetailItem("TERMINAL", flight.terminal)
                FlightDetailItem("CLASS", flight.seatClass)
            }

            GradientButton(
                text = "Start Check-In",
                onClick = onContinue,
                icon = Icons.Filled.ArrowForward,
                gradientColors = listOf(CoralOrange, CoralOrangeLight)
            )
        }
    }
}

@Composable
private fun FlightDetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = NeutralGray500, fontSize = 10.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SavedBoardingPassCard(pass: BoardingPass, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SkyBlueSubtle)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(SkyBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AirplaneTicket, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("${pass.origin} → ${pass.destination}", fontWeight = FontWeight.SemiBold)
                    Text("${pass.flightNumber} • Seat ${pass.seat}", style = MaterialTheme.typography.bodySmall, color = NeutralGray600)
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = NeutralGray500)
        }
    }
}

@Composable
private fun CachedFlightCard(flight: FlightItinerary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Flight, null, tint = SkyBlue, modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = 45f })
                Column {
                    Text("${flight.origin} → ${flight.destination}", fontWeight = FontWeight.Medium)
                    Text(flight.departureTime, style = MaterialTheme.typography.bodySmall, color = NeutralGray600)
                }
            }
            StatusChip(text = flight.checkInStatus, color = NeutralGray600)
        }
    }
}
