package com.example.myapplication.ui.checkin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.SpecialRequests
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.ScreenScaffold
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.PureWhite
import com.example.myapplication.viewmodel.AppUiState

/** Step 6/6 — dietary, assistance, infant, pet toggles and free-text notes. */
@Composable
fun SpecialRequestsScreen(
    state: AppUiState, onUpdate: (SpecialRequests) -> Unit,
    onFinish: () -> Unit, onBack: () -> Unit
) {
    var diet   by remember { mutableStateOf(state.draft?.specialRequests?.dietaryPreference ?: "Standard") }
    var assist by remember { mutableStateOf(state.draft?.specialRequests?.needsAssistance ?: false) }
    var infant by remember { mutableStateOf(state.draft?.specialRequests?.travelingWithInfant ?: false) }
    var pet    by remember { mutableStateOf(state.draft?.specialRequests?.travelingWithPet ?: false) }
    var notes  by remember { mutableStateOf(state.draft?.specialRequests?.notes ?: "") }

    ScreenScaffold(
        title = "Special requests", subtitle = "Step 6 of 6", onBack = onBack,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    PrimaryButton("Complete check-in", onClick = {
                        onUpdate(SpecialRequests(diet, assist, "", infant, "", pet, "", notes))
                        onFinish()
                    })
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = diet, onValueChange = { diet = it },
                        label = { Text("Dietary preference") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = EmeraldGreen, cursorColor = EmeraldGreen
                        )
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SwitchTile("Need wheelchair / assistance", "We'll prepare in advance", assist) { assist = it }
                    SwitchTile("Traveling with infant",        "On lap or own seat",       infant) { infant = it }
                    SwitchTile("Traveling with pet",           "Service or emotional",     pet)    { pet    = it }
                    OutlinedTextField(
                        value = notes, onValueChange = { notes = it },
                        label = { Text("Additional notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = EmeraldGreen, cursorColor = EmeraldGreen
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchTile(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = EmeraldGreen, checkedThumbColor = PureWhite)
        )
    }
}
