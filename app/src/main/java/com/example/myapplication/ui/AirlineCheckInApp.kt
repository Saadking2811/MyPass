package com.example.myapplication.ui

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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
    const val SPLASH         = "splash"
    const val SIGN_IN        = "signin"
    const val SIGN_UP        = "signup"
    const val MAIN           = "main"
    const val LOOKUP         = "lookup"
    const val PASSPORT       = "passport"
    const val REVIEW         = "review"
    const val SEAT           = "seat"
    const val BAGGAGE        = "baggage"
    const val REQUESTS       = "requests"
    const val PASS           = "pass"
    const val PREFERENCES    = "preferences"
}

private object Img {
    const val HERO_AIRPORT = "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=1200&q=80"
    const val HERO_PLANE   = "https://images.unsplash.com/photo-1569154941061-e231b4725ef1?w=1200&q=80"
    const val PROMO_BANNER = "https://images.unsplash.com/photo-1488085061387-422e29b40080?w=1200&q=80"

    const val PARIS    = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800&q=80"
    const val DUBAI    = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&q=80"
    const val ISTANBUL = "https://images.unsplash.com/photo-1527838832700-5059252407fa?w=800&q=80"
    const val LONDON   = "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=800&q=80"
    const val TOKYO    = "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=800&q=80"
    const val NYC      = "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?w=800&q=80"
    const val ROME     = "https://images.unsplash.com/photo-1531572753322-ad063cecc140?w=800&q=80"
    const val BARCELONA= "https://images.unsplash.com/photo-1583422409516-2895a77efded?w=800&q=80"

    const val INSPO_BEACH    = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&q=80"
    const val INSPO_MOUNTAIN = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&q=80"
}

private data class Destination(
    val code: String,
    val city: String,
    val country: String,
    val imageUrl: String,
    val price: String
)

private val popularDestinations = listOf(
    Destination("CDG", "Paris",     "France",  Img.PARIS,     ""),
    Destination("DXB", "Dubai",     "UAE",     Img.DUBAI,     ""),
    Destination("IST", "Istanbul",  "Turkey",  Img.ISTANBUL,  ""),
    Destination("LHR", "London",    "UK",      Img.LONDON,    ""),
    Destination("BCN", "Barcelona", "Spain",   Img.BARCELONA, ""),
    Destination("FCO", "Rome",      "Italy",   Img.ROME,      ""),
    Destination("NRT", "Tokyo",     "Japan",   Img.TOKYO,     ""),
    Destination("JFK", "New York",  "USA",     Img.NYC,       "")
)

// ─── Proper Algerian Flag (vertical green-white halves + red crescent + 5-pointed star) ───
@Composable
private fun AlgerianFlag(modifier: Modifier = Modifier, cornerRadius: Int = 2) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Left half green, right half white
        drawRect(EmeraldGreen, Offset(0f, 0f), Size(w / 2, h))
        drawRect(PureWhite,    Offset(w / 2, 0f), Size(w / 2, h))

        val cx = w / 2f
        val cy = h / 2f
        val outerR = h * 0.34f

        // Crescent (outer disk minus offset disk via even-odd fill)
        val crescent = androidx.compose.ui.graphics.Path().apply {
            fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
            addOval(androidx.compose.ui.geometry.Rect(
                Offset(cx - outerR, cy - outerR), Size(outerR * 2, outerR * 2)
            ))
            // inner disk slightly offset right to carve crescent shape
            val innerR = outerR * 0.84f
            addOval(androidx.compose.ui.geometry.Rect(
                Offset(cx - innerR + outerR * 0.18f, cy - innerR),
                Size(innerR * 2, innerR * 2)
            ))
        }
        drawPath(crescent, CrimsonRed)

        // 5-pointed red star inside the crescent opening
        val starCx = cx + outerR * 0.55f
        val starCy = cy
        val starOR = outerR * 0.36f
        val starIR = starOR * 0.40f
        val star = androidx.compose.ui.graphics.Path().apply {
            for (i in 0..9) {
                val r = if (i % 2 == 0) starOR else starIR
                val ang = (i * 36.0 - 90.0) * kotlin.math.PI / 180.0
                val vx = starCx + r * kotlin.math.cos(ang).toFloat()
                val vy = starCy + r * kotlin.math.sin(ang).toFloat()
                if (i == 0) moveTo(vx, vy) else lineTo(vx, vy)
            }
            close()
        }
        drawPath(star, CrimsonRed)
    }
}

