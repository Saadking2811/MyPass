package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.PureWhite

/**
 * Sends a password-reset link to the user's email.
 * UI only — the network call is dispatched by the caller via [onSent].
 */
@Composable
fun ForgotPasswordDialog(
    initialEmail: String,
    onDismiss: () -> Unit,
    onSent: (email: String) -> Unit
) {
    var email by remember { mutableStateOf(initialEmail) }
    val emailValid = email.contains("@") && email.length >= 5

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Reset password", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Enter the email linked to your MyPass account. We'll send a reset link.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        focusedLabelColor = EmeraldGreen,
                        cursorColor = EmeraldGreen
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSent(email.trim()) },
                enabled = emailValid,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = PureWhite),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Send link", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
