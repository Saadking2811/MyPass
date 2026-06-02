package com.example.myapplication.ui.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.BoardingPass
import com.example.myapplication.ui.components.GoldAccent
import com.example.myapplication.ui.home.FlightSummaryCard
import com.example.myapplication.ui.home.StatusPill
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.StatusSuccess
import com.example.myapplication.viewmodel.AppUiState

/** Bottom-nav "Trips" tab — switches between cached flights and boarding passes. */
@Composable
fun TripsTab(
    state: AppUiState,
    s: (String) -> String,
    onViewPass: (BoardingPass) -> Unit = {}
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
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
                    "Your trips",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black, letterSpacing = (-0.6).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Flights & boarding passes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TripTab("Flights", tab == 0) { tab = 0 }
            TripTab("Boarding passes", tab == 1) { tab = 1 }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (tab == 0) {
                if (state.cachedFlights.isEmpty()) {
                    item { EmptyTripsCard(Icons.Filled.FlightTakeoff, "No flights yet", "Start a check-in to add flights here.") }
                } else {
                    items(state.cachedFlights) { flight -> FlightSummaryCard(flight, s) }
                }
            } else {
                if (state.cachedBoardingPasses.isEmpty()) {
                    item { EmptyTripsCard(Icons.Filled.QrCode2, "No boarding passes yet", "Complete a check-in to receive your boarding pass.") }
                } else {
                    items(state.cachedBoardingPasses) { pass ->
                        BoardingPassListItem(pass, onClick = { onViewPass(pass) })
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun RowScope.TripTab(title: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f).height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun BoardingPassListItem(pass: BoardingPass, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.QrCode2, null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(pass.flightNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(pass.airlineName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                StatusPill(pass.status, StatusSuccess)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(pass.origin, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(pass.originCity, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(pass.destination, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(pass.destinationCity, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LabeledValue("Seat", pass.seat)
                LabeledValue("Gate", pass.gate)
                LabeledValue("Boarding", pass.boardingGroup)
                LabeledValue("Date", pass.issuedAt.take(10))
            }
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun EmptyTripsCard(icon: ImageVector, title: String, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation= CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = EmeraldGreen, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
        }
    }
}