// ═══════════════════════════════════════════════════════════════
//  MAIN APP — root navigation host
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
        state.statusMessage?.let { snackbarHost.showSnackbar(it); vm.clearMessage() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost   = { SnackbarHost(hostState = snackbarHost) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController     = navController,
                startDestination  = Routes.SPLASH,
                modifier          = Modifier.padding(innerPadding),
                enterTransition   = { fadeIn(tween(280)) + slideInHorizontally(tween(280)) { it / 6 } },
                exitTransition    = { fadeOut(tween(220)) + slideOutHorizontally(tween(220)) { -it / 8 } },
                popEnterTransition= { fadeIn(tween(280)) + slideInHorizontally(tween(280)) { -it / 6 } },
                popExitTransition = { fadeOut(tween(220)) + slideOutHorizontally(tween(220)) { it / 8 } }
            ) {
                composable(
                    Routes.SPLASH,
                    enterTransition = { EnterTransition.None }
                ) {
                    SplashScreen {
                        // Wait for session-restore to resolve before deciding the route.
                        // The splash animates ~3.2s which is plenty of time for /auth/me.
                        navController.navigate(
                            if (state.currentUser != null) Routes.MAIN else Routes.SIGN_IN
                        ) { popUpTo(Routes.SPLASH) { inclusive = true } }
                    }
                }
                composable(Routes.SIGN_IN) {
                    SignInScreen(state, vm,
                        onNavigateToHome = {
                            navController.navigate(Routes.MAIN) {
                                popUpTo(Routes.SIGN_IN) { inclusive = true }
                            }
                        },
                        onNavigateToSignUp = { navController.navigate(Routes.SIGN_UP) }
                    )
                }
                composable(Routes.SIGN_UP) {
                    SignUpScreen(state, vm,
                        onNavigateToHome = {
                            navController.navigate(Routes.MAIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.MAIN) {
                    MainScaffold(
                        state = state, vm = vm, s = s,
                        onStartCheckIn = { navController.navigate(Routes.LOOKUP) },
                        onOpenPreferences = { navController.navigate(Routes.PREFERENCES) },
                        onLogout = {
                            vm.logout()
                            navController.navigate(Routes.SIGN_IN) { popUpTo(0) { inclusive = true } }
                        }
                    )
                }
                composable(Routes.LOOKUP) {
                    BookingLookupScreen(state, vm,
                        onNext = { navController.navigate(Routes.PASSPORT) },
                        onBack = { navController.popBackStack() })
                }
                composable(Routes.PASSPORT) {
                    PassportScanScreen(vm, state,
                        onPassportScanned = { navController.navigate(Routes.REVIEW) },
                        onBack = { navController.popBackStack() })
                }
                composable(Routes.REVIEW) {
                    DetailsReviewScreen(state,
                        onNext = { navController.navigate(Routes.SEAT) },
                        onBack = { navController.popBackStack() })
                }
                composable(Routes.SEAT) {
                    SeatSelectionScreen(state, vm::updateSeat,
                        onNext = { navController.navigate(Routes.BAGGAGE) },
                        onBack = { navController.popBackStack() })
                }
                composable(Routes.BAGGAGE) {
                    BaggageScreen(state, vm::updateBaggage,
                        onNext = { navController.navigate(Routes.REQUESTS) },
                        onBack = { navController.popBackStack() })
                }
                composable(Routes.REQUESTS) {
                    SpecialRequestsScreen(state, vm::updateSpecialRequests,
                        onFinish = { vm.completeCheckIn(); navController.navigate(Routes.PASS) },
                        onBack = { navController.popBackStack() })
                }
                composable(Routes.PASS) {
                    BoardingPassScreen(state, vm, s,
                        onNewLookup = {
                            vm.resetForNewCheckIn()
                            navController.navigate(Routes.MAIN) { popUpTo(Routes.MAIN) { inclusive = true } }
                        },
                        onBack = { navController.popBackStack() })
                }
                composable(Routes.PREFERENCES) {
                    PreferencesScreen(preferencesManager, isDarkMode, currentLanguage, s) { navController.popBackStack() }
                }
            }

            if (state.isSyncing) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape  = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(color = EmeraldGreen, strokeWidth = 2.5.dp,
                                modifier = Modifier.size(28.dp))
                            Text("Syncing your data...", style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  SPLASH — Luxury depth particles + metallic gold sweep + horizon
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SplashScreen(onFinished: () -> Unit) {
    // Phase animatables
    val trailProg     = remember { Animatable(0f) }   // gold meteor sweeps in
    val burstAlpha    = remember { Animatable(0f) }   // light flash at impact
    val burstRadius   = remember { Animatable(0f) }
    val ringExpand    = remember { Animatable(0f) }   // expanding gold rings (shock waves)
    val emblemAlpha   = remember { Animatable(0f) }   // emblem appears
    val emblemScale   = remember { Animatable(0.4f) }
    val emblemRot     = remember { Animatable(-180f) }
    val wordmarkScale = remember { Animatable(0.6f) }
    val wordmarkAlpha = remember { Animatable(0f) }
    val underlineProg = remember { Animatable(0f) }
    val taglineAlpha  = remember { Animatable(0f) }
    val flagAlpha     = remember { Animatable(0f) }

    // Continuous shimmer + particles
    val anim = rememberInfiniteTransition(label = "splash")
    val shimmerX by anim.animateFloat(
        initialValue = -1.5f, targetValue = 2.5f,
        animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing)),
        label = "shim"
    )
    val particleTime by anim.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(11000, easing = LinearEasing)),
        label = "pt"
    )
    val emblemSpin by anim.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(14000, easing = LinearEasing)),
        label = "spin"
    )

    // Constellation particles — different depths
    data class Particle(val x: Float, val y: Float, val r: Float, val speed: Float, val depth: Float)
    val particles = remember {
        val rng = kotlin.random.Random(12345)
        List(70) {
            Particle(
                x = rng.nextFloat(),
                y = rng.nextFloat(),
                r = rng.nextFloat() * 1.3f + 0.3f,
                speed = rng.nextFloat() * 0.4f + 0.2f,
                depth = rng.nextFloat()
            )
        }
    }

    LaunchedEffect(Unit) {
        // Phase 1 (0-1100ms) — Gold meteor streaks across screen toward center
        launch { trailProg.animateTo(1f, tween(1100, easing = FastOutSlowInEasing)) }
        // Phase 2 (~900ms) — Impact burst flares
        launch {
            delay(900L)
            launch { burstAlpha.animateTo(1f, tween(180)) }
            launch { burstRadius.animateTo(1f, tween(450, easing = FastOutSlowInEasing)) }
            delay(380L)
            burstAlpha.animateTo(0f, tween(400))
        }
        // Phase 3 (1000-1600ms) — Shockwave gold ring expands outward
        launch {
            delay(1000L)
            ringExpand.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        }
        // Phase 4 (1100-1700ms) — Emblem materializes (rotation + scale)
        launch {
            delay(1100L)
            launch { emblemAlpha.animateTo(1f, tween(500)) }
            launch { emblemScale.animateTo(1f, tween(800, easing = EaseOutBack)) }
            launch { emblemRot.animateTo(0f, tween(900, easing = FastOutSlowInEasing)) }
        }
        // Phase 5 (1500-2100ms) — Wordmark expands with shimmer
        launch {
            delay(1500L)
            launch { wordmarkAlpha.animateTo(1f, tween(500)) }
            launch { wordmarkScale.animateTo(1f, tween(700, easing = EaseOutBack)) }
        }
        // Phase 6 (1900-2400ms) — Underline draws
        launch {
            delay(1900L)
            underlineProg.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }
        // Phase 7 — Tagline + flag
        launch { taglineAlpha.animateTo(1f, tween(500, delayMillis = 2200)) }
        launch { flagAlpha.animateTo(1f, tween(500, delayMillis = 2400)) }

        delay(3200)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(Color(0xFF02180F), Color(0xFF04261A), Color(0xFF062E20), Color(0xFF02180F))
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        // ── Layer 1: Drifting depth particles ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val driftY = (particleTime * p.speed) % 1f
                val y = ((p.y - driftY + 1f) % 1f) * size.height
                val x = p.x * size.width + kotlin.math.sin((particleTime * 6.28f + p.x * 10f).toDouble()).toFloat() * 7f
                val alpha = (0.12f + p.depth * 0.50f)
                val radius = (p.r * (0.6f + p.depth * 1.1f)).dp.toPx()
                drawCircle(
                    color = if (p.depth > 0.72f) LiquidGold.copy(alpha = alpha)
                            else Color.White.copy(alpha = alpha * 0.55f),
                    radius = radius,
                    center = Offset(x, y)
                )
                if (p.depth > 0.88f) {
                    drawCircle(LiquidGold.copy(alpha = alpha * 0.30f), radius * 2.4f, Offset(x, y))
                }
            }
            // Vignette anchored at impact point
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                    center = Offset(size.width / 2, size.height * 0.48f),
                    radius = size.maxDimension * 0.7f
                ),
                radius = size.maxDimension * 0.7f,
                center = Offset(size.width / 2, size.height * 0.48f)
            )
        }

        // ── Layer 2: Gold meteor trail (Bezier curve from off-screen-bottom-left to center) ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height * 0.48f
            val startX = -size.width * 0.15f
            val startY = size.height * 1.05f

            val prog = trailProg.value
            if (prog > 0f && prog < 1f) {
                // Trail tail (soft fading curve segments)
                val steps = 22
                for (i in 0..steps) {
                    val t1 = (prog - (i / steps.toFloat()) * 0.32f).coerceIn(0f, 1f)
                    if (t1 <= 0f) break
                    val t2 = (t1 - (1f / steps.toFloat()) * 0.32f).coerceIn(0f, 1f)

                    fun pt(t: Float): Offset {
                        // Quadratic Bezier curve
                        val ctrlX = size.width * 0.15f
                        val ctrlY = size.height * 0.30f
                        val x = (1 - t).let { it * it } * startX + 2 * (1 - t) * t * ctrlX + t * t * centerX
                        val y = (1 - t).let { it * it } * startY + 2 * (1 - t) * t * ctrlY + t * t * centerY
                        return Offset(x, y)
                    }

                    val p1 = pt(t1)
                    val p2 = pt(t2)
                    val alpha = ((1f - i / steps.toFloat()) * 0.55f).coerceAtLeast(0f)
                    val width = (4f - i * 0.13f).coerceAtLeast(0.6f)
                    drawLine(
                        color = LiquidGold.copy(alpha = alpha),
                        start = p1, end = p2,
                        strokeWidth = width.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Outer glow on first few segments
                    if (i < 6) {
                        drawLine(
                            color = LiquidGold.copy(alpha = alpha * 0.3f),
                            start = p1, end = p2,
                            strokeWidth = (width + 4f).dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
                // The meteor head — bright gold dot
                fun pt(t: Float): Offset {
                    val ctrlX = size.width * 0.15f
                    val ctrlY = size.height * 0.30f
                    val x = (1 - t).let { it * it } * startX + 2 * (1 - t) * t * ctrlX + t * t * centerX
                    val y = (1 - t).let { it * it } * startY + 2 * (1 - t) * t * ctrlY + t * t * centerY
                    return Offset(x, y)
                }
                val head = pt(prog)
                drawCircle(LiquidGold.copy(alpha = 0.4f), 14.dp.toPx(), head)
                drawCircle(LiquidGold.copy(alpha = 0.85f), 7.dp.toPx(), head)
                drawCircle(Color(0xFFFFFBEB), 3.dp.toPx(), head)
            }

            // ── Impact burst flash at the meeting point ──
            if (burstAlpha.value > 0f) {
                val r = burstRadius.value * 80f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFFBEB).copy(alpha = burstAlpha.value),
                            LiquidGold.copy(alpha = burstAlpha.value * 0.7f),
                            LiquidGold.copy(alpha = burstAlpha.value * 0.2f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = r.dp.toPx().coerceAtLeast(20f)
                    ),
                    radius = r.dp.toPx().coerceAtLeast(20f),
                    center = Offset(centerX, centerY)
                )
            }

            // ── Shockwave ring (expands outward from impact) ──
            if (ringExpand.value > 0f && ringExpand.value < 1f) {
                val ringR = (40f + ringExpand.value * 200f).dp.toPx()
                drawCircle(
                    color = LiquidGold.copy(alpha = (1f - ringExpand.value) * 0.55f),
                    radius = ringR,
                    center = Offset(centerX, centerY),
                    style = Stroke(1.4f)
                )
                // Second ring (delayed)
                val ring2 = (ringExpand.value - 0.2f).coerceAtLeast(0f)
                if (ring2 > 0f) {
                    val ringR2 = (40f + ring2 * 240f).dp.toPx()
                    drawCircle(
                        color = LiquidGold.copy(alpha = (1f - ring2) * 0.35f),
                        radius = ringR2,
                        center = Offset(centerX, centerY),
                        style = Stroke(0.8f)
                    )
                }
            }
        }

        // ── Wordmark + emblem column ──
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Emblem with rotating gold ring + airplane
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .graphicsLayer {
                        alpha = emblemAlpha.value
                        scaleX = emblemScale.value; scaleY = emblemScale.value
                        rotationZ = emblemRot.value
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = emblemSpin }
                ) {
                    val cx = size.width / 2; val cy = size.height / 2
                    drawCircle(LiquidGold.copy(alpha = 0.06f), size.minDimension / 2 + 16.dp.toPx(), Offset(cx, cy))
                    drawCircle(LiquidGold.copy(alpha = 0.10f), size.minDimension / 2 + 6.dp.toPx(),  Offset(cx, cy))
                    // Outer ring with gold gradient stroke (slowly rotating)
                    drawCircle(LiquidGold.copy(alpha = 0.85f), size.minDimension / 2.05f, Offset(cx, cy), style = Stroke(1.5f))
                    // Dashed inner ring (slow rotation)
                    val dashLen = 8f
                    val gapLen  = 6f
                    val innerR  = size.minDimension / 2.4f
                    val circumference = 2 * kotlin.math.PI * innerR
                    val dashCount = (circumference / (dashLen + gapLen)).toInt()
                    for (i in 0 until dashCount) {
                        val angle1 = (i * (dashLen + gapLen) / innerR)
                        val angle2 = angle1 + (dashLen / innerR)
                        drawArc(
                            color = Color(0xFFE6C46B).copy(alpha = 0.6f),
                            startAngle = (angle1 * 180.0 / kotlin.math.PI).toFloat(),
                            sweepAngle = ((angle2 - angle1) * 180.0 / kotlin.math.PI).toFloat(),
                            useCenter = false,
                            topLeft = Offset(cx - innerR, cy - innerR),
                            size = Size(innerR * 2, innerR * 2),
                            style = Stroke(0.8f)
                        )
                    }
                }
                Box(
                    modifier = Modifier.size(82.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF064E3B), Color(0xFF02180F))))
                        .border(1.dp, LiquidGold.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.FlightTakeoff, null,
                        modifier = Modifier.size(38.dp), tint = Color(0xFFEACD8A))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Wordmark with metallic gold sweep + scale entrance
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = wordmarkAlpha.value
                    scaleX = wordmarkScale.value; scaleY = wordmarkScale.value
                },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "MyPass",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight    = FontWeight.Black,
                        letterSpacing = (-1.6).sp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFE8DAB1),
                                Color(0xFFD4AF37),
                                Color(0xFFFFFBEB),
                                Color(0xFFD4AF37),
                                Color(0xFFE8DAB1)
                            ),
                            startX = (shimmerX - 0.4f) * 600f,
                            endX   = (shimmerX + 0.4f) * 600f
                        )
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Underline draws across
            Canvas(modifier = Modifier.width(180.dp).height(2.dp)) {
                val w = size.width * underlineProg.value
                val centerX = size.width / 2
                val halfW = w / 2
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, LiquidGold, Color(0xFFFFE9A8), LiquidGold, Color.Transparent)
                    ),
                    start = Offset(centerX - halfW, size.height / 2),
                    end   = Offset(centerX + halfW, size.height / 2),
                    strokeWidth = 1.5f, cap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                "ALGERIAN  ·  AVIATION",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 4.5.sp),
                color = Color(0xFFE8DAB1).copy(alpha = 0.78f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.graphicsLayer { alpha = taglineAlpha.value }
            )
        }

        // ── Bottom: Algerian flag ──
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .graphicsLayer { alpha = flagAlpha.value },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AlgerianFlag(
                modifier = Modifier
                    .size(width = 24.dp, height = 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .border(0.4.dp, LiquidGold.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            )
            Text(
                "MADE IN ALGERIA",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = Color(0xFFE8DAB1).copy(alpha = 0.55f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  AUTH — Sign In + Sign Up (separate screens with shared luxury hero)
// ═══════════════════════════════════════════════════════════════

/** Shared luxury hero — deep emerald + depth particles + gold emblem */
@Composable
private fun AuthLuxeHero(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null
) {
    val deepEmerald = Color(0xFF04261A)

    val anim = rememberInfiniteTransition(label = "heroAnim")
    val particleTime by anim.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(12000, easing = LinearEasing)),
        label = "ap"
    )

    data class HParticle(val x: Float, val y: Float, val r: Float, val speed: Float, val depth: Float)
    val particles = remember {
        val rng = kotlin.random.Random(7777)
        List(36) {
            HParticle(rng.nextFloat(), rng.nextFloat(),
                rng.nextFloat() * 1.2f + 0.3f,
                rng.nextFloat() * 0.4f + 0.2f,
                rng.nextFloat())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.32f)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF02180F), Color(0xFF04261A), Color(0xFF062E20))
                )
            )
    ) {
        // Ambient particle field — pure atmosphere, no central focal element
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val drift = (particleTime * p.speed) % 1f
                val y = ((p.y - drift + 1f) % 1f) * size.height
                val x = p.x * size.width + kotlin.math.sin((particleTime * 6.28f + p.x * 8f).toDouble()).toFloat() * 6f
                val alpha = 0.12f + p.depth * 0.45f
                val radius = (p.r * (0.5f + p.depth)).dp.toPx()
                drawCircle(
                    color = if (p.depth > 0.7f) LiquidGold.copy(alpha = alpha)
                            else Color.White.copy(alpha = alpha * 0.55f),
                    radius = radius,
                    center = Offset(x, y)
                )
                if (p.depth > 0.85f) {
                    drawCircle(LiquidGold.copy(alpha = alpha * 0.25f), radius * 2.2f, Offset(x, y))
                }
            }
            // Soft vignette to anchor the sheet card visually
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                    startY = size.height * 0.5f,
                    endY = size.height
                )
            )
        }

        // Refined top bar — only branding element
        Row(
            modifier = Modifier.fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(Color.White.copy(0.10f))
                        .border(0.5.dp, LiquidGold.copy(0.4f), CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                        tint = Color(0xFFE8DAB1), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0xFFD4AF37), Color(0xFFB8860B))))
                        .border(0.8.dp, Color(0xFFFFE9A8).copy(0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.FlightTakeoff, null,
                        tint = deepEmerald, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text("MyPass",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black, letterSpacing = (-0.4).sp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFFFFBEB), Color(0xFFE8DAB1))
                    )
                ))
            Spacer(modifier = Modifier.weight(1f))
            AlgerianFlag(
                modifier = Modifier
                    .size(width = 26.dp, height = 17.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .border(0.4.dp, LiquidGold.copy(0.5f), RoundedCornerShape(2.dp))
            )
        }

        // Hero space intentionally left atmospheric — focal content lives in the sheet card
    }
}

