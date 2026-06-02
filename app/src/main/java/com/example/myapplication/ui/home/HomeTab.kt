package com.example.myapplication.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.model.BoardingPass
import com.example.myapplication.model.FlightItinerary
import com.example.myapplication.ui.components.BrandLogo
import com.example.myapplication.ui.components.GoldAccent
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.theme.CharcoalGray
import com.example.myapplication.ui.theme.CrimsonRed
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.PureWhite
import com.example.myapplication.ui.theme.SeatOccupied
import com.example.myapplication.ui.theme.StatusInfo
import com.example.myapplication.ui.theme.StatusSuccess
import com.example.myapplication.ui.theme.StatusWarning
import com.example.myapplication.ui.util.Destination
import com.example.myapplication.ui.util.Img
import com.example.myapplication.ui.util.popularDestinations
import com.example.myapplication.viewmodel.AppUiState
import com.example.myapplication.viewmodel.AppViewModel

/** Bottom-nav "Home" tab — compact emerald hero, search, next trip and inspiration. */
@Composable
fun HomeTab(
    state: AppUiState, vm: AppViewModel, s: (String) -> String,
    onStartCheckIn: () -> Unit,
    onViewPass: (BoardingPass) -> Unit = {}
) {
    val user = state.currentUser
    val ctx = LocalContext.current
    var bookingRef by rememberSaveable { mutableStateOf("") }
    var lastName   by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── 1. Compact emerald hero — fully filled, no decorative shapes ──
        item {
            EmeraldHeroSection(
                fullName = user?.fullName,
                isOnline = state.isOnline
            )
        }

        // ── 2. Search card overlapping the hero (premium airline app pattern) ──
        item {
            Box(modifier = Modifier.offset(y = (-30).dp)) {
                SearchCard(
                    bookingRef = bookingRef,
                    lastName   = lastName,
                    onBookingRefChange = { bookingRef = it.uppercase() },
                    onLastNameChange   = { lastName = it.uppercase() },
                    onSubmit = {
                        if (bookingRef.isBlank() || lastName.isBlank()) {
                            bookingRef = "NM2025A"; lastName = "NAMOUNE"
                            vm.lookupFlight("NM2025A", "NAMOUNE")
                        } else {
                            vm.lookupFlight(bookingRef, lastName)
                        }
                        onStartCheckIn()
                    }
                )
            }
        }

        // ── 3. Next trip card (Air France / Emirates style) OR Discover featured ──
        item {
            val nextFlight = state.cachedFlights.firstOrNull()
            if (nextFlight != null) {
                NextTripCard(flight = nextFlight, onCheckIn = onStartCheckIn)
            } else {
                FeaturedDiscoveryCard()
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // ── 4. Quick chips ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickChip(Icons.Filled.QrCodeScanner, "Check-in", EmeraldGreen, onStartCheckIn, modifier = Modifier.weight(1f))
                QuickChip(Icons.Filled.AccessTime, "Status", StatusInfo, {
                    val first = state.cachedFlights.firstOrNull()
                    if (first != null) {
                        vm.showMessage("${first.flightNumber} · ${first.checkInStatus} · Gate ${first.gate}, Term ${first.terminal}")
                    } else {
                        vm.showMessage("No flights yet — search a booking to see status.")
                    }
                }, modifier = Modifier.weight(1f))
                QuickChip(Icons.Filled.SupportAgent, "Support", CharcoalGray, {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                        data = android.net.Uri.parse("mailto:support@mypass.dz")
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "MyPass support request")
                    }.apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
                    runCatching { ctx.startActivity(intent) }
                        .onFailure { vm.showMessage("No email app installed.") }
                }, modifier = Modifier.weight(1f))
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }

        // ── 5. Promo banner ──
        item { PromoBanner() }

        item { Spacer(modifier = Modifier.height(28.dp)) }

        // ── 6. Popular destinations ──
        item { SectionHeader("Popular destinations", "View all") {} }
        item { Spacer(modifier = Modifier.height(12.dp)) }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(popularDestinations) { dest -> DestinationCard(dest) }
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }

        // ── 7. Upcoming flights list (if any) ──
        if (state.cachedFlights.size > 1) {
            item { SectionHeader("Other upcoming flights", null) {} }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            items(state.cachedFlights.drop(1).take(3)) { flight ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    FlightSummaryCard(flight, s)
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // ── 8. Recent boarding passes ──
        if (state.cachedBoardingPasses.isNotEmpty()) {
            item { SectionHeader("Recent boarding passes", null) {} }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            items(state.cachedBoardingPasses.take(3)) { pass ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onViewPass(pass) },
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation= CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.QrCode2, null, tint = EmeraldGreen, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${pass.origin} → ${pass.destination}",
                                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${pass.flightNumber} • Seat ${pass.seat}",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusPill(pass.status, StatusSuccess)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        // ── 9. Travel inspiration ──
        item { SectionHeader("Travel inspiration", null) {} }
        item { Spacer(modifier = Modifier.height(12.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InspirationCard("Beach getaways", Img.INSPO_BEACH, modifier = Modifier.weight(1f))
                InspirationCard("Mountain escapes", Img.INSPO_MOUNTAIN, modifier = Modifier.weight(1f))
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }

        // ── 10. Footer ──
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    "MyPass · Every journey matters",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Version 1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HERO — pure emerald gradient, no decorative shapes, fills the screen width
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmeraldHeroSection(
    fullName: String?,
    isOnline: Boolean
) {
    val firstName = fullName?.split(" ")?.first() ?: "Traveler"
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
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 36.dp)) {
            // Top bar — brand mark, MyPass wordmark, notifications, avatar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrandLogo(modifier = Modifier.size(38.dp), cornerRadius = 10)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "MyPass",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black, letterSpacing = (-0.4).sp
                    ),
                    color = PureWhite
                )
                if (!isOnline) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(StatusWarning))
                        Text(
                            "Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f))
                ) {
                    Icon(Icons.Filled.NotificationsNone, null, tint = PureWhite, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp).clip(CircleShape)
                        .background(PureWhite)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        fullName?.take(1)?.uppercase() ?: "G",
                        fontWeight = FontWeight.Black, color = EmeraldGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Greeting block — dense, no empty space
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    "Hello, $firstName",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black, letterSpacing = (-0.6).sp
                    ),
                    color = PureWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Where are you heading next?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PureWhite.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subtle dashed flight path line at the bottom of the hero
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(2.dp)
            ) {
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

// ─────────────────────────────────────────────────────────────────────────────
//  SEARCH CARD — overlaps the hero
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchCard(
    bookingRef: String,
    lastName: String,
    onBookingRefChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(22.dp), spotColor = EmeraldGreen.copy(alpha = 0.35f)),
        shape     = RoundedCornerShape(22.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(EmeraldGreen))
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GoldAccent(modifier = Modifier.height(22.dp))
                    Column {
                        Text(
                            "Find your booking",
                            style = MaterialTheme.typography.titleMedium.copy(letterSpacing = (-0.2).sp),
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Enter reference + last name",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bookingRef, onValueChange = onBookingRefChange,
                    label = { Text("Booking reference") },
                    leadingIcon = { Icon(Icons.Filled.ConfirmationNumber, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = EmeraldGreen,
                        cursorColor = EmeraldGreen
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = lastName, onValueChange = onLastNameChange,
                    label = { Text("Last name") },
                    leadingIcon = { Icon(Icons.Filled.Person, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = EmeraldGreen,
                        cursorColor = EmeraldGreen
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    text = "Find Booking",
                    leadingIcon = Icons.Filled.Search,
                    onClick = onSubmit
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  NEXT TRIP CARD — Air France / Emirates style. Big, prominent, with route.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NextTripCard(flight: FlightItinerary, onCheckIn: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation= CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Header label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldGreen)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.FlightTakeoff, null, tint = PureWhite, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "NEXT TRIP",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.6.sp),
                    color = PureWhite,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    flight.flightNumber,
                    style = MaterialTheme.typography.titleMedium,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // Route — big IATA codes
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            flight.origin,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Black, letterSpacing = (-1.5).sp
                            ),
                            color = EmeraldGreen
                        )
                        Text(
                            flight.originCity.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            flight.departureTime.takeLast(5),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Center divider with plane
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.FlightTakeoff, null,
                            tint = EmeraldGreen, modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                            drawLine(
                                color = SeatOccupied,
                                start = Offset(8f, size.height / 2),
                                end   = Offset(size.width - 8f, size.height / 2),
                                strokeWidth = 1.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            flight.duration.ifBlank { "—" },
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(
                            flight.destination,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Black, letterSpacing = (-1.5).sp
                            ),
                            color = EmeraldGreen
                        )
                        Text(
                            flight.destinationCity.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            flight.arrivalTime.takeLast(5),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(14.dp))

                // 3 detail items: date / gate / terminal
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DetailMini("Date",     flight.departureTime.take(10))
                    DetailMini("Gate",     flight.gate.ifBlank { "—" })
                    DetailMini("Terminal", flight.terminal.ifBlank { "1" })
                }

                Spacer(modifier = Modifier.height(18.dp))

                PrimaryButton(
                    text = if (flight.checkInStatus == "Checked-In") "View boarding pass" else "Check in now",
                    leadingIcon = Icons.Filled.QrCodeScanner,
                    onClick = onCheckIn
                )
            }
        }
    }
}

@Composable
private fun DetailMini(label: String, value: String) {
    Column {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            color = EmeraldGreen
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  FEATURED DISCOVERY — shown when user has no upcoming flight
//  Big photographic card with title overlay, used by Skyscanner/Trip.com
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FeaturedDiscoveryCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(240.dp),
        shape    = RoundedCornerShape(20.dp),
        elevation= CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(Img.HERO_AIRPORT).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.85f)),
                    startY = 40f
                )
            ))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EmeraldGreen)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "DISCOVER",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp),
                        color = PureWhite, fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Your next adventure",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black, letterSpacing = (-0.6).sp
                    ),
                    color = PureWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Search a booking to start your journey",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PureWhite.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  PROMO BANNER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PromoBanner() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(180.dp),
        shape    = RoundedCornerShape(20.dp),
        elevation= CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(Img.PROMO_BANNER).crossfade(true).build(),
                contentDescription = "Promotion",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                    startY = 60f
                )
            ))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CrimsonRed)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "LIMITED TIME", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = PureWhite, fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Summer in Europe",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = PureWhite
                )
                Text(
                    "Save up to 25% on selected flights",
                    style = MaterialTheme.typography.bodySmall, color = PureWhite.copy(0.85f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HELPERS (chip, section title, destination tile, inspiration tile, …)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickChip(
    icon: ImageVector, label: String, accent: Color,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation= CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
internal fun SectionHeader(title: String, action: String?, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoldAccent(modifier = Modifier.height(18.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(letterSpacing = (-0.2).sp),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (action != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(
                    action,
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.3.sp),
                    color = EmeraldGreen, fontWeight = FontWeight.Bold
                )
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp), tint = EmeraldGreen)
            }
        }
    }
}

