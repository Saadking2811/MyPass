package com.example.myapplication.ui.checkin

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.myapplication.model.PassportInfo
import com.example.myapplication.ocr.MrzParser
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.ScreenScaffold
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.PureWhite
import com.example.myapplication.viewmodel.AppUiState
import com.example.myapplication.viewmodel.AppViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/** Step 2/6 — live passport MRZ scan via CameraX + ML Kit. Multi-frame voting for accuracy. */
@Composable
fun PassportScanScreen(
    vm: AppViewModel, state: AppUiState,
    onPassportScanned: () -> Unit, onBack: () -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var mrzResult     by remember { mutableStateOf<MrzParser.MrzResult?>(null) }
    var isProcessing  by remember { mutableStateOf(false) }
    var scanStatus    by remember { mutableStateOf("Position your passport in the frame") }
    val candidates    = remember { mutableListOf<MrzParser.MrzResult>() }
    var firstSeenAt   by remember { mutableStateOf(0L) }
    var sampledCount  by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasCameraPermission = it
    }
    LaunchedEffect(Unit) { if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA) }

    if (!hasCameraPermission) {
        ScreenScaffold("Scan passport", "Step 2 of 6", onBack = onBack) {
            Column(
                modifier = Modifier.fillMaxSize().padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.CameraAlt, null, tint = EmeraldGreen, modifier = Modifier.size(40.dp)) }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Camera permission required", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Grant camera access to scan your passport.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                PrimaryButton(text = "Grant permission", onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) })
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                        if (!isProcessing && mrzResult == null) {
                            isProcessing = true
                            processPassportFrame(imageProxy) { result ->
                                if (result != null && mrzResult == null) {
                                    if (firstSeenAt == 0L) firstSeenAt = System.currentTimeMillis()
                                    candidates += result
                                    sampledCount = candidates.size
                                    val elapsed = System.currentTimeMillis() - firstSeenAt

                                    val highConfHits = candidates.count { it.confidence >= 0.80f && it.checkDigitsValid.allValid }
                                    val sampleCount  = candidates.size

                                    scanStatus = when {
                                        highConfHits >= 2 -> "Verifying… ($highConfHits/2 high-conf)"
                                        sampleCount >= 1  -> "Hold steady… analyzing ($sampleCount samples)"
                                        else              -> "Scanning MRZ zone…"
                                    }

                                    val finalResult: MrzParser.MrzResult? = when {
                                        highConfHits >= 2 -> pickBest(candidates.filter { it.confidence >= 0.80f })
                                        sampleCount >= 5 && elapsed > 2500 -> pickBest(candidates)
                                        sampleCount >= 12 -> pickBest(candidates)
                                        else -> null
                                    }

                                    if (finalResult != null) {
                                        mrzResult = finalResult
                                        scanStatus = "Verified"
                                        val passportInfo = PassportInfo(
                                            fullName       = "${finalResult.firstName} ${finalResult.lastName}".trim(),
                                            passportNumber = finalResult.passportNumber,
                                            nationality    = finalResult.nationality,
                                            dateOfBirth    = finalResult.dateOfBirth,
                                            expiryDate     = finalResult.expiryDate,
                                            gender         = finalResult.sex,
                                            rawText        = finalResult.rawMrz,
                                            verified       = finalResult.isValid
                                        )
                                        vm.attachPassportInfo(passportInfo)
                                    }
                                } else if (firstSeenAt == 0L) {
                                    scanStatus = "Scanning MRZ zone…"
                                }
                                isProcessing = false
                            }
                        } else { imageProxy.close() }
                    }
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                    } catch (_: Exception) {}
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        ScanOverlay(isScanning = mrzResult == null)

        Row(
            modifier = Modifier.fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Black.copy(0.45f))
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PureWhite) }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Scan passport", color = PureWhite, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (mrzResult != null) {
                    val r = mrzResult!!
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Filled.CheckCircle, null, tint = EmeraldGreen, modifier = Modifier.size(20.dp)) }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Verified", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "ICAO 9303 MRZ • ${(r.confidence * 100).toInt()}% confidence",
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ScanField("Name", "${r.firstName} ${r.lastName}")
                            ScanField("Passport No.", r.passportNumber)
                            ScanField("Nationality", MrzParser.getCountryName(r.nationality))
                            ScanField("Date of birth", r.dateOfBirth)
                            ScanField("Expiry", r.expiryDate)
                        }
                    }
                    PrimaryButton("Continue", onClick = onPassportScanned)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = EmeraldGreen)
                        Column {
                            Text(scanStatus, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Hold steady — MRZ scanning...",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanField(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ScanOverlay(isScanning: Boolean) {
    val anim = rememberInfiniteTransition(label = "scan")
    val y by anim.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(animation = tween(2400), repeatMode = RepeatMode.Restart),
        label = "y"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val wW = size.width * 0.86f
        val wH = wW * 0.63f
        val left = (size.width - wW) / 2
        val top  = (size.height - wH) / 2 - 100f

        drawRect(Color.Black.copy(0.65f))
        drawRoundRect(Color.Transparent, Offset(left, top), Size(wW, wH), CornerRadius(14f), blendMode = BlendMode.Clear)

        val borderColor = if (isScanning) PureWhite.copy(0.7f) else EmeraldGreen
        drawRoundRect(borderColor, Offset(left, top), Size(wW, wH), CornerRadius(14f), style = Stroke(2f))

        val cLen = 28f; val cW = 3f
        val cc = if (isScanning) PureWhite else EmeraldGreen
        drawLine(cc, Offset(left, top), Offset(left + cLen, top), cW, StrokeCap.Round)
        drawLine(cc, Offset(left, top), Offset(left, top + cLen), cW, StrokeCap.Round)
        drawLine(cc, Offset(left + wW, top), Offset(left + wW - cLen, top), cW, StrokeCap.Round)
        drawLine(cc, Offset(left + wW, top), Offset(left + wW, top + cLen), cW, StrokeCap.Round)
        drawLine(cc, Offset(left, top + wH), Offset(left + cLen, top + wH), cW, StrokeCap.Round)
        drawLine(cc, Offset(left, top + wH), Offset(left, top + wH - cLen), cW, StrokeCap.Round)
        drawLine(cc, Offset(left + wW, top + wH), Offset(left + wW - cLen, top + wH), cW, StrokeCap.Round)
        drawLine(cc, Offset(left + wW, top + wH), Offset(left + wW, top + wH - cLen), cW, StrokeCap.Round)

        if (isScanning) {
            val ly = top + wH * y
            drawLine(EmeraldGreen, Offset(left + 8f, ly), Offset(left + wW - 8f, ly), 2f)
        }
    }
}

