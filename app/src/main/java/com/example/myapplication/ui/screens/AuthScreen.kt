package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

@Composable
fun AuthScreen(
    isLoginMode: Boolean,
    isLoading: Boolean,
    onToggleMode: () -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    onLogin: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val transition = rememberInfiniteTransition(label = "bg")
    val floatOffset by transition.animateFloat(
        initialValue = -20f, targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )
    val planeRotation by transition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "planeRot"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(SkyBlue, Color(0xFF00C6FB), Color(0xFFE8F4FD))
                    )
                )
        ) {
            // Floating decorative circles
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer { translationX = -50f; translationY = 100f + floatOffset }
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            )
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer { translationX = 800f; translationY = 300f - floatOffset }
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // Logo & plane icon
            Icon(
                Icons.Filled.Flight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer { rotationZ = -45f + planeRotation }
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "SkyPass",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Your journey begins here",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(Modifier.height(32.dp))

            // Auth card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tab selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AuthTab("Sign In", isLoginMode) { onToggleMode() }
                        AuthTab("Sign Up", !isLoginMode) { onToggleMode() }
                    }

                    Spacer(Modifier.height(4.dp))

                    AnimatedContent(
                        targetState = isLoginMode,
                        transitionSpec = {
                            fadeIn(tween(300)) + slideInVertically { it / 4 } togetherWith
                                    fadeOut(tween(200)) + slideOutVertically { -it / 4 }
                        },
                        label = "authContent"
                    ) { loginMode ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (!loginMode) {
                                AppTextField(
                                    value = name, onValueChange = { name = it },
                                    label = "Full Name", leadingIcon = Icons.Outlined.Person
                                )
                            }

                            AppTextField(
                                value = email, onValueChange = { email = it },
                                label = "Email Address", leadingIcon = Icons.Outlined.Email,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )

                            if (!loginMode) {
                                AppTextField(
                                    value = phone, onValueChange = { phone = it },
                                    label = "Phone Number", leadingIcon = Icons.Outlined.Phone,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                            }

                            AppTextField(
                                value = password, onValueChange = { password = it },
                                label = "Password", leadingIcon = Icons.Outlined.Lock,
                                isPassword = true
                            )

                            Spacer(Modifier.height(4.dp))

                            GradientButton(
                                text = if (loginMode) "Sign In" else "Create Account",
                                onClick = {
                                    if (loginMode) onLogin(email, password)
                                    else onRegister(name, email, phone, password)
                                },
                                enabled = !isLoading,
                                icon = if (loginMode) Icons.Filled.Login else Icons.Filled.PersonAdd
                            )
                        }
                    }

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Text(
                            "  or  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }

                    // Google sign-in
                    SecondaryButton(
                        text = "Continue with Google",
                        onClick = onGoogleSignIn,
                        icon = Icons.Filled.AccountCircle
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Bottom features row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeaturePill(Icons.Outlined.Security, "Secure")
                FeaturePill(Icons.Outlined.Speed, "Fast")
                FeaturePill(Icons.Outlined.CloudOff, "Offline")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RowScope.AuthTab(text: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeaturePill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.9f))
    }
}