/** Reusable luxury sheet wrapper that slides up from below the hero */
@Composable
private fun AuthLuxeSheet(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible,
        enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(tween(800, delayMillis = 200)) { it / 4 }
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth().fillMaxHeight()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopCenter)) {
                    drawRect(Brush.horizontalGradient(
                        listOf(Color.Transparent, LiquidGold.copy(0.5f), LiquidGold, LiquidGold.copy(0.5f), Color.Transparent)
                    ))
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 26.dp)
                        .padding(top = 22.dp, bottom = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                    content()
                }
            }
        }
    }
}

/** Luxury primary action button — deep emerald gradient + gold edge + shimmer */
@Composable
private fun LuxButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val deepEmerald = Color(0xFF04261A)
    val anim = rememberInfiniteTransition(label = "btnShim")
    val shimmerX by anim.animateFloat(
        initialValue = -1.5f, targetValue = 2.5f,
        animationSpec = infiniteRepeatable(animation = tween(3200, easing = LinearEasing)),
        label = "bsh"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(8.dp, RoundedCornerShape(14.dp),
                ambientColor = deepEmerald.copy(0.4f),
                spotColor = deepEmerald.copy(0.4f))
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (enabled) Brush.linearGradient(listOf(deepEmerald, Color(0xFF064E3B), Color(0xFF02180F)))
                else         Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
            )
            .border(0.8.dp,
                if (enabled) LiquidGold.copy(0.5f) else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(14.dp))
            .clickable(enabled = enabled && !loading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (enabled) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val sx = (shimmerX - 0.4f) * size.width
                drawRect(Brush.horizontalGradient(
                    listOf(Color.Transparent, LiquidGold.copy(alpha = 0.18f), Color.Transparent),
                    startX = sx, endX = sx + 200f
                ))
            }
        }
        if (loading) {
            CircularProgressIndicator(color = LiquidGold, strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp))
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text,
                    style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 0.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.outline)
                if (enabled) Icon(Icons.AutoMirrored.Filled.ArrowForward, null,
                    tint = LiquidGold, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun SignInScreen(
    state: AppUiState, vm: AppViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.currentUser) {
        if (state.currentUser != null) onNavigateToHome()
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuthLuxeHero(
            title = "Welcome back",
            subtitle = "Sign in to continue your journey"
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.74f)
        ) {
            AuthLuxeSheet {
                // Title
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier
                        .width(3.dp).height(28.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(Brush.verticalGradient(listOf(LiquidGold, Color(0xFF8B6914)))))
                    Column {
                        Text("Sign in",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black, letterSpacing = (-0.6).sp),
                            color = MaterialTheme.colorScheme.onSurface)
                        Text("Use your MyPass credentials",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                CleanField("Email", email, keyboardType = KeyboardType.Email, onValueChange = { email = it })
                Spacer(modifier = Modifier.height(12.dp))
                CleanField("Password", password, keyboardType = KeyboardType.Password, isPassword = true, onValueChange = { password = it })

                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = {},
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
                    Text("  OR  ", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                        color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                }

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedButton(
                    onClick  = { vm.signInWithGoogle() },
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

                // Cross-link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("New to MyPass?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(onClick = onNavigateToSignUp, contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text("Create an account",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldGreen, fontWeight = FontWeight.Bold)
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

@Composable
private fun SignUpScreen(
    state: AppUiState, vm: AppViewModel,
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
            subtitle = "Join thousands of refined travelers",
            onBack = onBack
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.74f)
        ) {
            AuthLuxeSheet {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier
                        .width(3.dp).height(28.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(Brush.verticalGradient(listOf(LiquidGold, Color(0xFF8B6914)))))
                    Column {
                        Text("Register",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black, letterSpacing = (-0.6).sp),
                            color = MaterialTheme.colorScheme.onSurface)
                        Text("A few details to get you started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
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

                // Cross-link back to Sign In
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already registered?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text("Sign in",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldGreen, fontWeight = FontWeight.Bold)
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

@Composable
private fun CleanField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(12.dp),
        singleLine    = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldGreen,
            unfocusedBorderColor= MaterialTheme.colorScheme.outline,
            focusedLabelColor  = EmeraldGreen,
            cursorColor        = EmeraldGreen
        )
    )
}

// ═══════════════════════════════════════════════════════════════
//  PRIMARY/SECONDARY BUTTONS — clean, modern
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    val deepEmerald = Color(0xFF04261A)
    val anim = rememberInfiniteTransition(label = "primShim")
    val shimmerX by anim.animateFloat(
        initialValue = -1.5f, targetValue = 2.5f,
        animationSpec = infiniteRepeatable(animation = tween(3500, easing = LinearEasing)),
        label = "psh"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(if (enabled) 8.dp else 0.dp, RoundedCornerShape(14.dp),
                ambientColor = deepEmerald.copy(0.4f),
                spotColor = deepEmerald.copy(0.4f))
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (enabled) Brush.linearGradient(listOf(deepEmerald, Color(0xFF064E3B), Color(0xFF02180F)))
                else         Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
            )
            .border(0.8.dp,
                if (enabled) LiquidGold.copy(0.5f) else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(14.dp))
            .clickable(enabled = enabled && !loading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (enabled) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val sx = (shimmerX - 0.4f) * size.width
                drawRect(Brush.horizontalGradient(
                    listOf(Color.Transparent, LiquidGold.copy(alpha = 0.16f), Color.Transparent),
                    startX = sx, endX = sx + 200f
                ))
            }
        }
        if (loading) {
            CircularProgressIndicator(color = LiquidGold, strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp))
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (leadingIcon != null) {
                    Icon(leadingIcon, null, modifier = Modifier.size(18.dp),
                        tint = if (enabled) LiquidGold else MaterialTheme.colorScheme.outline)
                }
                Text(text,
                    style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 0.4.sp),
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.outline)
                if (leadingIcon == null && enabled) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null,
                        tint = LiquidGold, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SecondaryButton(
    text: String, onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(14.dp),
        border   = BorderStroke(1.5.dp, EmeraldGreen),
        colors   = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen)
    ) {
        Text(text,
            style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 0.3.sp),
            fontWeight = FontWeight.Bold)
    }
}

