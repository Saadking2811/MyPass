package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Seat
import com.example.myapplication.ui.AppUiState
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

@Composable
fun SeatSelectionScreen(
    state: AppUiState,
    onSeatSelected: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val selectedSeat = state.draft?.selectedSeat

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = "Choose Your Seat",
            subtitle = state.draft?.itinerary?.let { "${it.origin} → ${it.destination} • ${it.aircraftType}" } ?: ""
        )

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SeatLegend(SeatAvailable, "Available")
            SeatLegend(SeatPremium, "Premium")
            SeatLegend(SeatExtraLeg, "Extra Leg")
            SeatLegend(SeatOccupied, "Occupied")
            SeatLegend(SeatSelected, "Selected")
        }

        // Aircraft grid
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NeutralGray50),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Column headers
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("A", "B", "C", "", "D", "E", "F").forEach { label ->
                        Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                            if (label.isNotEmpty()) {
                                Text(label, style = MaterialTheme.typography.labelMedium, color = NeutralGray500, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Seat grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.seatMap) { seat ->
                        // Insert aisle gap after column C
                        if (seat.column == "D") {
                            // The grid cell before D would be the aisle
                        }
                        SeatItem(
                            seat = seat,
                            isSelected = seat.seatCode == selectedSeat,
                            onClick = { if (!seat.occupied) onSeatSelected(seat.seatCode) }
                        )
                    }
                }
            }
        }

        // Selected seat info
        AnimatedVisibility(
            visible = selectedSeat != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val seat = state.seatMap.find { it.seatCode == selectedSeat }
            if (seat != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SkyBlueSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(SeatSelected),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(selectedSeat ?: "", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Seat ${selectedSeat}", fontWeight = FontWeight.SemiBold)
                                Text(
                                    buildString {
                                        if (seat.window) append("Window")
                                        if (seat.aisle) append("Aisle")
                                        if (seat.premium) append(" • Premium")
                                        if (seat.extraLegroom) append(" • Extra Legroom")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralGray600
                                )
                            }
                        }
                        if (seat.price > 0) {
                            Text(
                                "+${seat.price.toInt()} DZD",
                                style = MaterialTheme.typography.labelLarge,
                                color = CoralOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

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
                text = "Confirm Seat",
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = !selectedSeat.isNullOrBlank(),
                icon = Icons.Filled.EventSeat
            )
        }
    }
}

@Composable
private fun SeatItem(seat: Seat, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "seatScale"
    )

    val bgColor = when {
        seat.occupied -> SeatOccupied
        isSelected -> SeatSelected
        seat.premium -> SeatPremium
        seat.extraLegroom -> SeatExtraLeg
        else -> SeatAvailable
    }

    val textColor = when {
        isSelected -> Color.White
        seat.occupied -> NeutralGray500
        seat.premium -> NeutralGray800
        else -> NeutralGray700
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(
                if (isSelected) Modifier.border(2.dp, SkyBlueDark, RoundedCornerShape(8.dp))
                else Modifier
            )
            .clickable(enabled = !seat.occupied, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (seat.occupied) {
            Icon(Icons.Filled.Close, null, tint = NeutralGray400, modifier = Modifier.size(14.dp))
        } else {
            Text(
                seat.seatCode,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun SeatLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = NeutralGray600, fontSize = 10.sp)
    }
}
