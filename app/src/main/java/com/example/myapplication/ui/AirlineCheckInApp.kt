package com.example.myapplication.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutElastic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.PreferencesManager
import com.example.myapplication.model.PassportInfo
import com.example.myapplication.model.SpecialRequests
import com.example.myapplication.ocr.MrzParser
import com.example.myapplication.ui.theme.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val DASHBOARD = "dashboard"
    const val LOOKUP = "lookup"
    const val PASSPORT = "passport"
    const val REVIEW = "review"
    const val SEAT = "seat"
    const val BAGGAGE = "baggage"
    const val REQUESTS = "requests"
    const val PASS = "pass"
    const val PROFILE = "profile"
    const val PREFERENCES = "preferences"
    const val BOOKING_HISTORY = "booking_history"
}

// ═══════════════════════════════════════════════════════════════
//  MAIN APP
// ═══════════════════════════════════════════════════════════════

@Composable
fun AirlineCheckInApp(
    preferencesManager: PreferencesManager? = null,
    isDarkMode: Boolean = false,
    currentLanguage: String = "en"
) {
    val navController = rememberNavController()
    val vm: AppViewModel = viewModel()
    val state by vm.uiState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    val lang = remember(currentLanguage) {
        when (currentLanguage) {
            "fr" -> AppStrings.Lang.FR
            "ar" -> AppStrings.Lang.AR
            else -> AppStrings.Lang.EN
        }
    }
    val s = { key: String -> AppStrings.get(key, lang) }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let {
            snackbarHost.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHost) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Routes.SPLASH,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(tween(400)) + slideInHorizontally(tween(400)) { it / 3 } },
                exitTransition = { fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -it / 4 } },
                popEnterTransition = { fadeIn(tween(400)) + slideInHorizontally(tween(400)) { -it / 3 } },
                popExitTransition = { fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { it / 4 } }
            ) {
                composable(
                    Routes.SPLASH,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { fadeOut(tween(500)) + scaleOut(tween(500), targetScale = 1.2f) }
                ) {
                    SplashScreen(
                        onFinished = {
                            navController.navigate(
                                if (state.currentUser == null) Routes.AUTH else Routes.DASHBOARD
                            ) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.AUTH) {
                    AuthScreen(
                        state = state,
                        vm = vm,
                        onNavigateToHome = {
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.AUTH) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.DASHBOARD) {
                    DashboardScreen(
                        state = state,
                        vm = vm,
                        lang = lang,
                        s = s,
                        onNavigateToLookup = { navController.navigate(Routes.LOOKUP) },
                        onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                        onNavigateToPreferences = { navController.navigate(Routes.PREFERENCES) },
                        onNavigateToHistory = { navController.navigate(Routes.BOOKING_HISTORY) },
                        onLogout = {
                            vm.logout()
                            navController.navigate(Routes.AUTH) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.LOOKUP) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppBackdrop()
                        Column(modifier = Modifier.fillMaxSize()) {
                            StatusBar(state, s)
                            MainCard {
                                BookingLookupScreen(
                                    state = state,
                                    vm = vm,
                                    onNext = { navController.navigate(Routes.PASSPORT) }
                                )
                            }
                        }
                    }
                }
                composable(Routes.PASSPORT) {
                    PassportScanScreen(
                        vm = vm,
                        state = state,
                        onPassportScanned = { navController.navigate(Routes.REVIEW) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.REVIEW) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppBackdrop()
                        Column(modifier = Modifier.fillMaxSize()) {
                            StatusBar(state, s)
                            MainCard {
                                DetailsReviewScreen(
                                    state = state,
                                    onNext = { navController.navigate(Routes.SEAT) },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
                composable(Routes.SEAT) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppBackdrop()
                        Column(modifier = Modifier.fillMaxSize()) {
                            StatusBar(state, s)
                            MainCard {
                                SeatSelectionScreen(
                                    state = state,
                                    onSeatSelected = vm::updateSeat,
                                    onNext = { navController.navigate(Routes.BAGGAGE) }
                                )
                            }
                        }
                    }
                }
                composable(Routes.BAGGAGE) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppBackdrop()
                        Column(modifier = Modifier.fillMaxSize()) {
                            StatusBar(state, s)
                            MainCard {
                                BaggageScreen(
                                    state = state,
                                    onUpdate = vm::updateBaggage,
                                    onNext = { navController.navigate(Routes.REQUESTS) }
                                )
                            }
                        }
                    }
                }
                composable(Routes.REQUESTS) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppBackdrop()
                        Column(modifier = Modifier.fillMaxSize()) {
                            StatusBar(state, s)
                            MainCard {
                                SpecialRequestsScreen(
                                    state = state,
                                    onUpdate = vm::updateSpecialRequests,
                                    onFinish = {
                                        vm.completeCheckIn()
                                        navController.navigate(Routes.PASS)
                                    }
                                )
                            }
                        }
                    }
                }
                composable(Routes.PASS) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppBackdrop()
                        Column(modifier = Modifier.fillMaxSize()) {
                            StatusBar(state, s)
                            MainCard {
                                BoardingPassScreen(
                                    state = state,
                                    vm = vm,
                                    s = s,
                                    onNewLookup = {
                                        vm.resetForNewCheckIn()
                                        navController.navigate(Routes.DASHBOARD) {
                                            popUpTo(Routes.DASHBOARD) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        state = state,
                        preferencesManager = preferencesManager,
                        s = s,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.PREFERENCES) {
                    PreferencesScreen(
                        preferencesManager = preferencesManager,
                        isDarkMode = isDarkMode,
                        currentLanguage = currentLanguage,
                        s = s,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.BOOKING_HISTORY) {
                    BookingHistoryScreen(
                        state = state,
                        s = s,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            if (state.isSyncing) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FreshGreen)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  SPLASH SCREEN — Modern typography with geometric animation
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SplashScreen(onFinished: () -> Unit) {
    val context = LocalContext.current

    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val shimmer = remember { Animatable(0f) }
    val lineProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(context, uri)?.play()
        } catch (_: Exception) {}

        // Staggered entrance
        launch { lineProgress.animateTo(1f, tween(1400, easing = FastOutSlowInEasing)) }
        launch { logoAlpha.animateTo(1f, tween(600, delayMillis = 200)) }
        launch { logoScale.animateTo(1f, tween(1000, delayMillis = 200, easing = EaseOutBack)) }
        launch { textAlpha.animateTo(1f, tween(600, delayMillis = 700)) }
        launch { taglineAlpha.animateTo(1f, tween(600, delayMillis = 1000)) }
        launch { shimmer.animateTo(1f, tween(800, delayMillis = 1200)) }
        delay(2800)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DarkGreen, EmeraldGreen, FreshGreen)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Geometric line pattern background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val progress = lineProgress.value
            val lineColor = Color.White.copy(alpha = 0.06f)
            val spacing = 60f
            // Diagonal lines sweeping in
            for (i in -20..40) {
                val startX = i * spacing * progress
                val endX = startX + size.height
                drawLine(
                    color = lineColor,
                    start = Offset(startX, 0f),
                    end = Offset(endX, size.height),
                    strokeWidth = 1f
                )
            }
            // Horizontal accent lines
            val accentColor = Color.White.copy(alpha = 0.04f)
            for (i in 0..8) {
                val y = size.height * (i / 8f)
                drawLine(accentColor, Offset(0f, y), Offset(size.width * progress, y), 0.5f)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant icon — just a thin airplane line
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = logoAlpha.value
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    }
            ) {
                Icon(
                    Icons.Filled.FlightTakeoff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Beautiful typography "MyPass"
            Text(
                "MyPass",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2).sp
                ),
                color = Color.White,
                modifier = Modifier.graphicsLayer { alpha = textAlpha.value }
            )

            // Thin red accent line under logo text
            Box(
                modifier = Modifier
                    .graphicsLayer { alpha = textAlpha.value }
                    .padding(top = 4.dp)
                    .width((80 * textAlpha.value).dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CrimsonRed)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Your Digital Passport to the World",
                style = MaterialTheme.typography.bodyLarge.copy(
                    letterSpacing = 1.sp
                ),
                color = Color.White.copy(alpha = 0.65f),
                modifier = Modifier.graphicsLayer { alpha = taglineAlpha.value }
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Minimalist loading bar
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(shimmer.value)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color.White.copy(alpha = 0.4f), Color.White, Color.White.copy(alpha = 0.4f))
                            )
                        )
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  APP BACKDROP & SHARED COMPONENTS
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AppBackdrop() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(DarkGreen, EmeraldGreen, Color(0xFF004D2B))
                )
            )
    ) {
        // Modern diagonal line pattern instead of circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineColor = Color.White.copy(alpha = 0.035f)
            val redAccent = CrimsonRed.copy(alpha = 0.04f)
            val spacing = 50f
            // Diagonal lines
            for (i in -10..30) {
                val x = i * spacing
                drawLine(lineColor, Offset(x, 0f), Offset(x + size.height * 0.5f, size.height), 1f)
            }
            // Subtle horizontal grid
            for (i in 0..12) {
                val y = size.height * (i / 12f)
                drawLine(lineColor, Offset(0f, y), Offset(size.width, y), 0.5f)
            }
            // Red accent diagonal (single)
            drawLine(
                redAccent,
                Offset(size.width * 0.6f, 0f),
                Offset(size.width, size.height * 0.4f),
                2f
            )
        }
    }
}

