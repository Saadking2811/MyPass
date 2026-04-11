package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
fun BaggageScreen(
    state: AppUiState,
    onUpdate: (Int, Int, Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var checked by remember { mutableIntStateOf(state.draft?.baggageDeclaration?.checkedBags ?: 0) }
    var carryOn by remember { mutableIntStateOf(state.draft?.baggageDeclaration?.carryOnBags ?: 1) }
    var oversized by remember { mutableIntStateOf(state.draft?.baggageDeclaration?.oversizedBags ?: 0) }

    val totalWeight = checked * 23.0 + carryOn * 7.0
    val extraFee = if (checked > 1) (checked - 1) * 5000.0 else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = "Baggage",
            subtitle = "Manage your luggage for this flight"
        )

        // Weight summary card
        InfoCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedCounter(targetValue = (checked + carryOn + oversized))
                    Text("Total Bags", style = MaterialTheme.typography.labelSmall, color = NeutralGray600)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedCounter(targetValue = totalWeight.toInt())
                    Text("Total kg", style = MaterialTheme.typography.labelSmall, color = NeutralGray600)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (extraFee > 0) "${extraFee.toInt()} DZD" else "Free",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (extraFee > 0) CoralOrange else SuccessGreen
                    )
                    Text("Extra Fee", style = MaterialTheme.typography.labelSmall, color = NeutralGray600)
                }
            }
        }

        // Baggage counters
        CounterControl(
            label = "Checked Bags",
            value = checked,
            onIncrement = { checked++ },
            onDecrement = { checked-- },
            icon = Icons.Outlined.Luggage,
            maxValue = 5
        )

        // Info about checked bags
        AnimatedVisibility(visible = checked > 0, enter = fadeIn() + expandVertically()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SkyBlueSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Info, null, tint = SkyBlue, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Each checked bag: max 23 kg. First bag included free.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkyBlueDark
                    )
                }
            }
        }

        CounterControl(
            label = "Carry-On Bags",
            value = carryOn,
            onIncrement = { carryOn++ },
            onDecrement = { carryOn-- },
            icon = Icons.Outlined.ShoppingBag,
            maxValue = 2
        )

        CounterControl(
            label = "Oversized Items",
            value = oversized,
            onIncrement = { oversized++ },
            onDecrement = { oversized-- },
            icon = Icons.Outlined.Fitbit,
            maxValue = 3
        )

        // Oversized info
        AnimatedVisibility(visible = oversized > 0, enter = fadeIn() + expandVertically()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WarningAmberLight)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Oversized items (sports equipment, etc.) require special handling.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralGray700
                    )
                }
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
                text = "Continue",
                onClick = {
                    onUpdate(checked, carryOn, oversized)
                    onNext()
                },
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.ArrowForward
            )
        }
    }
}
