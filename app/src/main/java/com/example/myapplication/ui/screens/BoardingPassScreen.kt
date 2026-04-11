package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.BoardingPass
import com.example.myapplication.ui.AppUiState
import com.example.myapplication.ui.AppViewModel
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun BoardingPassScreen(
    state: AppUiState,
    vm: AppViewModel,
    onNewCheckIn: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pass = state.latestBoardingPass ?: state.cachedBoardingPasses.firstOrNull()
    var isSavingPdf by remember { mutableStateOf(false) }

    if (pass == null) {
        EmptyBoardingPassState(onHome = onHome)
        return
    }

    val qrBitmap = remember(pass.qrPayload) { vm.generateQrBitmap(pass.qrPayload) }

    // Success confetti animation
    val infiniteTransition = rememberInfiniteTransition(label = "celebrate")
    val celebrateScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "celebrateScale"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Success header
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer { scaleX = celebrateScale; scaleY = celebrateScale }
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(SuccessGreen.copy(alpha = 0.2f), SuccessGreen.copy(alpha = 0.05f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Check-In Complete!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
                Text(
                    "Your boarding pass is ready",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralGray600
                )
            }
        }

        // Boarding pass card
        item {
            BoardingPassCard(pass = pass, qrBitmap = qrBitmap)
        }

        // Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GradientButton(
                    text = if (isSavingPdf) "Generating..." else "Download PDF",
                    onClick = {
                        isSavingPdf = true
                        scope.launch {
                            runCatching { vm.saveBoardingPassPdf(context, pass) }
                                .onSuccess { fileName -> vm.showMessage("Saved as $fileName") }
                                .onFailure { vm.showMessage("PDF generation failed.") }
                            isSavingPdf = false
                        }
                    },
                    enabled = !isSavingPdf,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Download
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryButton(
                    text = "New Check-In",
                    onClick = onNewCheckIn,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.FlightTakeoff
                )
                SecondaryButton(
                    text = "Home",
                    onClick = onHome,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Home
                )
            }
        }

        // Other saved passes
        if (state.cachedBoardingPasses.size > 1) {
            item {
                SectionHeader(
                    title = "Other Passes",
                    subtitle = "${state.cachedBoardingPasses.size - 1} more"
                )
            }
            state.cachedBoardingPasses.filter { it.id != pass.id }.forEach { otherPass ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.AirplaneTicket, null, tint = SkyBlue)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("${otherPass.origin} → ${otherPass.destination}", fontWeight = FontWeight.Medium)
                                Text("${otherPass.flightNumber} • Seat ${otherPass.seat}", style = MaterialTheme.typography.bodySmall, color = NeutralGray600)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardingPassCard(pass: BoardingPass, qrBitmap: android.graphics.Bitmap) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            // Blue header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(SkyBlue, Color(0xFF00C6FB)))
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("BOARDING PASS", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
                        StatusChip(text = pass.status, color = Color.White, icon = Icons.Filled.CheckCircle)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(pass.airlineName, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(pass.flightNumber, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                }
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Route display
                FlightRouteDisplay(
                    origin = pass.origin,
                    originCity = pass.originCity,
                    destination = pass.destination,
                    destinationCity = pass.destinationCity,
                    duration = ""
                )

                HorizontalDivider(color = NeutralGray200)

                // Details grid
                Row(modifier = Modifier.fillMaxWidth()) {
                    PassField("PASSENGER", pass.passengerName, Modifier.weight(1f))
                    PassField("BOOKING", pass.bookingReference, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    PassField("SEAT", pass.seat, Modifier.weight(1f))
                    PassField("CLASS", pass.seatClass, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    PassField("GATE", pass.gate, Modifier.weight(1f))
                    PassField("TERMINAL", pass.terminal, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    PassField("DEPARTURE", pass.departureTime, Modifier.weight(1f))
                    PassField("ARRIVAL", pass.arrivalTime, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    PassField("BOARDING", pass.boardingGroup, Modifier.weight(1f))
                    PassField("SEQUENCE", pass.sequence, Modifier.weight(1f))
                }

                if (pass.baggageInfo.isNotBlank()) {
                    PassField("BAGGAGE", pass.baggageInfo, Modifier.fillMaxWidth())
                }

                HorizontalDivider(color = NeutralGray200)

                // QR Code
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NeutralGray50),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Boarding pass QR code",
                                modifier = Modifier.size(200.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Show this QR code at the gate",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralGray500
                    )
                }
            }
        }
    }
}

@Composable
private fun PassField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = NeutralGray500, fontSize = 10.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyBoardingPassState(onHome: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.AirplaneTicket,
                contentDescription = null,
                tint = NeutralGray400,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No Boarding Pass",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = NeutralGray700
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Complete the check-in process to receive your digital boarding pass.",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralGray500,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            GradientButton(text = "Go Home", onClick = onHome, icon = Icons.Filled.Home)
        }
    }
}
