package com.example.myapplication.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.myapplication.model.PassportInfo
import com.example.myapplication.ui.AppUiState
import com.example.myapplication.ui.AppViewModel
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PassportScanScreen(
    state: AppUiState,
    vm: AppViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isOcrProcessing by remember { mutableStateOf(false) }
    var scanSource by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
        scanSource = "Gallery"
        if (uri != null) {
            isOcrProcessing = true
            vm.clearMessage()
            scope.launch {
                runCatching { vm.scanPassportWithOcr(context, uri) }
                    .onFailure { vm.showMessage("OCR failed. Please try a clearer image.") }
                isOcrProcessing = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            scanSource = "Camera"
            isOcrProcessing = true
            vm.clearMessage()
            scope.launch {
                runCatching { vm.scanPassportBitmapWithOcr(bitmap) }
                    .onFailure { vm.showMessage("Camera OCR failed. Try again or use gallery.") }
                isOcrProcessing = false
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(null)
        else vm.showMessage("Camera permission is required.")
    }

    val passportInfo = state.draft?.passportInfo
    val transition = rememberInfiniteTransition(label = "scanAnim")
    val scanPulse by transition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        SectionHeader(
            title = "Passport Scan",
            subtitle = "Scan your passport to verify your identity"
        )

        // Scan area
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (passportInfo != null) SuccessGreenLight else SkyBlueSubtle
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isOcrProcessing) {
                    // Scanning animation
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(scanPulse)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(SkyBlue.copy(alpha = 0.3f), SkyBlue.copy(alpha = 0.05f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 3.dp,
                            color = SkyBlue,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    Text("Scanning passport...", style = MaterialTheme.typography.bodyLarge, color = SkyBlue)
                } else if (passportInfo != null) {
                    // Success state
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
                    }
                    Text("Passport Verified", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
                } else {
                    // Ready to scan
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(2.dp, SkyBlue.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.CameraAlt, null, tint = SkyBlue, modifier = Modifier.size(40.dp))
                    }
                    Text(
                        "Take a photo or select an image\nof your passport's data page",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = NeutralGray600
                    )
                }
            }
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isOcrProcessing
            ) {
                Icon(Icons.Outlined.PhotoLibrary, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Gallery")
            }

            Button(
                onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) cameraLauncher.launch(null)
                    else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isOcrProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
            ) {
                Icon(Icons.Filled.CameraAlt, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Camera")
            }
        }

        // Passport info extracted
        AnimatedVisibility(
            visible = passportInfo != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut()
        ) {
            passportInfo?.let { info ->
                PassportInfoCard(info = info)
            }
        }

        Spacer(Modifier.weight(1f))

        // Navigation buttons
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
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = passportInfo != null,
                icon = Icons.Filled.ArrowForward
            )
        }
    }
}

@Composable
private fun PassportInfoCard(info: PassportInfo) {
    InfoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Badge, null, tint = SkyBlue, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text("Extracted Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(color = NeutralGray200)
        PassportField("Full Name", info.fullName)
        PassportField("Passport Number", info.passportNumber)
        if (info.nationality.isNotBlank()) PassportField("Nationality", info.nationality)
        if (info.dateOfBirth.isNotBlank()) PassportField("Date of Birth", info.dateOfBirth)
        if (info.expiryDate.isNotBlank()) PassportField("Expiry Date", info.expiryDate)
        if (info.gender.isNotBlank()) PassportField("Gender", info.gender)

        if (info.verified) {
            StatusChip(text = "Verified", color = SuccessGreen, icon = Icons.Filled.Verified)
        } else {
            StatusChip(text = "Needs Review", color = WarningAmber, icon = Icons.Filled.Warning)
        }
    }
}

@Composable
private fun PassportField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = NeutralGray600)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
