package com.example.myapplication.ui.auth

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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.CleanField
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.util.Img
import com.example.myapplication.viewmodel.AppUiState
import com.example.myapplication.viewmodel.AppViewModel

/** Account-creation screen — name, email, phone, password. */
@Composable
fun SignUpScreen(
    state: AppUiState,
    vm: AppViewModel,
    onNavigateToHome: () -> Unit,
    onBack: () -> Unit
) {
    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var phone    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.currentUser) {
        if (state.currentUser != null) onNavigateToHome()
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuthLuxeHero(
            title = "Create account",
            subtitle = "Your journey starts here",
            heroImageUrl = Img.HERO_AIRPORT,
            onBack = onBack
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
                            "Register",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black, letterSpacing = (-0.6).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "A few details to get you started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                CleanField("Full name", name, onValueChange = { name = it })
                Spacer(modifier = Modifier.height(12.dp))
                CleanField("Email", email, keyboardType = KeyboardType.Email, onValueChange = { email = it })
                Spacer(modifier = Modifier.height(12.dp))
                CleanField("Phone", phone, keyboardType = KeyboardType.Phone, onValueChange = { phone = it })
                Spacer(modifier = Modifier.height(12.dp))
                CleanField("Password", password, keyboardType = KeyboardType.Password, isPassword = true, onValueChange = { password = it })

                Spacer(modifier = Modifier.height(20.dp))

                LuxButton(
                    text = "Create account",
                    onClick = { vm.register(name, email, phone, password) },
                    enabled = !state.isLoading,
                    loading = state.isLoading
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Already registered?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text(
                            "Sign in",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldGreen, fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "By creating an account, you agree to our Terms of Service and Privacy Policy.",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.outline,
                    textAlign= TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            }
        }
    }
}