@Composable
private fun StatusBar(state: AppUiState, s: (String) -> String = { it }) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val (text, color) = when {
                !state.isOnline -> s("offline_mode") to StatusError
                state.isSyncing -> s("syncing") to StatusWarning
                else -> s("ready") to StatusSuccess
            }
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Text(text, color = Color.White, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.weight(1f))
            Text("MyPass", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MainCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f))
            .padding(top = 4.dp)
    ) {
        content()
    }
}

@Composable
private fun ScreenContainer(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (onBack != null) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { -40 }
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400, delayMillis = 100)) + slideInVertically(tween(400, delayMillis = 100)) { -30 }
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200)) { 60 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun GreenButton(text: String, onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen, contentColor = PureWhite)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
private fun RedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

// ═══════════════════════════════════════════════════════════════
//  AUTH SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AuthScreen(state: AppUiState, vm: AppViewModel, onNavigateToHome: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLogin = state.isAuthMode

    LaunchedEffect(state.currentUser) {
        if (state.currentUser != null) onNavigateToHome()
    }

    var headerVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        headerVisible = true
        delay(300)
        cardVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Green gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    brush = Brush.verticalGradient(listOf(DarkGreen, EmeraldGreen, FreshGreen))
                )
        ) {
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -80 }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.FlightTakeoff,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("MyPass", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Digital Check-in", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }

        // Floating card
        AnimatedVisibility(
            visible = cardVisible,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 120 },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 260.dp)
        ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    if (isLogin) "Welcome Back" else "Create Account",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (!isLogin) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                GreenButton(
                    text = if (isLogin) "Sign In" else "Create Account",
                    onClick = {
                        if (isLogin) vm.login(email, password) else vm.register(name, email, phone, password)
                    },
                    enabled = !state.isLoading
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { vm.toggleAuthMode() }) {
                        Text(
                            if (isLogin) "Create account" else "Already have an account?",
                            color = FreshGreen
                        )
                    }
                    TextButton(onClick = { vm.signInWithGoogle() }) {
                        Text("Google Sign-In", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = FreshGreen)
                }
            }
        }
        } // End AnimatedVisibility
    }
}

