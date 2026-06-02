package com.example.myapplication.ui.checkin

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.BoardingPass
import com.example.myapplication.ui.components.BrandLogo
import com.example.myapplication.ui.components.ScreenScaffold
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.PureWhite
import com.example.myapplication.viewmodel.AppUiState
import com.example.myapplication.viewmodel.AppViewModel
import kotlinx.coroutines.launch

/** Final screen — shows the rendered boarding pass card + Add to Wallet (PDF share). */
@Composable
fun BoardingPassScreen(
    state: AppUiState, vm: AppViewModel, s: (String) -> String,
    onNewLookup: () -> Unit, onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val pass    = state.latestBoardingPass ?: state.cachedBoardingPasses.firstOrNull()

    if (pass == null) {
        ScreenScaffold(title = "Boarding pass", onBack = onBack) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (state.isLoading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = EmeraldGreen, strokeWidth = 2.5.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Generating boarding pass...")
                    }
                } else { Text("No boarding pass yet.") }
            }
        }
        return
    }

    val qrBitmap = remember(pass.qrPayload) { vm.generateQrBitmap(pass.qrPayload) }

    ScreenScaffold(
        title = "Boarding pass", subtitle = "Confirmed", onBack = onBack,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick  = onNewLookup,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape    = RoundedCornerShape(14.dp),
                        border   = BorderStroke(1.dp, EmeraldGreen.copy(0.5f)),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text(
                            "Done",
                            style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 0.5.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    vm.shareBoardingPass(context, pass)
                                    vm.showMessage("Boarding pass saved to Downloads")
                                } catch (e: Exception) {
                                    vm.showMessage("Failed: ${e.message}")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = PureWhite),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(Icons.Filled.AccountBalanceWallet, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add to Wallet",
                            style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 0.2.sp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(0.5.dp, EmeraldGreen.copy(0.30f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(EmeraldGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, tint = PureWhite, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Check-in complete",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleSmall.copy(letterSpacing = (-0.2).sp)
                            )
                            Text(
                                "Your boarding pass is ready",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.8f)
                            )
                        }
                    }
                }
            }

            item { CleanBoardingPassCard(pass, qrBitmap = qrBitmap) }
        }
    }
}

@Composable
private fun CleanBoardingPassCard(pass: BoardingPass, qrBitmap: Bitmap) {
    val charcoal = Color(0xFF111827)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = PureWhite),
        elevation= CardDefaults.cardElevation(2.dp),
        border   = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(EmeraldGreen)
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "AIR ALGÉRIE",
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                            color = PureWhite.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Boarding pass",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "FLIGHT",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                            color = PureWhite.copy(alpha = 0.7f)
                        )
                        Text(
                            pass.flightNumber,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = PureWhite
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 26.dp, vertical = 24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            pass.origin,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black, letterSpacing = (-2).sp, fontSize = 42.sp
                            ),
                            color = EmeraldGreen
                        )
                        Text(
                            pass.originCity.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.3.sp),
                            color = charcoal.copy(0.65f), fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(pass.departureTime.takeLast(5), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = charcoal)
                    }
                    Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Icon(Icons.Filled.FlightTakeoff, null, tint = EmeraldGreen, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                            drawLine(
                                color = charcoal.copy(alpha = 0.2f),
                                start = Offset(8f, size.height / 2),
                                end   = Offset(size.width - 8f, size.height / 2),
                                strokeWidth = 0.8f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f))
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "DIRECT",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(
                            pass.destination,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black, letterSpacing = (-2).sp, fontSize = 42.sp
                            ),
                            color = EmeraldGreen
                        )
                        Text(
                            pass.destinationCity.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.3.sp),
                            color = charcoal.copy(0.65f), fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(pass.arrivalTime.takeLast(5), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = charcoal)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "PASSENGER",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold
                )
                Text(
                    pass.passengerName.uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
                    color = EmeraldGreen
                )

                Spacer(modifier = Modifier.height(22.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    LuxBlock("SEAT",     pass.seat,                       modifier = Modifier.weight(1f))
                    LuxDivider()
                    LuxBlock("GATE",     pass.gate,                       modifier = Modifier.weight(1f))
                    LuxDivider()
                    LuxBlock("GROUP",    pass.boardingGroup,              modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    LuxBlock("DEPART",   pass.departureTime.takeLast(5),  modifier = Modifier.weight(1f))
                    LuxDivider()
                    LuxBlock("ARRIVAL",  pass.arrivalTime.takeLast(5),    modifier = Modifier.weight(1f))
                    LuxDivider()
                    LuxBlock("TERMINAL", pass.terminal.ifBlank { "—" },   modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    LuxBlock("CLASS",    pass.seatClass,                  modifier = Modifier.weight(1f))
                    LuxDivider()
                    LuxBlock("SEQUENCE", pass.sequence,                   modifier = Modifier.weight(1f))
                    LuxDivider()
                    LuxBlock("BAGGAGE",  pass.baggageInfo.take(8).ifBlank { "—" }, modifier = Modifier.weight(1f))
                }
            }

            Canvas(modifier = Modifier.fillMaxWidth().height(22.dp)) {
                val y = size.height / 2
                drawLine(
                    charcoal.copy(0.18f),
                    Offset(28f, y), Offset(size.width - 28f, y),
                    1.0f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )
            }

            Column(modifier = Modifier.padding(horizontal = 26.dp).padding(top = 4.dp, bottom = 24.dp)) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(196.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PureWhite)
                                .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Boarding QR",
                                modifier = Modifier.fillMaxSize().padding(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Scan at gate",
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                            color = charcoal.copy(0.6f), fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BrandLogo(modifier = Modifier.size(18.dp), cornerRadius = 4)
                        Text(
                            "AIR ALGÉRIE",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
                            color = charcoal.copy(0.6f), fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        pass.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                        color = EmeraldGreen, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            .background(EmeraldGreen.copy(alpha = 0.10f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LuxBlock(label: String, value: String, modifier: Modifier = Modifier) {
    val charcoal = Color(0xFF111827)
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(
            label, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp),
            color = charcoal.copy(alpha = 0.55f), fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.3).sp),
            color = EmeraldGreen
        )
    }
}

@Composable
private fun LuxDivider() {
    Box(
        modifier = Modifier
            .width(0.5.dp)
            .height(36.dp)
            .background(Color(0xFFE5E7EB))
            .padding(vertical = 4.dp)
    )
}
