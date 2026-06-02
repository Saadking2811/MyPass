package com.example.myapplication.ui.checkin

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.ScreenScaffold
import com.example.myapplication.ui.theme.CrimsonRed
import com.example.myapplication.ui.theme.DarkGreen
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.MintGreen
import com.example.myapplication.ui.theme.StatusInfo
import com.example.myapplication.viewmodel.AppUiState

/** Step 5/6 — count checked / carry-on / oversized bags. */
@Composable
fun BaggageScreen(
    state: AppUiState, onUpdate: (Int, Int, Int) -> Unit,
    onNext: () -> Unit, onBack: () -> Unit
) {
    var checked   by remember { mutableIntStateOf(state.draft?.baggageDeclaration?.checkedBags ?: 0) }
    var carryOn   by remember { mutableIntStateOf(state.draft?.baggageDeclaration?.carryOnBags ?: 1) }
    var oversized by remember { mutableIntStateOf(state.draft?.baggageDeclaration?.oversizedBags ?: 0) }

    ScreenScaffold(
        title = "Baggage", subtitle = "Step 5 of 6", onBack = onBack,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    PrimaryButton("Save & continue", onClick = { onUpdate(checked, carryOn, oversized); onNext() })
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BagRow("Checked bags",  "23 kg each",     Icons.Filled.Luggage, EmeraldGreen, checked)   { checked = it.coerceAtLeast(0) }
            BagRow("Carry-on bags", "7 kg each",      Icons.Filled.Luggage, StatusInfo,   carryOn)   { carryOn = it.coerceAtLeast(0) }
            BagRow("Oversized",     "Special items",  Icons.Filled.Luggage, CrimsonRed,   oversized) { oversized = it.coerceAtLeast(0) }
            Spacer(modifier = Modifier.height(8.dp))
            val totalKg = checked * 23.0 + carryOn * 7.0
            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MintGreen),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total estimated weight", style = MaterialTheme.typography.bodyMedium, color = DarkGreen)
                    Text(
                        "${"%.1f".format(totalKg)} kg",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black, color = EmeraldGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun BagRow(
    title: String, subtitle: String, icon: ImageVector, accent: Color,
    value: Int, onChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CounterBtn("−") { onChange(value - 1) }
                Text(
                    "$value", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black,
                    modifier = Modifier.width(24.dp), textAlign = TextAlign.Center
                )
                CounterBtn("+") { onChange(value + 1) }
            }
        }
    }
}

@Composable
private fun CounterBtn(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(32.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