// ═══════════════════════════════════════════════════════════════
//  DASHBOARD SCREEN — Account + Flights + History
// ═══════════════════════════════════════════════════════════════

@Composable
private fun DashboardScreen(
    state: AppUiState,
    vm: AppViewModel,
    lang: AppStrings.Lang = AppStrings.Lang.EN,
    s: (String) -> String = { it },
    onNavigateToLookup: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPreferences: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onLogout: () -> Unit
) {
    val user = state.currentUser
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize()) {
        AppBackdrop()

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top spacing
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Header with user greeting
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -60 }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "MyPass",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1).sp
                                ),
                                color = Color.White
                            )
                            Text(
                                if (user != null) "${s("welcome")}, ${user.fullName.split(" ").first()}" else s("welcome"),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { vm.synchronize() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.12f))
                            ) {
                                Icon(Icons.Filled.Search, s("sync"), tint = Color.White)
                            }
                            IconButton(
                                onClick = onLogout,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.12f))
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, s("logout"), tint = Color.White)
                            }
                        }
                    }
                }
            }

            // Quick Action Grid — 4 buttons: Check-In, Profile, History, Settings
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(tween(500, delayMillis = 100)) { 80 }
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DashboardQuickAction(
                                icon = Icons.Filled.FlightTakeoff,
                                label = s("new_checkin"),
                                color = FreshGreen,
                                onClick = onNavigateToLookup
                            )
                            DashboardQuickAction(
                                icon = Icons.Filled.Person,
                                label = s("profile"),
                                color = EmeraldGreen,
                                onClick = onNavigateToProfile
                            )
                            DashboardQuickAction(
                                icon = Icons.Filled.History,
                                label = s("my_bookings"),
                                color = CrimsonRed,
                                onClick = onNavigateToHistory
                            )
                            DashboardQuickAction(
                                icon = Icons.Filled.Settings,
                                label = s("settings"),
                                color = SteelGray,
                                onClick = onNavigateToPreferences
                            )
                        }
                    }
                }
            }

            // Quick Check-In CTA card
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200)) { 80 }
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        brush = Brush.linearGradient(listOf(EmeraldGreen, FreshGreen))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.FlightTakeoff, null, tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    s("new_checkin"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    s("enter_booking_ref"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = onNavigateToLookup,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text(s("start"), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Profile card
            if (user != null) {
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300)) { 80 }
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToProfile() },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MintGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            user.fullName.take(1).uppercase(),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(user.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (user.phone.isNotBlank()) {
                                            Text(user.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        null,
                                        modifier = Modifier.size(16.dp).graphicsLayer { rotationZ = 180f },
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Stats row
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500, delayMillis = 350)) + slideInVertically(tween(500, delayMillis = 350)) { 60 }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            label = s("total_flights"),
                            value = "${state.cachedFlights.size}",
                            color = FreshGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = s("checkins_done"),
                            value = "${state.cachedBoardingPasses.size}",
                            color = CrimsonRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Upcoming Flights section
            if (state.cachedFlights.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(500, delayMillis = 450)) + slideInVertically(tween(500, delayMillis = 450)) { 60 }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                s("upcoming_flights"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            TextButton(onClick = onNavigateToHistory) {
                                Text(s("view_all"), color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }
                }

                itemsIndexed(state.cachedFlights.take(3)) { index, flight ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400, delayMillis = 500 + index * 100)) +
                                slideInVertically(tween(400, delayMillis = 500 + index * 100)) { 60 }
                    ) {
                        FlightCard(flight = flight, s = s)
                    }
                }
            }

            // Boarding Pass History
            if (state.cachedBoardingPasses.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(500, delayMillis = 700)) + slideInVertically(tween(500, delayMillis = 700)) { 60 }
                    ) {
                        Text(
                            s("boarding_passes"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                itemsIndexed(state.cachedBoardingPasses.take(3)) { index, pass ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400, delayMillis = 800 + index * 100)) +
                                slideInVertically(tween(400, delayMillis = 800 + index * 100)) { 60 }
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            brush = Brush.linearGradient(listOf(EmeraldGreen, FreshGreen))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.ConfirmationNumber, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${pass.origin} → ${pass.destination}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${pass.flightNumber} • ${s("seat")} ${pass.seat}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    pass.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FreshGreen,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MintGreen)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun DashboardQuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FlightCard(flight: com.example.myapplication.model.FlightItinerary, s: (String) -> String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(flight.flightNumber, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = FreshGreen)
                Text(
                    if (flight.checkInStatus == "Checked-In") s("checked_in") else s("not_checked_in"),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (flight.checkInStatus == "Checked-In") FreshGreen else CrimsonRed,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (flight.checkInStatus == "Checked-In") MintGreen else WarmPink
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(flight.origin, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(flight.originCity, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(flight.duration, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Canvas(modifier = Modifier.width(60.dp).height(16.dp)) {
                        val y = size.height / 2
                        drawLine(SilverGray, Offset(0f, y), Offset(size.width, y), 1.5f)
                        drawCircle(CrimsonRed, 3f, Offset(size.width * 0.7f, y))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(flight.destination, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(flight.destinationCity, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(flight.departureTime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${s("gate")} ${flight.gate}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  BOOKING LOOKUP SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
private fun BookingLookupScreen(state: AppUiState, vm: AppViewModel, onNext: () -> Unit) {
    var bookingRef by remember { mutableStateOf("NM2025A") }
    var lastName by remember { mutableStateOf("NAMOUNE") }

    ScreenContainer(
        title = "Flight Lookup",
        subtitle = "Enter your booking reference and last name"
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MintGreen.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = bookingRef,
                    onValueChange = { bookingRef = it.uppercase() },
                    label = { Text("Booking Reference") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it.uppercase() },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { vm.lookupFlight(bookingRef, lastName) },
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.FlightTakeoff, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (state.isLoading) "Searching..." else "Retrieve")
            }
            OutlinedButton(onClick = { vm.synchronize() }, shape = RoundedCornerShape(12.dp)) {
                Text("Sync")
            }
        }

        AnimatedVisibility(visible = state.lookupResult != null, enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
            state.lookupResult?.let { flight ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(flight.origin, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = FreshGreen)
                                Text(flight.originCity, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Filled.AirplanemodeActive, null, tint = CrimsonRed, modifier = Modifier.size(32.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(flight.destination, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = FreshGreen)
                                Text(flight.destinationCity, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            InfoLabel("Flight", flight.flightNumber)
                            InfoLabel("Gate", flight.gate)
                            InfoLabel("Departure", flight.departureTime.takeLast(5))
                            InfoLabel("Status", flight.checkInStatus)
                        }
                    }
                }
            }
        }

        GreenButton(text = "Continue to Passport Scan", onClick = onNext, enabled = state.lookupResult != null)
    }
}

@Composable
private fun InfoLabel(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

// ═══════════════════════════════════════════════════════════════
//  PASSPORT SCAN SCREEN — CameraX + ML Kit + ICAO 9303 MRZ
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PassportScanScreen(
    vm: AppViewModel,
    state: AppUiState,
    onPassportScanned: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var mrzResult by remember { mutableStateOf<MrzParser.MrzResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var scanStatus by remember { mutableStateOf("Position your passport in the frame") }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Filled.CameraAlt, null, modifier = Modifier.size(72.dp), tint = FreshGreen)
                Text("Camera permission required", style = MaterialTheme.typography.titleLarge)
                Text("Grant camera access to scan your passport", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                GreenButton("Grant Permission", onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }, modifier = Modifier.padding(horizontal = 48.dp))
                TextButton(onClick = onBack) { Text("Go Back") }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // CameraX Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                        if (!isProcessing && mrzResult == null) {
                            isProcessing = true
                            processPassportFrame(imageProxy) { result ->
                                if (result != null && mrzResult == null) {
                                    mrzResult = result
                                    scanStatus = "Passport detected!"
                                    val passportInfo = PassportInfo(
                                        fullName = "${result.firstName} ${result.lastName}".trim(),
                                        passportNumber = result.passportNumber,
                                        nationality = result.nationality,
                                        dateOfBirth = result.dateOfBirth,
                                        expiryDate = result.expiryDate,
                                        gender = result.sex,
                                        rawText = result.rawMrz,
                                        verified = result.isValid
                                    )
                                    vm.attachPassportInfo(passportInfo)
                                } else {
                                    scanStatus = "Scanning MRZ zone..."
                                }
                                isProcessing = false
                            }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (_: Exception) {}
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Scan overlay
        PassportScanOverlay(isScanning = mrzResult == null)

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Scan Passport", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        // Bottom panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (mrzResult != null) {
                val r = mrzResult!!
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = FreshGreen, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Passport Verified", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FreshGreen)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("${(r.confidence * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, color = FreshGreen)
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MintGreen.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        MrzField("Name", "${r.firstName} ${r.lastName}")
                        MrzField("Passport", r.passportNumber)
                        MrzField("Nationality", MrzParser.getCountryName(r.nationality))
                        MrzField("Date of Birth", r.dateOfBirth)
                        MrzField("Expiry", r.expiryDate)
                        MrzField("Gender", r.sex)
                    }
                }

                GreenButton("Confirm & Continue", onClick = onPassportScanned)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = FreshGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(scanStatus, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("ICAO 9303 MRZ Technology", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun MrzField(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PassportScanOverlay(isScanning: Boolean) {
    val transition = rememberInfiniteTransition(label = "scan")
    val scanY by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500)),
        label = "scanLine"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val windowW = size.width * 0.88f
        val windowH = windowW * 0.63f
        val left = (size.width - windowW) / 2
        val top = (size.height - windowH) / 2 - 80f

        // Dark overlay
        drawRect(Color.Black.copy(alpha = 0.55f))

        // Clear passport window
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(windowW, windowH),
            cornerRadius = CornerRadius(16f),
            blendMode = BlendMode.Clear
        )

        // Border
        val borderColor = if (isScanning) Color.White.copy(alpha = 0.6f) else FreshGreen
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(left, top),
            size = Size(windowW, windowH),
            cornerRadius = CornerRadius(16f),
            style = Stroke(3f)
        )

        // Corner markers
        val cLen = 35f
        val cStroke = 4f
        val corners = listOf(
            Offset(left, top) to Offset(left + cLen, top) to Offset(left, top + cLen),
            Offset(left + windowW, top) to Offset(left + windowW - cLen, top) to Offset(left + windowW, top + cLen),
            Offset(left, top + windowH) to Offset(left + cLen, top + windowH) to Offset(left, top + windowH - cLen),
            Offset(left + windowW, top + windowH) to Offset(left + windowW - cLen, top + windowH) to Offset(left + windowW, top + windowH - cLen)
        )
        val markerColor = if (isScanning) Color.White else FreshGreen
        corners.forEach { (pair, third) ->
            val (corner, h) = pair
            drawLine(markerColor, corner, h, cStroke, StrokeCap.Round)
            drawLine(markerColor, corner, third, cStroke, StrokeCap.Round)
        }

        // Animated scan line
        if (isScanning) {
            val lineY = top + windowH * scanY
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, FreshGreen, FreshGreen, Color.Transparent)
                ),
                start = Offset(left + 10f, lineY),
                end = Offset(left + windowW - 10f, lineY),
                strokeWidth = 2.5f
            )
        }

        // MRZ zone indicator at bottom of window
        val mrzTop = top + windowH - windowH * 0.22f
        drawRoundRect(
            color = if (isScanning) CrimsonRed.copy(alpha = 0.15f) else FreshGreen.copy(alpha = 0.15f),
            topLeft = Offset(left + 8f, mrzTop),
            size = Size(windowW - 16f, windowH * 0.18f),
            cornerRadius = CornerRadius(8f)
        )
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processPassportFrame(imageProxy: ImageProxy, onResult: (MrzParser.MrzResult?) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        onResult(null)
        return
    }

    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    recognizer.process(inputImage)
        .addOnSuccessListener { result ->
            val mrzResult = MrzParser.findMrzInText(result.text)
            onResult(mrzResult)
        }
        .addOnFailureListener {
            onResult(null)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

// ═══════════════════════════════════════════════════════════════
//  DETAILS REVIEW SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
private fun DetailsReviewScreen(state: AppUiState, onNext: () -> Unit, onBack: () -> Unit) {
    val draft = state.draft ?: return

    ScreenContainer(title = "Review Details", subtitle = "Verify your information before seat selection", onBack = onBack) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Flight Information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = FreshGreen)
                ReviewRow("Passenger", draft.itinerary.passengerName)
                ReviewRow("Flight", "${draft.itinerary.flightNumber} (${draft.itinerary.airlineName})")
                ReviewRow("Route", "${draft.itinerary.origin} to ${draft.itinerary.destination}")
                ReviewRow("Departure", draft.itinerary.departureTime)
                ReviewRow("Aircraft", draft.itinerary.aircraftType)
            }
        }

        draft.passportInfo?.let { passport ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MintGreen.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null, tint = FreshGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Passport Verified", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = FreshGreen)
                    }
                    ReviewRow("Name", passport.fullName)
                    ReviewRow("Passport No.", passport.passportNumber)
                    ReviewRow("Nationality", MrzParser.getCountryName(passport.nationality))
                    ReviewRow("DOB", passport.dateOfBirth)
                    ReviewRow("Expiry", passport.expiryDate)
                }
            }
        }

        GreenButton("Continue to Seat Selection", onClick = onNext)
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}

