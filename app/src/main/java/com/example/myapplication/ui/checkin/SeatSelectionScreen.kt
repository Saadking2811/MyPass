package com.example.myapplication.ui.checkin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Seat
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.ScreenScaffold
import com.example.myapplication.ui.theme.DeepGold
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.PureWhite
import com.example.myapplication.ui.theme.SeatAvailable
import com.example.myapplication.ui.theme.SeatOccupied
import com.example.myapplication.ui.theme.SeatPremium
import com.example.myapplication.viewmodel.AppUiState

/** Step 4/6 — interactive seat map. */
@Composable
fun SeatSelectionScreen(
    state: AppUiState, onSeatSelected: (String) -> Unit,
    onNext: () -> Unit, onBack: () -> Unit
) {
    val selectedSeat = state.draft?.selectedSeat
    ScreenScaffold(
        title = "Choose your seat", subtitle = "Step 4 of 6", onBack = onBack,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    if (selectedSeat != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Selected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(selectedSeat, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = EmeraldGreen)
                            }
                            Text("Standard seat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    PrimaryButton("Confirm seat", onClick = onNext, enabled = !selectedSeat.isNullOrBlank())
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LegendItem("Available", SeatAvailable)
                LegendItem("Premium",   SeatPremium)
                LegendItem("Selected",  EmeraldGreen)
                LegendItem("Taken",     SeatOccupied)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).heightIn(min = 480.dp),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation= CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 6.dp)) {
                        listOf("A", "B", "C", "", "D", "E", "F").forEach { l ->
                            if (l.isEmpty()) Spacer(modifier = Modifier.width(20.dp))
                            else Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(l, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val seats = state.seatMap
                        val rows = seats.groupBy { it.row }.toSortedMap()
                        items(rows.entries.toList()) { (rowNum, rowSeats) ->
                            val sortedSeats = rowSeats.sortedBy { it.column }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                sortedSeats.forEachIndexed { i, seat ->
                                    if (i == 3) {
                                        Text("$rowNum", style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                                    }
                                    SeatChip(
                                        seat = seat,
                                        isSelected = seat.seatCode == selectedSeat,
                                        onClick = { if (!seat.occupied) onSeatSelected(seat.seatCode) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(modifier = Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SeatChip(
    seat: Seat,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "ss")
    val bg = when {
        seat.occupied -> SeatOccupied
        isSelected    -> EmeraldGreen
        seat.premium  -> SeatPremium
        else          -> SeatAvailable
    }
    Box(
        modifier = modifier
            .heightIn(min = 36.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(enabled = !seat.occupied, onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            seat.seatCode.last().toString(),
            style = MaterialTheme.typography.labelMedium,
            color = when {
                seat.occupied -> MaterialTheme.colorScheme.onSurfaceVariant
                isSelected -> PureWhite
                seat.premium -> DeepGold
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