/** Tiny luxury section accent — gold mark next to titles */
@Composable
private fun GoldAccent(modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .width(3.dp).height(20.dp)
        .clip(RoundedCornerShape(1.5.dp))
        .background(Brush.verticalGradient(listOf(LiquidGold, Color(0xFF8B6914)))))
}

@Composable
private fun GoldDivider(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(Color.Transparent, LiquidGold.copy(0.5f), LiquidGold.copy(0.5f), Color.Transparent)
            ),
            start = Offset(0f, 0.5f),
            end   = Offset(size.width, 0.5f),
            strokeWidth = 0.8f
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  MAIN SCAFFOLD — Bottom Navigation
// ═══════════════════════════════════════════════════════════════

@Composable
private fun MainScaffold(
    state: AppUiState,
    vm: AppViewModel,
    s: (String) -> String,
    onStartCheckIn: () -> Unit,
    onOpenPreferences: () -> Unit,
    onLogout: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Box {
                // Gold accent line at top of nav bar
                Canvas(modifier = Modifier.fillMaxWidth().height(1.dp).align(Alignment.TopCenter)) {
                    drawRect(Brush.horizontalGradient(
                        listOf(Color.Transparent, LiquidGold.copy(0.4f), LiquidGold.copy(0.4f), Color.Transparent)
                    ))
                }
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    LuxNavItem(selected = tab == 0, icon = Icons.Filled.Home,
                        label = "Home", onClick = { tab = 0 })
                    LuxNavItem(selected = tab == 1, icon = Icons.Filled.ConfirmationNumber,
                        label = "Trips", onClick = { tab = 1 })
                    NavigationBarItem(
                        selected = false,
                        onClick = onStartCheckIn,
                        icon = {
                            Box(
                                modifier = Modifier.size(48.dp)
                                    .shadow(6.dp, CircleShape,
                                        ambientColor = Color(0xFF04261A).copy(0.5f),
                                        spotColor = Color(0xFF04261A).copy(0.5f))
                                    .clip(CircleShape)
                                    .background(Brush.radialGradient(listOf(EmeraldGreen, Color(0xFF02180F))))
                                    .border(1.dp, LiquidGold.copy(0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.QrCodeScanner, null,
                                    tint = LiquidGold, modifier = Modifier.size(22.dp))
                            }
                        },
                        label = {
                            Text("Check-in",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold)
                        }
                    )
                    LuxNavItem(selected = tab == 2, icon = Icons.Outlined.Place,
                        label = "Explore", onClick = { tab = 2 })
                    LuxNavItem(selected = tab == 3, icon = Icons.Filled.Person,
                        label = "Profile", onClick = { tab = 3 })
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> HomeTab(state, vm, s, onStartCheckIn = onStartCheckIn)
                1 -> TripsTab(state, s)
                2 -> ExploreTab()
                else -> ProfileTab(state, s, onOpenPreferences, onLogout)
            }
        }
    }
}

/** Luxury bottom nav item — gold underline on selected */
@Composable
private fun androidx.compose.foundation.layout.RowScope.LuxNavItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, null, modifier = Modifier.size(22.dp))
                if (selected) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier.size(width = 14.dp, height = 1.5.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(LiquidGold)
                    )
                }
            }
        },
        label = {
            Text(label,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor   = EmeraldGreen,
            selectedTextColor   = EmeraldGreen,
            indicatorColor      = Color.Transparent,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

// ═══════════════════════════════════════════════════════════════
//  HOME TAB — Search-first like Skyscanner / Trip.com
// ═══════════════════════════════════════════════════════════════

@Composable
private fun HomeTab(
    state: AppUiState, vm: AppViewModel, s: (String) -> String,
    onStartCheckIn: () -> Unit
) {
    val user = state.currentUser
    var bookingRef by rememberSaveable { mutableStateOf("") }
    var lastName   by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── Top App Bar — luxury gold seal + wordmark ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0xFFD4AF37), Color(0xFFB8860B))))
                        .border(0.6.dp, Color(0xFFFFE9A8).copy(0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.FlightTakeoff, null,
                        tint = Color(0xFF04261A), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("MyPass",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black, letterSpacing = (-0.4).sp),
                    color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Filled.NotificationsNone, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer).border(1.dp, EmeraldGreen.copy(0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user?.fullName?.take(1)?.uppercase() ?: "G",
                        fontWeight = FontWeight.Black, color = EmeraldGreen
                    )
                }
            }
        }

        // ── Greeting ──
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                Text(
                    "Hello, ${user?.fullName?.split(" ")?.first() ?: "Traveler"}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text("Where are you heading next?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // ── Hero Search Card with luxury gold accent ──
        item {
            Card(
                modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape     = RoundedCornerShape(22.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column {
                    // Top gold metallic line
                    Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                        drawRect(Brush.horizontalGradient(
                            listOf(Color.Transparent, LiquidGold.copy(0.6f), LiquidGold, LiquidGold.copy(0.6f), Color.Transparent)
                        ))
                    }
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            GoldAccent(modifier = Modifier.height(22.dp))
                            Column {
                                Text("Find your booking",
                                    style = MaterialTheme.typography.titleMedium.copy(letterSpacing = (-0.2).sp),
                                    fontWeight = FontWeight.Black)
                                Text("Enter reference + last name",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = bookingRef,
                        onValueChange = { bookingRef = it.uppercase() },
                        label = { Text("Booking reference") },
                        leadingIcon = { Icon(Icons.Filled.ConfirmationNumber, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = EmeraldGreen,
                            cursorColor = EmeraldGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it.uppercase() },
                        label = { Text("Last name") },
                        leadingIcon = { Icon(Icons.Filled.Person, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = EmeraldGreen,
                            cursorColor = EmeraldGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(
                        text = "Find Booking",
                        leadingIcon = Icons.Filled.Search,
                        onClick = {
                            if (bookingRef.isBlank() || lastName.isBlank()) {
                                bookingRef = "NM2025A"; lastName = "NAMOUNE"
                                vm.lookupFlight("NM2025A", "NAMOUNE")
                            } else {
                                vm.lookupFlight(bookingRef, lastName)
                            }
                            onStartCheckIn()
                        }
                    )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }

        // ── Quick chips ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickChip(Icons.Filled.QrCodeScanner, "Check-in", EmeraldGreen, onStartCheckIn, modifier = Modifier.weight(1f))
                QuickChip(Icons.Filled.AccessTime, "Status", StatusInfo, {}, modifier = Modifier.weight(1f))
                QuickChip(Icons.Filled.SupportAgent, "Support", CharcoalGray, {}, modifier = Modifier.weight(1f))
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }

        // ── Promotional banner with REAL image ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(180.dp),
                shape    = RoundedCornerShape(20.dp),
                elevation= CardDefaults.cardElevation(2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(Img.PROMO_BANNER).crossfade(true).build(),
                        contentDescription = "Promotion",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Dark overlay for text legibility
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 80f
                        )
                    ))
                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(LiquidGold)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("LIMITED TIME", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = PureWhite, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Summer in Europe",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                            color = PureWhite)
                        Text("Save up to 25% on selected flights",
                            style = MaterialTheme.typography.bodySmall, color = PureWhite.copy(0.85f))
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }

        // ── Popular Destinations ──
        item {
            SectionHeader("Popular destinations", "View all") {}
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(popularDestinations) { dest ->
                    DestinationCard(dest)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }

        // ── Upcoming Flights ──
        if (state.cachedFlights.isNotEmpty()) {
            item { SectionHeader("Your upcoming flights", null) {} }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            items(state.cachedFlights.take(3)) { flight ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    FlightSummaryCard(flight, s)
                }
            }
        }

        // ── Recent Boarding Passes ──
        if (state.cachedBoardingPasses.isNotEmpty()) {
            item { Spacer(modifier = Modifier.height(20.dp)) }
            item { SectionHeader("Recent boarding passes", null) {} }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            items(state.cachedBoardingPasses.take(3)) { pass ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation= CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.QrCode2, null, tint = EmeraldGreen, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${pass.origin} → ${pass.destination}",
                                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${pass.flightNumber} • Seat ${pass.seat}",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusPill(pass.status, StatusSuccess)
                        }
                    }
                }
            }
        }

        // ── Travel inspiration ──
        item { Spacer(modifier = Modifier.height(32.dp)) }
        item { SectionHeader("Travel inspiration", null) {} }
        item { Spacer(modifier = Modifier.height(12.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InspirationCard("Beach getaways", Img.INSPO_BEACH, modifier = Modifier.weight(1f))
                InspirationCard("Mountain escapes", Img.INSPO_MOUNTAIN, modifier = Modifier.weight(1f))
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun QuickChip(
    icon: ImageVector, label: String, accent: Color,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation= CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String?, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoldAccent(modifier = Modifier.height(18.dp))
            Text(title,
                style = MaterialTheme.typography.titleMedium.copy(letterSpacing = (-0.2).sp),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground)
        }
        if (action != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(action,
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.3.sp),
                    color = EmeraldGreen, fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp), tint = EmeraldGreen)
            }
        }
    }
}

