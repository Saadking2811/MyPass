package com.example.myapplication.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.CleanField
import com.example.myapplication.ui.components.ForgotPasswordDialog
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.util.Img
import com.example.myapplication.viewmodel.AppUiState
import com.example.myapplication.viewmodel.AppViewModel

/**
 * Sign-in screen — email + password form plus a real Google account picker
 * sourced from the device via Android's AccountManager chooser intent.
 */
@Composable
fun SignInScreen(
    state: AppUiState,
    vm: AppViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.currentUser) {
        if (state.currentUser != null) onNavigateToHome()
    }

    val accountPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val pickedEmail = result.data?.getStringExtra(
                android.accounts.AccountManager.KEY_ACCOUNT_NAME
            )?.trim().orEmpty()
            if (pickedEmail.isNotBlank()) {
                val displayName = pickedEmail.substringBefore("@")
                    .replace(".", " ").replace("_", " ").replace("-", " ")
                    .split(" ").joinToString(" ") { w ->
                        if (w.isNotEmpty()) w[0].uppercase() + w.substring(1) else w
                    }
                vm.signInWithGoogle(pickedEmail, displayName.ifBlank { pickedEmail })
            } else {
                vm.showMessage("No Google account selected.")
            }
        }
    }
    val launchGooglePicker = {
        val intent = android.accounts.AccountManager.newChooseAccountIntent(
            null, null, arrayOf("com.google"),
            null, null, null, null
        )
        runCatching { accountPickerLauncher.launch(intent) }
            .onFailure { vm.showMessage("Account picker unavailable: ${it.message}") }
        Unit
    }
    if (showForgotDialog) {
        ForgotPasswordDialog(
            initialEmail = email,
            onDismiss = { showForgotDialog = false },
            onSent = { sentEmail ->
                showForgotDialog = false
                vm.showMessage("If $sentEmail is registered, a reset link has been sent.")
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuthLuxeHero(
            title = "Welcome back",
            subtitle = "Sign in to continue your journey",
            heroImageUrl = Img.HERO_PLANE
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.56f)
        ) {
            AuthLuxeSheet {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .width(3.dp).height(28.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(EmeraldGreen)
                    )
                    Column {
                        Text(
                            "Sign in",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black, letterSpacing = (-0.6).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Use your MyPass credentials",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                CleanField("Email", email, keyboardType = KeyboardType.Email, onValueChange = { email = it })
                Spacer(modifier = Modifier.height(12.dp))
                CleanField("Password", password, keyboardType = KeyboardType.Password, isPassword = true, onValueChange = { password = it })

                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = { showForgotDialog = true },
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) { Text("Forgot password?", color = EmeraldGreen, fontWeight = FontWeight.SemiBold) }

                Spacer(modifier = Modifier.height(18.dp))

                LuxButton(
                    text = "Sign In",
                    onClick = { vm.login(email, password) },
                    enabled = !state.isLoading,
                    loading = state.isLoading
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        "  OR  ", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                        color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                }

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedButton(
                    onClick  = { launchGooglePicker() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("G", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF4285F4))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Continue with Google", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "New to MyPass?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(onClick = onNavigateToSignUp, contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text(
                            "Create an account",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldGreen, fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "By continuing, you agree to our Terms of Service and Privacy Policy.",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.outline,
                    textAlign= TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            }
        }
    }
}
