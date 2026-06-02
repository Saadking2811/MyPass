package com.example.myapplication.ui.checkin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ocr.MrzParser
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.ScreenScaffold
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.MintGreen
import com.example.myapplication.viewmodel.AppUiState

/** Step 3/6 — review flight + parsed passport info before continuing. */
@Composable
fun DetailsReviewScreen(state: AppUiState, onNext: () -> Unit, onBack: () -> Unit) {
    val draft = state.draft ?: return
    ScreenScaffold(
        title = "Review details", subtitle = "Step 3 of 6", onBack = onBack,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    PrimaryButton("Continue to seat selection", onClick = onNext)
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Flight information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DetailRow("Passenger", draft.itinerary.passengerName)
                    DetailRow("Flight",    "${draft.itinerary.flightNumber} (${draft.itinerary.airlineName})")
                    DetailRow("Route",     "${draft.itinerary.origin} → ${draft.itinerary.destination}")
                    DetailRow("Departure", draft.itinerary.departureTime)
                    DetailRow("Aircraft",  draft.itinerary.aircraftType)
                }
            }
            draft.passportInfo?.let { passport ->
                Card(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MintGreen),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.CheckCircle, null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                            Text("Passport verified", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                        HorizontalDivider(color = EmeraldGreen.copy(0.2f))
                        DetailRow("Name",         passport.fullName)
                        DetailRow("Passport no.", passport.passportNumber)
                        DetailRow("Nationality",  MrzParser.getCountryName(passport.nationality))
                        DetailRow("Birth",        passport.dateOfBirth)
                        DetailRow("Expiry",       passport.expiryDate)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