// ═══════════════════════════════════════════════════════════════
//  SEAT SELECTION SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SeatSelectionScreen(state: AppUiState, onSeatSelected: (String) -> Unit, onNext: () -> Unit) {
    val selectedSeat = state.draft?.selectedSeat

    ScreenContainer(title = "Select Your Seat", subtitle = "Aircraft cabin layout") {
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SeatLegendItem("Available", SeatAvailable)
            SeatLegendItem("Premium", SeatPremium)
            SeatLegendItem("Selected", SeatSelected)
            SeatLegendItem("Taken", SeatOccupied)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Aircraft fuselage card
        Card(
            shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = IvoryWhite),
            modifier = Modifier.fillMaxWidth().height(480.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state.seatMap) { seat ->
                    val isSelected = seat.seatCode == selectedSeat
                    val seatScale by animateFloatAsState(if (isSelected) 1.1f else 1f, label = "seatScale")
                    val bg = when {
                        seat.occupied -> SeatOccupied
                        isSelected -> SeatSelected
                        seat.premium -> SeatPremium
                        else -> SeatAvailable
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp, 44.dp)
                            .scale(seatScale)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(bg)
                            .then(if (isSelected) Modifier.border(2.dp, DarkGreen, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp)) else Modifier)
                            .clickable(enabled = !seat.occupied) { onSeatSelected(seat.seatCode) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            seat.seatCode,
                            color = if (isSelected || seat.premium) Color.White else DeepCharcoal,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = selectedSeat != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MintGreen.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Selected Seat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(selectedSeat ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = FreshGreen)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${state.draft?.itinerary?.origin ?: ""} -> ${state.draft?.itinerary?.destination ?: ""}", fontWeight = FontWeight.SemiBold)
                        Text("Flight ${state.draft?.itinerary?.flightNumber ?: ""}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        GreenButton("Confirm Seat", onClick = onNext, enabled = !selectedSeat.isNullOrBlank())
    }
}

@Composable
private fun SeatLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(14.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

// ═══════════════════════════════════════════════════════════════
//  BAGGAGE SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
private fun BaggageScreen(state: AppUiState, onUpdate: (Int, Int, Int) -> Unit, onNext: () -> Unit) {
    var checked by remember { mutableIntStateOf(state.draft?.baggageDeclaration?.checkedBags ?: 0) }
    var carryOn by remember { mutableIntStateOf(state.draft?.baggageDeclaration?.carryOnBags ?: 1) }
    var oversized by remember { mutableIntStateOf(state.draft?.baggageDeclaration?.oversizedBags ?: 0) }

    ScreenContainer(title = "Baggage", subtitle = "Declare your luggage") {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Filled.Luggage, null, tint = FreshGreen, modifier = Modifier.size(40.dp))
                BaggageCounter("Checked Bags (23kg each)", checked) { checked = it.coerceAtLeast(0) }
                BaggageCounter("Carry-on Bags (7kg each)", carryOn) { carryOn = it.coerceAtLeast(0) }
                BaggageCounter("Oversized Items", oversized) { oversized = it.coerceAtLeast(0) }

                val totalWeight = checked * 23.0 + carryOn * 7.0
                Text("Total estimated weight: ${"%.1f".format(totalWeight)} kg", fontWeight = FontWeight.SemiBold, color = FreshGreen)
            }
        }

        GreenButton("Save & Continue", onClick = {
            onUpdate(checked, carryOn, oversized)
            onNext()
        })
    }
}

