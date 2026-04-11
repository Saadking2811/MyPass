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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.SpecialRequests
import com.example.myapplication.ui.AppUiState
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpecialRequestsScreen(
    state: AppUiState,
    onUpdate: (SpecialRequests) -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    val existing = state.draft?.specialRequests ?: SpecialRequests()
    var diet by remember { mutableStateOf(existing.dietaryPreference) }
    var needsAssistance by remember { mutableStateOf(existing.needsAssistance) }
    var assistanceType by remember { mutableStateOf(existing.assistanceType) }
    var travelingWithInfant by remember { mutableStateOf(existing.travelingWithInfant) }
    var infantName by remember { mutableStateOf(existing.infantName) }
    var travelingWithPet by remember { mutableStateOf(existing.travelingWithPet) }
    var petType by remember { mutableStateOf(existing.petType) }
    var notes by remember { mutableStateOf(existing.notes) }

    val dietOptions = listOf("Standard", "Vegetarian", "Vegan", "Halal", "Kosher", "Gluten-Free", "Diabetic", "Low Sodium")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = "Special Requests",
            subtitle = "Let us know about any special requirements"
        )

        // Dietary preference
        InfoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Restaurant, null, tint = CoralOrange, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Meal Preference", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dietOptions.forEach { option ->
                    FilterChip(
                        selected = diet == option,
                        onClick = { diet = option },
                        label = { Text(option) },
                        leadingIcon = if (diet == option) {
                            { Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralOrange.copy(alpha = 0.15f),
                            selectedLabelColor = CoralOrange
                        )
                    )
                }
            }
        }

        // Assistance
        InfoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Accessible, null, tint = SkyBlue, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Assistance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            ToggleRow(
                label = "I need special assistance",
                checked = needsAssistance,
                onCheckedChange = { needsAssistance = it }
            )

            AnimatedVisibility(visible = needsAssistance, enter = fadeIn() + expandVertically()) {
                AppTextField(
                    value = assistanceType,
                    onValueChange = { assistanceType = it },
                    label = "Type of assistance needed",
                    leadingIcon = Icons.Outlined.Accessibility
                )
            }
        }

        // Traveling with infant
        InfoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ChildCare, null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Infant / Pet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            ToggleRow(
                label = "Traveling with infant",
                checked = travelingWithInfant,
                onCheckedChange = { travelingWithInfant = it }
            )

            AnimatedVisibility(visible = travelingWithInfant, enter = fadeIn() + expandVertically()) {
                AppTextField(
                    value = infantName,
                    onValueChange = { infantName = it },
                    label = "Infant's name",
                    leadingIcon = Icons.Outlined.Face
                )
            }

            ToggleRow(
                label = "Traveling with pet",
                checked = travelingWithPet,
                onCheckedChange = { travelingWithPet = it }
            )

            AnimatedVisibility(visible = travelingWithPet, enter = fadeIn() + expandVertically()) {
                AppTextField(
                    value = petType,
                    onValueChange = { petType = it },
                    label = "Type of pet (e.g. Dog, Cat)",
                    leadingIcon = Icons.Outlined.Pets
                )
            }
        }

        // Additional notes
        InfoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Notes, null, tint = NeutralGray700, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Additional Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            AppTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Any other requests or information",
                leadingIcon = Icons.Outlined.Edit,
                singleLine = false
            )
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
                text = "Complete Check-In",
                onClick = {
                    onUpdate(SpecialRequests(
                        dietaryPreference = diet,
                        needsAssistance = needsAssistance,
                        assistanceType = assistanceType,
                        travelingWithInfant = travelingWithInfant,
                        infantName = infantName,
                        travelingWithPet = travelingWithPet,
                        petType = petType,
                        notes = notes
                    ))
                    onFinish()
                },
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.CheckCircle,
                gradientColors = listOf(SuccessGreen, Color(0xFF2ED8A3))
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = SkyBlue)
        )
    }
}