@Composable
private fun DestinationCard(dest: Destination) {
    Card(
        modifier  = Modifier.width(180.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(dest.imageUrl).crossfade(true).build(),
                    contentDescription = dest.city,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Code chip top-left
                Box(
                    modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(0.55f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(dest.code, style = MaterialTheme.typography.labelSmall,
                        color = PureWhite, fontWeight = FontWeight.Black)
                }
                // Heart top-right
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                        .size(28.dp).clip(CircleShape).background(Color.White.copy(0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.FavoriteBorder, null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurface)
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(dest.city, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Place, null, modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dest.country, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun InspirationCard(title: String, imageUrl: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        shape    = RoundedCornerShape(16.dp),
        elevation= CardDefaults.cardElevation(1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.6f)), startY = 30f)
            ))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                color = PureWhite, modifier = Modifier.align(Alignment.BottomStart).padding(14.dp))
        }
    }
}

@Composable
private fun FlightSummaryCard(
    flight: com.example.myapplication.model.FlightItinerary,
    s: (String) -> String
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.FlightTakeoff, null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text(flight.flightNumber, style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(flight.airlineName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                StatusPill(
                    if (flight.checkInStatus == "Checked-In") "Checked in" else "Not checked in",
                    if (flight.checkInStatus == "Checked-In") StatusSuccess else StatusWarning
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(flight.origin,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(flight.originCity, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(flight.departureTime.takeLast(5), style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(flight.duration, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Canvas(modifier = Modifier.fillMaxWidth().height(20.dp)) {
                        val y = size.height / 2
                        drawCircle(SeatOccupied, 4f, Offset(8f, y))
                        drawLine(SeatOccupied, Offset(14f, y), Offset(size.width - 14f, y),
                            1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
                        drawCircle(EmeraldGreen, 4f, Offset(size.width - 8f, y))
                        // small plane icon dot
                        drawCircle(EmeraldGreen, 5f, Offset(size.width / 2, y))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Direct", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(flight.destination,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(flight.destinationCity, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(flight.arrivalTime.takeLast(5), style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                FlightDetailItem(Icons.Filled.CalendarMonth, flight.departureTime.take(10))
                FlightDetailItem(Icons.Filled.LocationOn, "Gate ${flight.gate}")
                FlightDetailItem(Icons.Filled.Apartment, "Term ${flight.terminal.ifBlank { "1" }}")
            }
        }
    }
}

@Composable
private fun FlightDetailItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

// ═══════════════════════════════════════════════════════════════
//  TRIPS TAB — Tabbed flights/passes
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TripsTab(state: AppUiState, s: (String) -> String) {
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoldAccent(modifier = Modifier.height(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("Your trips",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black, letterSpacing = (-0.6).sp),
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Flights & boarding passes",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TripTab("Flights",         tab == 0) { tab = 0 }
            TripTab("Boarding passes", tab == 1) { tab = 1 }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (tab == 0) {
                if (state.cachedFlights.isEmpty()) {
                    item { EmptyTripsCard(Icons.Filled.FlightTakeoff, "No flights yet", "Start a check-in to add flights here.") }
                } else {
                    items(state.cachedFlights) { flight -> FlightSummaryCard(flight, s) }
                }
            } else {
                if (state.cachedBoardingPasses.isEmpty()) {
                    item { EmptyTripsCard(Icons.Filled.QrCode2, "No boarding passes yet", "Complete a check-in to receive your boarding pass.") }
                } else {
                    items(state.cachedBoardingPasses) { pass -> BoardingPassListItem(pass) }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TripTab(
    title: String, selected: Boolean, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f).height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun BoardingPassListItem(pass: com.example.myapplication.model.BoardingPass) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.QrCode2, null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(pass.flightNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(pass.airlineName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                StatusPill(pass.status, StatusSuccess)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(pass.origin, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(pass.originCity, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(pass.destination, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(pass.destinationCity, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LabeledValue("Seat", pass.seat)
                LabeledValue("Gate", pass.gate)
                LabeledValue("Boarding", pass.boardingGroup)
                LabeledValue("Date", pass.issuedAt.take(10))
            }
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun EmptyTripsCard(icon: ImageVector, title: String, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation= CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = EmeraldGreen, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  EXPLORE TAB — Inspirational destinations
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ExploreTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoldAccent(modifier = Modifier.height(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Explore",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black, letterSpacing = (-0.6).sp),
                        color = MaterialTheme.colorScheme.onBackground)
                    Text("Discover your next destination",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(220.dp),
                shape    = RoundedCornerShape(20.dp),
                elevation= CardDefaults.cardElevation(2.dp)
            ) {
                Box {
                    AsyncImage(
                        model = Img.HERO_AIRPORT, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    )
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.65f)), startY = 60f)
                    ))
                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                        Text("Trending now", style = MaterialTheme.typography.labelMedium,
                            color = LightGreen, fontWeight = FontWeight.Bold)
                        Text("Summer escapes",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = PureWhite)
                        Text("Hand-picked destinations to inspire your next trip",
                            style = MaterialTheme.typography.bodySmall, color = PureWhite.copy(0.85f))
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(28.dp)) }
        item {
            Text("All destinations", modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
        item {
            // Grid of destinations
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().heightIn(min = 600.dp).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(popularDestinations) { dest ->
                    DestinationGridCard(dest)
                }
            }
        }
    }
}

@Composable
private fun DestinationGridCard(dest: Destination) {
    Card(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        shape    = RoundedCornerShape(14.dp),
        elevation= CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(model = dest.imageUrl, contentDescription = dest.city,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.7f)), startY = 60f)
            ))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(dest.city, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = PureWhite)
                Text(dest.country, style = MaterialTheme.typography.labelSmall,
                    color = PureWhite.copy(0.85f), fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  PROFILE TAB
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ProfileTab(
    state: AppUiState, s: (String) -> String,
    onOpenPreferences: () -> Unit, onLogout: () -> Unit
) {
    val user = state.currentUser
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoldAccent(modifier = Modifier.height(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Profile",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black, letterSpacing = (-0.6).sp))
                    Text("Account & preferences",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape    = RoundedCornerShape(18.dp),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation= CardDefaults.cardElevation(2.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer).border(1.dp, EmeraldGreen.copy(0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(user?.fullName?.take(1)?.uppercase() ?: "G",
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black,
                            color = EmeraldGreen)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user?.fullName ?: "Guest", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(user?.email ?: "Sign in to access your trips",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SoftGold).padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Filled.Star, null, tint = DeepGold, modifier = Modifier.size(11.dp))
                            Text("GOLD", style = MaterialTheme.typography.labelSmall, color = DeepGold,
                                fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { ProfileSectionLabel("Account") }
        item {
            ProfileMenuGroup {
                ProfileMenuItem(Icons.Filled.Person,        "Personal information", onClick = onOpenPreferences)
                Divider2()
                ProfileMenuItem(Icons.Filled.ConfirmationNumber, "My bookings", onClick = {})
                Divider2()
                ProfileMenuItem(Icons.Filled.Shield,        "Passport & documents", onClick = {})
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { ProfileSectionLabel("Preferences") }
        item {
            ProfileMenuGroup {
                ProfileMenuItem(Icons.Filled.Settings,   "App settings",     onClick = onOpenPreferences)
                Divider2()
                ProfileMenuItem(Icons.Filled.Notifications,"Notifications",  onClick = onOpenPreferences)
                Divider2()
                ProfileMenuItem(Icons.Filled.Language,   "Language",         onClick = onOpenPreferences)
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { ProfileSectionLabel("Support") }
        item {
            ProfileMenuGroup {
                ProfileMenuItem(Icons.Filled.SupportAgent,"Help center",      onClick = {})
                Divider2()
                ProfileMenuItem(Icons.AutoMirrored.Filled.Logout, "Log out",
                    iconTint = StatusError, onClick = onLogout)
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("MyPass v1.0  ·  Made in Algeria",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.width(6.dp))
                AlgerianFlag(modifier = Modifier.size(width = 18.dp, height = 12.dp).clip(RoundedCornerShape(2.dp)))
            }
        }
    }
}

@Composable
private fun ProfileSectionLabel(text: String) {
    Text(
        text.uppercase(),
        modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ProfileMenuGroup(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation= CardDefaults.cardElevation(1.dp)
    ) { Column { content() } }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector, title: String,
    iconTint: Color? = null, onClick: () -> Unit
) {
    val resolvedTint = iconTint ?: MaterialTheme.colorScheme.onSurface
    val isError = iconTint == StatusError
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = resolvedTint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium,
            color = if (isError) StatusError else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun Divider2() {
    HorizontalDivider(modifier = Modifier.padding(start = 52.dp), color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
}

// ═══════════════════════════════════════════════════════════════
//  IN-FLOW SCREENS — Lookup, Passport, Review, Seat, Baggage, Requests
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ScreenScaffold(
    title: String, subtitle: String? = null, onBack: () -> Unit,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    GoldAccent(modifier = Modifier.height(26.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(title,
                            style = MaterialTheme.typography.titleLarge.copy(letterSpacing = (-0.3).sp),
                            fontWeight = FontWeight.Black)
                        if (subtitle != null) {
                            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                GoldDivider()
            }
        },
        bottomBar = { if (bottomBar != null) bottomBar() }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            content()
        }
    }
}

@Composable
private fun BookingLookupScreen(
    state: AppUiState, vm: AppViewModel,
    onNext: () -> Unit, onBack: () -> Unit
) {
    var bookingRef by remember { mutableStateOf("NM2025A") }
    var lastName   by remember { mutableStateOf("NAMOUNE") }

    ScreenScaffold(
        title = "Find your booking", subtitle = "Step 1 of 6", onBack = onBack,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    PrimaryButton(
                        text = "Continue",
                        onClick = onNext,
                        enabled = state.lookupResult != null
                    )
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation= CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Booking details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = bookingRef, onValueChange = { bookingRef = it.uppercase() },
                        label = { Text("Booking reference") },
                        leadingIcon = { Icon(Icons.Filled.ConfirmationNumber, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = EmeraldGreen, cursorColor = EmeraldGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = lastName, onValueChange = { lastName = it.uppercase() },
                        label = { Text("Last name") },
                        leadingIcon = { Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = EmeraldGreen, cursorColor = EmeraldGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SecondaryButton("Search Flight", onClick = { vm.lookupFlight(bookingRef, lastName) })
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            AnimatedVisibility(state.lookupResult != null,
                enter = fadeIn() + slideInVertically { 30 }, exit = fadeOut()) {
                state.lookupResult?.let { flight ->
                    FlightSummaryCard(flight) { it }
                }
            }
            if (state.isLoading) {
                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EmeraldGreen)
                }
            }
        }
    }
}

// ── PASSPORT SCAN ──

@Composable
private fun PassportScanScreen(
    vm: AppViewModel, state: AppUiState,
    onPassportScanned: () -> Unit, onBack: () -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var mrzResult    by remember { mutableStateOf<MrzParser.MrzResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var scanStatus   by remember { mutableStateOf("Position your passport in the frame") }

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
                Text("Grant camera access to scan your passport.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
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
                                    mrzResult = result
                                    scanStatus = "Passport detected"
                                    val passportInfo = PassportInfo(
                                        fullName       = "${result.firstName} ${result.lastName}".trim(),
                                        passportNumber = result.passportNumber,
                                        nationality    = result.nationality,
                                        dateOfBirth    = result.dateOfBirth,
                                        expiryDate     = result.expiryDate,
                                        gender         = result.sex,
                                        rawText        = result.rawMrz,
                                        verified       = result.isValid
                                    )
                                    vm.attachPassportInfo(passportInfo)
                                } else { scanStatus = "Scanning MRZ zone..." }
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
                            Text("ICAO 9303 MRZ • ${(r.confidence * 100).toInt()}% confidence",
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Card(shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(0.dp)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ScanField("Name",         "${r.firstName} ${r.lastName}")
                            ScanField("Passport No.", r.passportNumber)
                            ScanField("Nationality",  MrzParser.getCountryName(r.nationality))
                            ScanField("Date of birth", r.dateOfBirth)
                            ScanField("Expiry",       r.expiryDate)
                        }
                    }
                    PrimaryButton("Continue", onClick = onPassportScanned)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = EmeraldGreen)
                        Column {
                            Text(scanStatus, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("Hold steady — MRZ scanning...",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        animationSpec = infiniteRepeatable(animation = tween(2400)),
        label = "y"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val wW = size.width * 0.86f
        val wH = wW * 0.63f
        val left = (size.width - wW) / 2
        val top  = (size.height - wH) / 2 - 100f

        drawRect(Color.Black.copy(0.65f))
        drawRoundRect(Color.Transparent, Offset(left, top), Size(wW, wH),
            CornerRadius(14f), blendMode = BlendMode.Clear)

        val borderColor = if (isScanning) PureWhite.copy(0.7f) else EmeraldGreen
        drawRoundRect(borderColor, Offset(left, top), Size(wW, wH), CornerRadius(14f),
            style = Stroke(2f))

        // Corner brackets
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

// ── DETAILS REVIEW ──

@Composable
private fun DetailsReviewScreen(state: AppUiState, onNext: () -> Unit, onBack: () -> Unit) {
    val draft = state.draft ?: return
    ScreenScaffold(
        title = "Review details", subtitle = "Step 3 of 6", onBack = onBack,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    PrimaryButton("Continue to seat selection", onClick = onNext)
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Flight information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DetailRow("Passenger", draft.itinerary.passengerName)
                    DetailRow("Flight",    "${draft.itinerary.flightNumber} (${draft.itinerary.airlineName})")
                    DetailRow("Route",     "${draft.itinerary.origin} → ${draft.itinerary.destination}")
                    DetailRow("Departure", draft.itinerary.departureTime)
                    DetailRow("Aircraft",  draft.itinerary.aircraftType)
                }
            }
            draft.passportInfo?.let { passport ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MintGreen),
                    elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.CheckCircle, null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                            Text("Passport verified", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                        HorizontalDivider(color = EmeraldGreen.copy(0.2f))
                        DetailRow("Name",         passport.fullName)
                        DetailRow("Passport no.", passport.passportNumber)
                        DetailRow("Nationality",  MrzParser.getCountryName(passport.nationality))
                        DetailRow("Birth",        passport.dateOfBirth)
                        DetailRow("Expiry",       passport.expiryDate)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ── SEAT SELECTION ──

@Composable
private fun SeatSelectionScreen(state: AppUiState, onSeatSelected: (String) -> Unit, onNext: () -> Unit, onBack: () -> Unit) {
    val selectedSeat = state.draft?.selectedSeat
    ScreenScaffold(
        title = "Choose your seat", subtitle = "Step 4 of 6", onBack = onBack,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    if (selectedSeat != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Selected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(selectedSeat, style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black, color = EmeraldGreen)
                            }
                            Text("Standard seat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    PrimaryButton("Confirm seat", onClick = onNext, enabled = !selectedSeat.isNullOrBlank())
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(8.dp))
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LegendItem("Available", SeatAvailable)
                LegendItem("Premium",   SeatPremium)
                LegendItem("Selected",  EmeraldGreen)
                LegendItem("Taken",     SeatOccupied)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).heightIn(min = 480.dp),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation= CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    // Header row labels
                    Row(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 6.dp)) {
                        listOf("A", "B", "C", "", "D", "E", "F").forEachIndexed { i, l ->
                            if (l.isEmpty()) Spacer(modifier = Modifier.width(20.dp))
                            else Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(l, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    // Seat grid
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val seats = state.seatMap
                        val rows = seats.groupBy { it.row }.toSortedMap()
                        items(rows.entries.toList()) { (rowNum, rowSeats) ->
                            val sortedSeats = rowSeats.sortedBy { it.column }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                sortedSeats.forEachIndexed { i, seat ->
                                    if (i == 3) {
                                        Text("$rowNum", style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                                    }
                                    SeatChip(
                                        seat = seat,
                                        isSelected = seat.seatCode == selectedSeat,
                                        onClick = { if (!seat.occupied) onSeatSelected(seat.seatCode) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(modifier = Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SeatChip(
    seat: com.example.myapplication.model.Seat,
    isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "ss")
    val bg = when {
        seat.occupied -> SeatOccupied
        isSelected    -> EmeraldGreen
        seat.premium  -> SeatPremium
        else          -> SeatAvailable
    }
    Box(
        modifier = modifier
            .heightIn(min = 36.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(enabled = !seat.occupied, onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(seat.seatCode.last().toString(),
            style = MaterialTheme.typography.labelMedium,
            color = when {
                seat.occupied -> MaterialTheme.colorScheme.onSurfaceVariant
                isSelected -> PureWhite
                seat.premium -> DeepGold
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
    }
}

// ── BAGGAGE ──

@Composable
private fun BaggageScreen(state: AppUiState, onUpdate: (Int, Int, Int) -> Unit, onNext: () -> Unit, onBack: () -> Unit) {
    var checked   by remember { mutableIntStateOf(state.draft?.baggageDeclaration?.checkedBags ?: 0) }
    var carryOn   by remember { mutableIntStateOf(state.draft?.baggageDeclaration?.carryOnBags ?: 1) }
    var oversized by remember { mutableIntStateOf(state.draft?.baggageDeclaration?.oversizedBags ?: 0) }

    ScreenScaffold(
        title = "Baggage", subtitle = "Step 5 of 6", onBack = onBack,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    PrimaryButton("Save & continue", onClick = { onUpdate(checked, carryOn, oversized); onNext() })
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BagRow("Checked bags",  "23 kg each",  Icons.Filled.Luggage, EmeraldGreen, checked)  { checked = it.coerceAtLeast(0) }
            BagRow("Carry-on bags", "7 kg each",   Icons.Filled.Luggage, StatusInfo,    carryOn)  { carryOn = it.coerceAtLeast(0) }
            BagRow("Oversized",     "Special items",Icons.Filled.Luggage, LiquidGold,   oversized){ oversized = it.coerceAtLeast(0) }
            Spacer(modifier = Modifier.height(8.dp))
            val totalKg = checked * 23.0 + carryOn * 7.0
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MintGreen),
                elevation = CardDefaults.cardElevation(0.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total estimated weight", style = MaterialTheme.typography.bodyMedium, color = DarkGreen)
                    Text("${"%.1f".format(totalKg)} kg", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black, color = EmeraldGreen)
                }
            }
        }
    }
}

@Composable
private fun BagRow(title: String, subtitle: String, icon: ImageVector, accent: Color,
                  value: Int, onChange: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CounterBtn("−") { onChange(value - 1) }
                Text("$value", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black,
                    modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                CounterBtn("+") { onChange(value + 1) }
            }
        }
    }
}

@Composable
private fun CounterBtn(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(32.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ── SPECIAL REQUESTS ──

@Composable
private fun SpecialRequestsScreen(
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
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp), modifier = Modifier.fillMaxWidth()) {
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

// ═══════════════════════════════════════════════════════════════
//  BOARDING PASS — Clean professional, like real airline apps
// ═══════════════════════════════════════════════════════════════

@Composable
private fun BoardingPassScreen(
    state: AppUiState, vm: AppViewModel, s: (String) -> String,
    onNewLookup: () -> Unit, onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val pass    = state.latestBoardingPass ?: state.cachedBoardingPasses.firstOrNull()

    if (pass == null) {
        ScreenScaffold(title = "Boarding pass", onBack = onBack) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (state.isLoading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = EmeraldGreen, strokeWidth = 2.5.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Generating boarding pass...")
                    }
                } else { Text("No boarding pass yet.") }
            }
        }
        return
    }

    val qrBitmap = remember(pass.qrPayload) { vm.generateQrBitmap(pass.qrPayload) }

    ScreenScaffold(
        title = "Boarding pass", subtitle = "Confirmed", onBack = onBack,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick  = onNewLookup,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape    = RoundedCornerShape(14.dp),
                        border   = BorderStroke(1.dp, LiquidGold.copy(0.6f)),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text("Done",
                            style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 0.5.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface)
                    }
                    // Add to Wallet — luxury gradient with wallet icon
                    val deepEmerald = Color(0xFF04261A)
                    Box(
                        modifier = Modifier
                            .weight(1f).height(50.dp)
                            .shadow(8.dp, RoundedCornerShape(14.dp),
                                ambientColor = deepEmerald.copy(0.4f), spotColor = deepEmerald.copy(0.4f))
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(deepEmerald, Color(0xFF064E3B), Color(0xFF02180F))))
                            .border(0.8.dp, LiquidGold.copy(0.5f), RoundedCornerShape(14.dp))
                            .clickable {
                                scope.launch {
                                    try {
                                        val fileName = vm.saveBoardingPassPdf(context, pass)
                                        vm.showMessage("Added to Wallet • $fileName")
                                    } catch (e: Exception) {
                                        vm.showMessage("Failed: ${e.message}")
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.AccountBalanceWallet, null,
                                tint = LiquidGold, modifier = Modifier.size(20.dp))
                            Text("Add to Wallet",
                                style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 0.3.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFFBEB))
                        }
                    }
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(0.8.dp, LiquidGold.copy(0.45f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(Brush.radialGradient(listOf(EmeraldGreen, Color(0xFF02180F))))
                                .border(0.6.dp, LiquidGold.copy(0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, tint = LiquidGold, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Check-in complete",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleSmall.copy(letterSpacing = (-0.2).sp))
                            Text("Your boarding pass is ready",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.8f))
                        }
                    }
                }
            }

            // Boarding pass card
            item { CleanBoardingPassCard(pass, qrBitmap = qrBitmap) }
        }
    }
}

@Composable
private fun CleanBoardingPassCard(
    pass: com.example.myapplication.model.BoardingPass,
    qrBitmap: android.graphics.Bitmap
) {
    val cream    = Color(0xFFFAF7F0)
    val charcoal = Color(0xFF1A1A1A)
    val deepEmerald = Color(0xFF04261A)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(22.dp),
        colors   = CardDefaults.cardColors(containerColor = cream),
        elevation= CardDefaults.cardElevation(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Luxury Header — deep emerald with subtle pattern + gold seal ──
            Box(modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(
                    listOf(deepEmerald, Color(0xFF064E3B), Color(0xFF02180F))
                ))
                .padding(horizontal = 24.dp, vertical = 22.dp)
            ) {
                // Watermark pattern: subtle gold diamonds
                Canvas(modifier = Modifier.matchParentSize()) {
                    val gold = LiquidGold.copy(alpha = 0.04f)
                    val sp = 28f
                    for (i in -2..(size.width / sp).toInt() + 2) {
                        for (j in -2..(size.height / sp).toInt() + 2) {
                            val cx = i * sp + (if (j % 2 == 0) 0f else sp / 2)
                            val cy = j * sp
                            val r = 2f
                            drawLine(gold, Offset(cx, cy - r), Offset(cx + r, cy), 0.6f)
                            drawLine(gold, Offset(cx + r, cy), Offset(cx, cy + r), 0.6f)
                            drawLine(gold, Offset(cx, cy + r), Offset(cx - r, cy), 0.6f)
                            drawLine(gold, Offset(cx - r, cy), Offset(cx, cy - r), 0.6f)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    // Gold seal with crescent — Algerian touch (no airline name)
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFFD4AF37), Color(0xFFB8860B))))
                            .border(1.dp, Color(0xFFFFE9A8).copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.FlightTakeoff, null,
                            tint = deepEmerald, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("MyPass",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black, letterSpacing = (-0.3).sp,
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFFFFFBEB), Color(0xFFE8DAB1))
                                )
                            ))
                        Text("BOARDING PASS",
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.4.sp),
                            color = Color(0xFFE8DAB1).copy(alpha = 0.78f),
                            fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("FLIGHT", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                            color = Color(0xFFE8DAB1).copy(alpha = 0.6f))
                        Text(pass.flightNumber, style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black, color = Color(0xFFFFFBEB))
                    }
                }
            }

            // ── Gold metallic accent line ──
            Canvas(modifier = Modifier.fillMaxWidth().height(3.dp)) {
                drawRect(Brush.horizontalGradient(listOf(
                    Color(0xFF8B6914), Color(0xFFD4AF37), Color(0xFFFFE9A8),
                    Color(0xFFD4AF37), Color(0xFF8B6914)
                )))
            }

            Column(modifier = Modifier.padding(horizontal = 26.dp, vertical = 24.dp)) {
                // ── Route — refined typography ──
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(pass.origin, style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black, letterSpacing = (-2).sp,
                            fontSize = 42.sp
                        ), color = deepEmerald)
                        Text(pass.originCity.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.3.sp),
                            color = charcoal.copy(0.65f), fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(pass.departureTime.takeLast(5),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = charcoal)
                    }
                    Column(
                        modifier = Modifier.weight(0.9f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        // Gold dotted route line with plane icon
                        Canvas(modifier = Modifier.fillMaxWidth().height(28.dp)) {
                            val y = size.height / 2
                            drawLine(LiquidGold.copy(0.55f), Offset(8f, y), Offset(size.width / 2 - 14f, y),
                                1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.5f, 3.5f)))
                            drawLine(LiquidGold.copy(0.55f), Offset(size.width / 2 + 14f, y), Offset(size.width - 8f, y),
                                1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.5f, 3.5f)))
                        }
                        Spacer(modifier = Modifier.height((-22).dp))
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape)
                                .background(deepEmerald)
                                .border(1.dp, LiquidGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.FlightTakeoff, null,
                                tint = LiquidGold, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("DIRECT",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                            color = LiquidGold, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(pass.destination, style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black, letterSpacing = (-2).sp,
                            fontSize = 42.sp
                        ), color = deepEmerald)
                        Text(pass.destinationCity.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.3.sp),
                            color = charcoal.copy(0.65f), fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(pass.arrivalTime.takeLast(5),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = charcoal)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Refined gold dot divider ──
                Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                    val cy = size.height / 2
                    val gap = 10f
                    var x = 4f
                    while (x < size.width) {
                        drawCircle(LiquidGold.copy(0.55f), 0.9f, Offset(x, cy))
                        x += gap
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Passenger — most prominent on a real boarding pass ──
                Text("PASSENGER",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = LiquidGold, fontWeight = FontWeight.Bold)
                Text(pass.passengerName.uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
                    color = deepEmerald)

                Spacer(modifier = Modifier.height(22.dp))

                // ── Info grid (luxury embossed style with cream/gold) ──
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    LuxBlock("SEAT",     pass.seat,                       modifier = Modifier.weight(1f))
                    LuxDivider()
                    LuxBlock("GATE",     pass.gate,                       modifier = Modifier.weight(1f))
                    LuxDivider()
                    LuxBlock("GROUP",    pass.boardingGroup,              modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    LuxBlock("DEPART",   pass.departureTime.takeLast(5),  modifier = Modifier.weight(1f))
                    LuxDivider()
                    LuxBlock("ARRIVAL",  pass.arrivalTime.takeLast(5),    modifier = Modifier.weight(1f))
                    LuxDivider()
                    LuxBlock("TERMINAL", pass.terminal.ifBlank { "—" },   modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    LuxBlock("CLASS",    pass.seatClass,                  modifier = Modifier.weight(1f))
                    LuxDivider()
                    LuxBlock("SEQUENCE", pass.sequence,                   modifier = Modifier.weight(1f))
                    LuxDivider()
                    LuxBlock("BAGGAGE",  pass.baggageInfo.take(8).ifBlank { "—" }, modifier = Modifier.weight(1f))
                }
            }

            // ── Perforated tear (luxury with cream + gold tint) ──
            Canvas(modifier = Modifier.fillMaxWidth().height(22.dp)) {
                val y = size.height / 2
                drawLine(LiquidGold.copy(0.4f), Offset(28f, y), Offset(size.width - 28f, y),
                    1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
                drawCircle(deepEmerald.copy(0.06f), 14f, Offset(-3f, y))
                drawCircle(LiquidGold.copy(0.5f), 14f, Offset(-3f, y), style = Stroke(0.8f))
                drawCircle(deepEmerald.copy(0.06f), 14f, Offset(size.width + 3f, y))
                drawCircle(LiquidGold.copy(0.5f), 14f, Offset(size.width + 3f, y), style = Stroke(0.8f))
            }

            Column(modifier = Modifier.padding(horizontal = 26.dp).padding(top = 4.dp, bottom = 24.dp)) {
                // ── QR with luxury gold-bordered frame ──
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(196.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PureWhite)
                                .border(1.dp, LiquidGold.copy(0.5f), RoundedCornerShape(12.dp))
                        ) {
                            // Inner subtle shadow ring
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val r = 12.dp.toPx()
                                drawRoundRect(
                                    color = LiquidGold.copy(0.15f),
                                    topLeft = Offset(4f, 4f),
                                    size = Size(size.width - 8f, size.height - 8f),
                                    cornerRadius = CornerRadius(r * 0.7f),
                                    style = Stroke(0.6f)
                                )
                            }
                            // Gold corner brackets
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val len = 14f; val w = 1.5f
                                val pad = 8f
                                drawLine(LiquidGold, Offset(pad, pad), Offset(pad + len, pad), w, StrokeCap.Round)
                                drawLine(LiquidGold, Offset(pad, pad), Offset(pad, pad + len), w, StrokeCap.Round)
                                drawLine(LiquidGold, Offset(size.width - pad, pad), Offset(size.width - pad - len, pad), w, StrokeCap.Round)
                                drawLine(LiquidGold, Offset(size.width - pad, pad), Offset(size.width - pad, pad + len), w, StrokeCap.Round)
                                drawLine(LiquidGold, Offset(pad, size.height - pad), Offset(pad + len, size.height - pad), w, StrokeCap.Round)
                                drawLine(LiquidGold, Offset(pad, size.height - pad), Offset(pad, size.height - pad - len), w, StrokeCap.Round)
                                drawLine(LiquidGold, Offset(size.width - pad, size.height - pad), Offset(size.width - pad - len, size.height - pad), w, StrokeCap.Round)
                                drawLine(LiquidGold, Offset(size.width - pad, size.height - pad), Offset(size.width - pad, size.height - pad - len), w, StrokeCap.Round)
                            }
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Boarding QR",
                                modifier = Modifier.fillMaxSize().padding(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Scan at gate",
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                            color = charcoal.copy(0.6f), fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Footer ornament: gold dots + flag + status ──
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Canvas(modifier = Modifier.size(width = 20.dp, height = 1.dp)) {
                            drawLine(LiquidGold, Offset(0f, size.height/2), Offset(size.width, size.height/2), 1f)
                        }
                        AlgerianFlag(modifier = Modifier
                            .size(width = 20.dp, height = 13.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .border(0.4.dp, LiquidGold.copy(0.5f), RoundedCornerShape(1.dp)))
                        Canvas(modifier = Modifier.size(width = 20.dp, height = 1.dp)) {
                            drawLine(LiquidGold, Offset(0f, size.height/2), Offset(size.width, size.height/2), 1f)
                        }
                    }
                    Text(pass.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                        color = LiquidGold, fontWeight = FontWeight.Black,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            .border(0.6.dp, LiquidGold, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
        }
    }
}

@Composable
private fun LuxBlock(label: String, value: String, modifier: Modifier = Modifier) {
    val deepEmerald = Color(0xFF04261A)
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp),
            color = LiquidGold, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(3.dp))
        Text(value, style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Black, letterSpacing = (-0.3).sp),
            color = deepEmerald)
    }
}

@Composable
private fun LuxDivider() {
    Box(modifier = Modifier
        .width(0.6.dp)
        .height(40.dp)
        .background(LiquidGold.copy(0.3f))
        .padding(vertical = 4.dp))
}

// ═══════════════════════════════════════════════════════════════
//  PREFERENCES
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PreferencesScreen(
    preferencesManager: PreferencesManager?,
    isDarkMode: Boolean, currentLanguage: String,
    s: (String) -> String, onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var darkMode    by remember { mutableStateOf(isDarkMode) }
    var selectedLang by remember { mutableStateOf(currentLanguage) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var biometricEnabled     by remember { mutableStateOf(false) }
    var serverUrl by remember { mutableStateOf(com.example.myapplication.network.RetrofitClient.getBaseUrl()) }
    var serverStatus by remember { mutableStateOf<String?>(null) }
    var probing by remember { mutableStateOf(false) }

    LaunchedEffect(preferencesManager) { preferencesManager?.notificationsEnabled?.collect { notificationsEnabled = it } }
    LaunchedEffect(preferencesManager) { preferencesManager?.biometricEnabled?.collect { biometricEnabled = it } }

    ScreenScaffold(title = "Settings", onBack = onBack) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 16.dp)) {
            // ── Server (backend) section ──
            item { ProfileSectionLabel("Backend Server") }
            item {
                ProfileMenuGroup {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Server URL",
                            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("PostgreSQL-backed API. Use 10.0.2.2:8082 for emulator, or your PC's LAN IP for a real device.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            placeholder = { Text("http://10.0.2.2:8082/api/") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen,
                                focusedLabelColor = EmeraldGreen,
                                cursorColor = EmeraldGreen
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    com.example.myapplication.network.RetrofitClient.setBaseUrl(serverUrl)
                                    serverStatus = "Saved"
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, EmeraldGreen),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen)
                            ) { Text("Save", fontWeight = FontWeight.SemiBold) }
                            Button(
                                onClick = {
                                    probing = true
                                    com.example.myapplication.network.RetrofitClient.setBaseUrl(serverUrl)
                                    scope.launch {
                                        val err = com.example.myapplication.network.RetrofitClient.probe()
                                        serverStatus = if (err == null) "✓ Connected" else "✗ $err"
                                        probing = false
                                    }
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = PureWhite),
                                enabled = !probing
                            ) {
                                if (probing) {
                                    CircularProgressIndicator(color = PureWhite, strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp))
                                } else Text("Test Connection", fontWeight = FontWeight.SemiBold)
                            }
                        }
                        serverStatus?.let { status ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (status.startsWith("✓")) EmeraldGreen.copy(alpha = 0.10f)
                                        else if (status.startsWith("✗")) CrimsonRed.copy(alpha = 0.10f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(status,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (status.startsWith("✓")) EmeraldGreen
                                            else if (status.startsWith("✗")) CrimsonRed
                                            else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item { ProfileSectionLabel("Appearance") }
            item {
                ProfileMenuGroup {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dark mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("Use dark theme across the app",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = darkMode, onCheckedChange = {
                            darkMode = it; scope.launch { preferencesManager?.setDarkMode(it) }
                        }, colors = SwitchDefaults.colors(checkedTrackColor = EmeraldGreen))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item { ProfileSectionLabel("Language") }
            item {
                ProfileMenuGroup {
                    val languages = listOf(
                        Triple("en", "English",  "EN"),
                        Triple("fr", "Français", "FR"),
                        Triple("ar", "العربية",  "AR")
                    )
                    languages.forEachIndexed { i, (code, name, badge) ->
                        val isSelected = selectedLang == code
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { selectedLang = code; scope.launch { preferencesManager?.setLanguage(code) } }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Country-code badge (clean, mode-aware)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) EmeraldGreen.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(badge,
                                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp),
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(name, style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            if (isSelected) Icon(Icons.Filled.Check, null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                        }
                        if (i < languages.size - 1) Divider2()
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item { ProfileSectionLabel("Notifications & Security") }
            item {
                ProfileMenuGroup {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Notifications, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Push notifications", style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Switch(checked = notificationsEnabled, onCheckedChange = {
                            notificationsEnabled = it; scope.launch { preferencesManager?.setNotifications(it) }
                        }, colors = SwitchDefaults.colors(checkedTrackColor = EmeraldGreen))
                    }
                    Divider2()
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Shield, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Biometric authentication", style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Switch(checked = biometricEnabled, onCheckedChange = {
                            biometricEnabled = it; scope.launch { preferencesManager?.setBiometric(it) }
                        }, colors = SwitchDefaults.colors(checkedTrackColor = EmeraldGreen))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MyPass", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black,
                        color = EmeraldGreen)
                    Text("Version 1.0.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("© 2026 MyPass. All rights reserved.",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