@Composable
private fun DestinationCard(dest: Destination) {
    Card(
        modifier  = Modifier.width(180.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(dest.imageUrl).crossfade(true).build(),
                    contentDescription = dest.city,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(0.55f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(dest.code, style = MaterialTheme.typography.labelSmall, color = PureWhite, fontWeight = FontWeight.Black)
                }
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                        .size(28.dp).clip(CircleShape).background(Color.White.copy(0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.FavoriteBorder, null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurface)
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(dest.city, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Place, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dest.country, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun InspirationCard(title: String, imageUrl: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        shape    = RoundedCornerShape(16.dp),
        elevation= CardDefaults.cardElevation(1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.6f)), startY = 30f)
            ))
            Text(
                title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                color = PureWhite, modifier = Modifier.align(Alignment.BottomStart).padding(14.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  FLIGHT SUMMARY CARD — also used by BookingLookupScreen and TripsTab
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FlightSummaryCard(
    flight: FlightItinerary,
    @Suppress("UNUSED_PARAMETER") s: (String) -> String
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.FlightTakeoff, null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text(flight.flightNumber, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(flight.airlineName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                StatusPill(
                    if (flight.checkInStatus == "Checked-In") "Checked in" else "Not checked in",
                    if (flight.checkInStatus == "Checked-In") StatusSuccess else StatusWarning
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        flight.origin,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(flight.originCity, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(flight.departureTime.takeLast(5), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(flight.duration, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Canvas(modifier = Modifier.fillMaxWidth().height(20.dp)) {
                        val y = size.height / 2
                        drawCircle(SeatOccupied, 4f, Offset(8f, y))
                        drawLine(
                            SeatOccupied, Offset(14f, y), Offset(size.width - 14f, y),
                            1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f))
                        )
                        drawCircle(EmeraldGreen, 4f, Offset(size.width - 8f, y))
                        drawCircle(EmeraldGreen, 5f, Offset(size.width / 2, y))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Direct", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        flight.destination,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(flight.destinationCity, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(flight.arrivalTime.takeLast(5), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                FlightDetailItem(Icons.Filled.CalendarMonth, flight.departureTime.take(10))
                FlightDetailItem(Icons.Filled.LocationOn, "Gate ${flight.gate}")
                FlightDetailItem(Icons.Filled.Apartment, "Term ${flight.terminal.ifBlank { "1" }}")
            }
        }
    }
}

@Composable
private fun FlightDetailItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatusPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}
