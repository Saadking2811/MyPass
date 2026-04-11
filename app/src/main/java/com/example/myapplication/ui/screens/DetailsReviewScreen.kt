package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.AppUiState
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

@Composable
fun DetailsReviewScreen(
    state: AppUiState,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val draft = state.draft ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = "Review Details",
            subtitle = "Confirm your information before seat selection"
        )

        // Flight info card
        InfoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Flight, null, tint = SkyBlue, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Flight Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            HorizontalDivider(color = NeutralGray200)
            FlightRouteDisplay(
                origin = draft.itinerary.origin,
                originCity = draft.itinerary.originCity,
                destination = draft.itinerary.destination,
                destinationCity = draft.itinerary.destinationCity,
                duration = draft.itinerary.duration
            )
            HorizontalDivider(color = NeutralGray200)
            ReviewField("Airline", draft.itinerary.airlineName)
            ReviewField("Flight Number", draft.itinerary.flightNumber)
            ReviewField("Booking Reference", draft.itinerary.bookingReference)
            ReviewField("Departure", draft.itinerary.departureTime)
            ReviewField("Gate", draft.itinerary.gate)
            ReviewField("Terminal", draft.itinerary.terminal)
            ReviewField("Class", draft.itinerary.seatClass)
        }

        // Passenger info card
        InfoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Person, null, tint = CoralOrange, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Passenger Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            HorizontalDivider(color = NeutralGray200)
            ReviewField("Name", draft.itinerary.passengerName)
            draft.passportInfo?.let { passport ->
                ReviewField("Passport", passport.passportNumber)
                if (passport.nationality.isNotBlank()) ReviewField("Nationality", passport.nationality)
                if (passport.dateOfBirth.isNotBlank()) ReviewField("Date of Birth", passport.dateOfBirth)
                if (passport.gender.isNotBlank()) ReviewField("Gender", passport.gender)
                StatusChip(
                    text = if (passport.verified) "Passport Verified" else "Needs Verification",
                    color = if (passport.verified) SuccessGreen else WarningAmber,
                    icon = if (passport.verified) Icons.Filled.Verified else Icons.Filled.Warning
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.ArrowBack
            )
            GradientButton(
                text = "Select Seat",
                onClick = onNext,
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.AirlineSeatReclineNormal
            )
        }
    }
}

@Composable
private fun ReviewField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = NeutralGray600)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