@Composable
private fun BaggageCounter(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = { onChange(value - 1) },
                modifier = Modifier.size(36.dp).clip(CircleShape).background(CloudGray)
            ) { Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            Text("$value", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
            IconButton(
                onClick = { onChange(value + 1) },
                modifier = Modifier.size(36.dp).clip(CircleShape).background(MintGreen)
            ) { Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkGreen) }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  SPECIAL REQUESTS SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SpecialRequestsScreen(state: AppUiState, onUpdate: (SpecialRequests) -> Unit, onFinish: () -> Unit) {
    var diet by remember { mutableStateOf(state.draft?.specialRequests?.dietaryPreference ?: "Standard") }
    var assist by remember { mutableStateOf(state.draft?.specialRequests?.needsAssistance ?: false) }
    var infant by remember { mutableStateOf(state.draft?.specialRequests?.travelingWithInfant ?: false) }
    var pet by remember { mutableStateOf(state.draft?.specialRequests?.travelingWithPet ?: false) }
    var notes by remember { mutableStateOf(state.draft?.specialRequests?.notes ?: "") }

    ScreenContainer(title = "Special Requests", subtitle = "Any additional needs for your flight?") {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = diet, onValueChange = { diet = it },
                    label = { Text("Dietary Preference") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                SwitchRow("Need wheelchair/assistance", assist) { assist = it }
                SwitchRow("Traveling with infant", infant) { infant = it }
                SwitchRow("Traveling with pet", pet) { pet = it }
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Additional Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )
            }
        }

        RedButton("Complete Check-In", onClick = {
            onUpdate(SpecialRequests(diet, assist, "", infant, "", pet, "", notes))
            onFinish()
        })
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = FreshGreen, checkedThumbColor = PureWhite)
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  BOARDING PASS SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
private fun BoardingPassScreen(state: AppUiState, vm: AppViewModel, s: (String) -> String = { it }, onNewLookup: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pass = state.latestBoardingPass ?: state.cachedBoardingPasses.firstOrNull()

    if (pass == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = FreshGreen)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Generating boarding pass...")
                } else {
                    Text("No boarding pass available yet.")
                }
            }
        }
        return
    }

    val qrBitmap = remember(pass.qrPayload) { vm.generateQrBitmap(pass.qrPayload) }
    var cardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { cardVisible = true }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Success header — animated entrance
            AnimatedVisibility(
                visible = cardVisible,
                enter = fadeIn(tween(400)) + scaleIn(tween(500), initialScale = 0.8f)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MintGreen.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = FreshGreen, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Check-In Complete!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FreshGreen)
                        Text("Your boarding pass is ready", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            // ── WORLD-CLASS BOARDING PASS CARD ──
            AnimatedVisibility(
                visible = cardVisible,
                enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(600, delayMillis = 200)) { 120 }
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // ── Green gradient header with brand ──
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(DarkGreen, EmeraldGreen, FreshGreen)
                                    )
                                )
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            // Geometric line pattern in header
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val lineCol = Color.White.copy(alpha = 0.06f)
                                for (i in 0..10) {
                                    val x = i * 40f
                                    drawLine(lineCol, Offset(x, 0f), Offset(x + size.height, size.height), 0.5f)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "MyPass",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-0.5).sp
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        "BOARDING PASS",
                                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        pass.flightNumber,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        pass.airlineName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        // Red accent strip
                        Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(CrimsonRed))

                        Column(modifier = Modifier.padding(20.dp)) {
                            // ── ROUTE — Large airport codes ──
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        pass.origin,
                                        style = MaterialTheme.typography.displaySmall.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-1).sp
                                        ),
                                        color = EmeraldGreen
                                    )
                                    Text(
                                        pass.originCity,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Flight path line with airplane
                                    Canvas(modifier = Modifier.width(80.dp).height(20.dp)) {
                                        val y = size.height / 2
                                        // Dashed line
                                        drawLine(
                                            color = SilverGray,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = 1.5f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                                        )
                                        // Airplane dot
                                        drawCircle(CrimsonRed, 4f, Offset(size.width * 0.65f, y))
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        pass.destination,
                                        style = MaterialTheme.typography.displaySmall.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-1).sp
                                        ),
                                        color = EmeraldGreen
                                    )
                                    Text(
                                        pass.destinationCity,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // ── Ticket tear-off effect (dotted line + semicircle cutouts) ──
                            val surfaceColor = MaterialTheme.colorScheme.surface
                            Canvas(modifier = Modifier.fillMaxWidth().height(20.dp)) {
                                val y = size.height / 2
                                // Dashed divider
                                drawLine(
                                    color = CloudGray,
                                    start = Offset(24f, y),
                                    end = Offset(size.width - 24f, y),
                                    strokeWidth = 1.5f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f))
                                )
                                // Left semicircle cutout
                                drawCircle(
                                    color = surfaceColor,
                                    radius = 14f,
                                    center = Offset(-2f, y)
                                )
                                // Right semicircle cutout
                                drawCircle(
                                    color = surfaceColor,
                                    radius = 14f,
                                    center = Offset(size.width + 2f, y)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // ── Passenger name (prominent) ──
                            Text("PASSENGER", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                pass.passengerName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // ── Info grid — 3 columns ──
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    PassLabel("SEAT")
                                    Text(
                                        pass.seat,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldGreen
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    PassLabel("GATE")
                                    Text(
                                        pass.gate,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldGreen
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    PassLabel("BOARDING")
                                    Text(
                                        pass.boardingGroup,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    PassLabel("DEPARTURE")
                                    PassValue(pass.departureTime.takeLast(5))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    PassLabel("ARRIVAL")
                                    PassValue(pass.arrivalTime.takeLast(5))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    PassLabel("TERMINAL")
                                    PassValue(pass.terminal)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    PassLabel("CLASS")
                                    PassValue(pass.seatClass)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    PassLabel("SEQUENCE")
                                    PassValue(pass.sequence)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    PassLabel("BAGGAGE")
                                    PassValue(pass.baggageInfo.take(12))
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // ── QR Code — centered, prominent ──
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(IvoryWhite)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Box(modifier = Modifier.padding(12.dp)) {
                                            Image(
                                                bitmap = qrBitmap.asImageBitmap(),
                                                contentDescription = "Boarding pass QR",
                                                modifier = Modifier.size(180.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Scan at gate for boarding",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // ── Footer ──
                            Text(
                                "MyPass™ — Status: ${pass.status}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = cardVisible,
                enter = fadeIn(tween(400, delayMillis = 500)) + slideInVertically(tween(400, delayMillis = 500)) { 60 }
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val fileName = vm.saveBoardingPassPdf(context, pass)
                                    vm.showMessage("PDF saved: $fileName")
                                } catch (e: Exception) {
                                    vm.showMessage("Failed to save PDF: ${e.message}")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Filled.Download, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save PDF", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = onNewLookup,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("New Check-In", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (state.cachedBoardingPasses.size > 1) {
            item {
                Text("Previous Passes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            items(state.cachedBoardingPasses.drop(1)) { cached ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${cached.flightNumber} ${cached.origin}→${cached.destination}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Seat ${cached.seat} • ${cached.issuedAt.take(10)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(cached.status, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = FreshGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun PassLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
}

@Composable
private fun PassValue(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
}

// ═══════════════════════════════════════════════════════════════
//  PROFILE SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ProfileScreen(
    state: AppUiState,
    preferencesManager: PreferencesManager?,
    s: (String) -> String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val user = state.currentUser

    var fullName by remember { mutableStateOf(user?.fullName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var passportNumber by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var isSaved by remember { mutableStateOf(false) }

    // Load saved personal info from preferences
    LaunchedEffect(preferencesManager) {
        preferencesManager?.let {
            it.savedFullName.collect { v -> if (v.isNotBlank()) fullName = v }
        }
    }
    LaunchedEffect(preferencesManager) {
        preferencesManager?.let {
            it.savedPassportNumber.collect { v -> passportNumber = v }
        }
    }
    LaunchedEffect(preferencesManager) {
        preferencesManager?.let {
            it.savedNationality.collect { v -> nationality = v }
        }
    }
    LaunchedEffect(preferencesManager) {
        preferencesManager?.let {
            it.savedDateOfBirth.collect { v -> dateOfBirth = v }
        }
    }
    LaunchedEffect(preferencesManager) {
        preferencesManager?.let {
            it.savedEmail.collect { v -> if (v.isNotBlank()) email = v }
        }
    }
    LaunchedEffect(preferencesManager) {
        preferencesManager?.let {
            it.savedPhone.collect { v -> if (v.isNotBlank()) phone = v }
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize()) {
        AppBackdrop()

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, s("back"), tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    s("profile"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Scrollable content
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar card
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -40 }
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.linearGradient(listOf(EmeraldGreen, FreshGreen))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        fullName.take(1).uppercase().ifBlank { "?" },
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(fullName.ifBlank { s("full_name") }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (email.isNotBlank()) {
                                    Text(email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Personal info fields
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400, delayMillis = 100)) + slideInVertically(tween(400, delayMillis = 100)) { 60 }
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(s("personal_info"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                                ProfileField(label = s("full_name"), value = fullName, onValueChange = { fullName = it })
                                ProfileField(label = s("email"), value = email, onValueChange = { email = it }, keyboardType = KeyboardType.Email)
                                ProfileField(label = s("phone"), value = phone, onValueChange = { phone = it }, keyboardType = KeyboardType.Phone)
                            }
                        }
                    }
                }

                // Passport info
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400, delayMillis = 200)) + slideInVertically(tween(400, delayMillis = 200)) { 60 }
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(s("passport_info"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                                ProfileField(label = s("passport_number"), value = passportNumber, onValueChange = { passportNumber = it })
                                ProfileField(label = s("nationality"), value = nationality, onValueChange = { nationality = it })
                                ProfileField(label = s("date_of_birth"), value = dateOfBirth, onValueChange = { dateOfBirth = it })
                            }
                        }
                    }
                }

                // Save button
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400, delayMillis = 300)) + slideInVertically(tween(400, delayMillis = 300)) { 40 }
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    preferencesManager?.savePersonalInfo(
                                        fullName = fullName,
                                        email = email,
                                        phone = phone,
                                        passportNumber = passportNumber,
                                        nationality = nationality,
                                        dateOfBirth = dateOfBirth
                                    )
                                    isSaved = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                        ) {
                            Text(
                                if (isSaved) "✓ ${s("saved")}" else s("save_profile"),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FreshGreen,
            focusedLabelColor = FreshGreen,
            cursorColor = FreshGreen
        ),
        singleLine = true
    )
}

// ═══════════════════════════════════════════════════════════════
//  PREFERENCES SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PreferencesScreen(
    preferencesManager: PreferencesManager?,
    isDarkMode: Boolean,
    currentLanguage: String,
    s: (String) -> String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var darkMode by remember { mutableStateOf(isDarkMode) }
    var selectedLang by remember { mutableStateOf(currentLanguage) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(preferencesManager) {
        preferencesManager?.notificationsEnabled?.collect { notificationsEnabled = it }
    }
    LaunchedEffect(preferencesManager) {
        preferencesManager?.biometricEnabled?.collect { biometricEnabled = it }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize()) {
        AppBackdrop()

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, s("back"), tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    s("settings"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Appearance section — Dark Mode
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -40 }
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(s("appearance"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(s("dark_mode"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            s("dark_mode_desc"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = darkMode,
                                        onCheckedChange = {
                                            darkMode = it
                                            scope.launch { preferencesManager?.setDarkMode(it) }
                                        },
                                        colors = SwitchDefaults.colors(checkedTrackColor = FreshGreen)
                                    )
                                }
                            }
                        }
                    }
                }

                // Language section
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400, delayMillis = 100)) + slideInVertically(tween(400, delayMillis = 100)) { 60 }
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(s("language"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))

                                val languages = listOf(
                                    Triple("en", "English", "\uD83C\uDDEC\uD83C\uDDE7"),
                                    Triple("fr", "Français", "\uD83C\uDDEB\uD83C\uDDF7"),
                                    Triple("ar", "العربية", "\uD83C\uDDE9\uD83C\uDDFF")
                                )

                                languages.forEach { (code, name, flag) ->
                                    val isSelected = selectedLang == code
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) MintGreen else Color.Transparent)
                                            .clickable {
                                                selectedLang = code
                                                scope.launch { preferencesManager?.setLanguage(code) }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(flag, style = MaterialTheme.typography.titleLarge)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Filled.CheckCircle, null, tint = FreshGreen, modifier = Modifier.size(22.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Notifications & Security
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400, delayMillis = 200)) + slideInVertically(tween(400, delayMillis = 200)) { 60 }
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(s("notifications_security"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(s("push_notifications"), style = MaterialTheme.typography.bodyLarge)
                                    Switch(
                                        checked = notificationsEnabled,
                                        onCheckedChange = {
                                            notificationsEnabled = it
                                            scope.launch { preferencesManager?.setNotifications(it) }
                                        },
                                        colors = SwitchDefaults.colors(checkedTrackColor = FreshGreen)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(s("biometric_auth"), style = MaterialTheme.typography.bodyLarge)
                                    Switch(
                                        checked = biometricEnabled,
                                        onCheckedChange = {
                                            biometricEnabled = it
                                            scope.launch { preferencesManager?.setBiometric(it) }
                                        },
                                        colors = SwitchDefaults.colors(checkedTrackColor = FreshGreen)
                                    )
                                }
                            }
                        }
                    }
                }

                // About
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400, delayMillis = 300)) + slideInVertically(tween(400, delayMillis = 300)) { 40 }
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(s("about"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("MyPass", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = EmeraldGreen)
                                Text(s("app_version"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    s("copyright"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  BOOKING HISTORY SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
private fun BookingHistoryScreen(
    state: AppUiState,
    s: (String) -> String,
    onBack: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize()) {
        AppBackdrop()

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, s("back"), tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    s("my_bookings"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Tab Row
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -30 }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(s("flights"), s("boarding_passes")).forEachIndexed { idx, title ->
                        val isActive = selectedTab == idx
                        Button(
                            onClick = { selectedTab = idx },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) FreshGreen else Color.White,
                                contentColor = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab == 0) {
                    // Flights tab
                    if (state.cachedFlights.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.FlightTakeoff,
                                message = s("no_flights")
                            )
                        }
                    } else {
                        itemsIndexed(state.cachedFlights) { index, flight ->
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(tween(300, delayMillis = index * 80)) +
                                        slideInVertically(tween(300, delayMillis = index * 80)) { 50 }
                            ) {
                                FlightCard(flight = flight, s = s)
                            }
                        }
                    }
                } else {
                    // Boarding passes tab
                    if (state.cachedBoardingPasses.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.ConfirmationNumber,
                                message = s("no_boarding_passes")
                            )
                        }
                    } else {
                        itemsIndexed(state.cachedBoardingPasses) { index, pass ->
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(tween(300, delayMillis = index * 80)) +
                                        slideInVertically(tween(300, delayMillis = index * 80)) { 50 }
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(
                                                            brush = Brush.linearGradient(listOf(EmeraldGreen, FreshGreen))
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Filled.ConfirmationNumber, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(pass.flightNumber, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = FreshGreen)
                                                    Text(pass.issuedAt.take(10), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            Text(
                                                pass.status,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = FreshGreen,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MintGreen)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(pass.origin, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Canvas(modifier = Modifier.width(50.dp).height(16.dp)) {
                                                    val y = size.height / 2
                                                    drawLine(SilverGray, Offset(0f, y), Offset(size.width, y), 1.5f)
                                                    drawCircle(CrimsonRed, 3f, Offset(size.width * 0.7f, y))
                                                }
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(pass.destination, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("${s("seat")} ${pass.seat}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                            Text("${s("gate")} ${pass.gate}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                            Text(pass.boardingGroup, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon, null,
                modifier = Modifier.size(48.dp),
                tint = SilverGray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