private fun pickBest(candidates: List<MrzParser.MrzResult>): MrzParser.MrzResult {
    if (candidates.size == 1) return candidates.first()
    val byPassportNum = candidates.groupBy { it.passportNumber }.maxByOrNull { it.value.size }?.value ?: candidates
    fun <T> majority(extract: (MrzParser.MrzResult) -> T): T {
        val groups = byPassportNum.groupBy(extract)
        return groups.maxByOrNull { it.value.size }!!.key
    }
    val base = byPassportNum.maxByOrNull { it.confidence }!!
    return base.copy(
        lastName       = majority { it.lastName },
        firstName      = majority { it.firstName },
        passportNumber = majority { it.passportNumber },
        nationality    = majority { it.nationality },
        dateOfBirth    = majority { it.dateOfBirth },
        expiryDate     = majority { it.expiryDate },
        sex            = majority { it.sex },
        confidence     = byPassportNum.maxOf { it.confidence },
        isValid        = byPassportNum.any { it.isValid }
    )
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processPassportFrame(imageProxy: ImageProxy, onResult: (MrzParser.MrzResult?) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) { imageProxy.close(); onResult(null); return }
    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    recognizer.process(inputImage)
        .addOnSuccessListener { result -> onResult(MrzParser.findMrzInText(result.text)) }
        .addOnFailureListener { onResult(null) }
        .addOnCompleteListener { imageProxy.close() }
}
